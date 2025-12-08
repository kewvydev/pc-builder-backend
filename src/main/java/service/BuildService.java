package service;

import com.pcBuilder.backend.model.build.Build;
import com.pcBuilder.backend.model.compatibility.CompatibilityRule;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import service.storage.FileStorageService;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildService.class);

    private final Map<String, Build> builds = new ConcurrentHashMap<>();
    private final List<CompatibilityRule> compatibilityRules = new ArrayList<>();
    private final FileStorageService fileStorageService;

    public BuildService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        registerDefaultRules();
    }

    @PostConstruct
    void loadFromStorage() {
        List<Build> stored = fileStorageService.loadBuilds();
        if (!stored.isEmpty()) {
            stored.forEach(build -> builds.put(build.getId(), analyzeBuild(build)));
            log.info("Loaded {} builds from disk", stored.size());
        }
    }

    public Build save(Build build) {
        if (build == null) {
            throw new IllegalArgumentException("Build cannot be null");
        }
        if (build.getId() == null || build.getId().isBlank()) {
            build.setId(UUID.randomUUID().toString());
        }

        analyzeBuild(build);
        builds.put(build.getId(), build);
        persistBuilds();
        log.info("Build {} stored with total price {}", build.getId(), build.getTotalPrice());
        return build;
    }

    public Build get(String id) {
        return builds.get(id);
    }

    public List<Build> getAll() {
        return new ArrayList<>(builds.values());
    }

    public Build analyzeBuild(Build build) {
        ensureSelections(build);
        applyMetrics(build);
        return build;
    }

    public void registerRule(CompatibilityRule rule) {
        if (rule != null) {
            compatibilityRules.add(rule);
        }
    }

    public List<CompatibilityRule> getCompatibilityRules() {
        return List.copyOf(compatibilityRules);
    }

    private void registerDefaultRules() {
        compatibilityRules.add(CompatibilityRule.builder()
                .primaryCategory(ComponentCategory.CPU)
                .dependentCategory(ComponentCategory.MOTHERBOARD)
                .primaryAttributeKey("socket")
                .dependentAttributeKey("socket")
                .description("El socket del CPU no coincide con la placa madre")
                .build());

        compatibilityRules.add(CompatibilityRule.builder()
                .primaryCategory(ComponentCategory.RAM)
                .dependentCategory(ComponentCategory.MOTHERBOARD)
                .primaryAttributeKey("memoryType")
                .dependentAttributeKey("supportedMemory")
                .description("El tipo de memoria RAM no es soportado por la placa madre")
                .build());
    }

    private void applyMetrics(Build build) {
        Map<ComponentCategory, Component> selections = build.getSelections();

        List<String> alerts = compatibilityRules.stream()
                .map(rule -> rule.validate(selections))
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        build.setCompatibilityAlerts(alerts);

        double estimatedPower = estimatePower(selections);
        build.setEstimatedPowerDraw(estimatedPower > 0 ? estimatedPower : null);

        List<String> recommendations = generateRecommendations(build, selections, estimatedPower);
        build.setRecommendations(recommendations);
    }

    private double estimatePower(Map<ComponentCategory, Component> selections) {
        double consumption = selections.values().stream()
                .mapToDouble(component -> {
                    double tdp = readAttributeAsDouble(component, "tdp");
                    if (tdp == 0) {
                        return estimateDefaultTdp(component.getCategory());
                    }
                    return tdp;
                })
                .sum();

        return consumption > 0 ? Math.round(consumption * 1.2 * 10.0) / 10.0 : 0.0;
    }

    private double estimateDefaultTdp(ComponentCategory category) {
        return switch (category) {
            case CPU -> 65.0;
            case GPU -> 150.0;
            case RAM -> 10.0;
            case STORAGE -> 15.0;
            case MOTHERBOARD -> 50.0;
            case PSU, CASE -> 0.0;
        };
    }

    private List<String> generateRecommendations(Build build,
            Map<ComponentCategory, Component> selections,
            double estimatedPower) {
        List<String> recommendations = new ArrayList<>();
        Set<ComponentCategory> missing = EnumSet.noneOf(ComponentCategory.class);

        for (ComponentCategory category : ComponentCategory.values()) {
            if (!selections.containsKey(category)) {
                missing.add(category);
            }
        }

        if (!missing.isEmpty()) {
            recommendations.add("Faltan componentes: " + missing.stream()
                    .map(ComponentCategory::getDisplayName)
                    .collect(Collectors.joining(", ")));
        }

        double totalPrice = build.getTotalPrice();
        if (build.getBudget() != null) {
            if (totalPrice > build.getBudget()) {
                recommendations.add(String.format("El build supera el presupuesto por %.2f",
                        totalPrice - build.getBudget()));
            } else if (build.getBudget() - totalPrice > 100) {
                recommendations.add("Aún queda presupuesto para mejorar componentes.");
            }
        }

        Component psu = selections.get(ComponentCategory.PSU);
        double wattage = readAttributeAsDouble(psu, "wattage");
        if (psu != null && wattage > 0 && estimatedPower > wattage) {
            recommendations.add(String.format("El PSU brinda %.0fW pero la build requiere ~%.0fW",
                    wattage, estimatedPower));
        }

        return recommendations;
    }

    private double readAttributeAsDouble(Component component, String attributeKey) {
        if (component == null || attributeKey == null) {
            return 0;
        }
        return component.getAttribute(attributeKey)
                .map(value -> {
                    try {
                        String numeric = value.replaceAll("[^0-9.]", "");
                        return Double.parseDouble(numeric);
                    } catch (NumberFormatException ex) {
                        log.debug("Unable to parse attribute {} value {} for component {}", attributeKey, value,
                                component.getId());
                        return 0.0;
                    }
                })
                .orElse(0.0);
    }

    private void ensureSelections(Build build) {
        if (build.getSelections() == null) {
            build.setSelections(new EnumMap<>(ComponentCategory.class));
        }
        if (build.getCompatibilityAlerts() == null) {
            build.setCompatibilityAlerts(new ArrayList<>());
        }
        if (build.getRecommendations() == null) {
            build.setRecommendations(new ArrayList<>());
        }
    }

    private void persistBuilds() {
        fileStorageService.saveBuilds(builds.values());
    }
}
