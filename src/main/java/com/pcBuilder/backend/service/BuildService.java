package com.pcBuilder.backend.service;

import com.pcBuilder.backend.dto.BuildRequest;
import com.pcBuilder.backend.exception.ResourceNotFoundException;
import com.pcBuilder.backend.exception.ValidationException;
import com.pcBuilder.backend.model.build.Build;
import com.pcBuilder.backend.model.build.BuildComponentEntity;
import com.pcBuilder.backend.model.build.BuildEntity;
import com.pcBuilder.backend.model.compatibility.CompatibilityRule;
import com.pcBuilder.backend.model.component.Component;
import com.pcBuilder.backend.model.component.ComponentCategory;
import com.pcBuilder.backend.repository.BuildRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing PC builds using PostgreSQL database.
 */
@Service
@Transactional
public class BuildService {

    private static final Logger log = LoggerFactory.getLogger(BuildService.class);

    private final List<CompatibilityRule> compatibilityRules = new ArrayList<>();
    private final BuildRepository buildRepository;
    private final ComponentService componentService;

    public BuildService(BuildRepository buildRepository, ComponentService componentService) {
        this.buildRepository = buildRepository;
        this.componentService = componentService;
        registerDefaultRules();
    }

    /**
     * Save a build to the database.
     */
    public Build save(Build build) {
        if (build == null) {
            throw new IllegalArgumentException("Build cannot be null");
        }

        analyzeBuild(build);

        BuildEntity entity = BuildEntity.fromDomain(build);
        BuildEntity saved = buildRepository.save(entity);

        log.info("Build {} stored with total price {}", saved.getId(), saved.getTotalPrice());

        // Convert back to domain with components
        return entityToDomain(saved);
    }

    /**
     * Get a build by ID.
     */
    @Transactional(readOnly = true)
    public Build get(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return buildRepository.findById(uuid)
                    .map(this::entityToDomain)
                    .orElseThrow(() -> new ResourceNotFoundException("Build", id));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID format: " + id);
        }
    }

    /**
     * Get all builds.
     */
    @Transactional(readOnly = true)
    public List<Build> getAll() {
        return buildRepository.findAll().stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Delete a build by ID.
     */
    public void delete(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            if (!buildRepository.existsById(uuid)) {
                throw new ResourceNotFoundException("Build", id);
            }
            buildRepository.deleteById(uuid);
            log.info("Deleted build: {}", id);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID format: " + id);
        }
    }

    /**
     * Update an existing build.
     */
    public Build update(String id, Build build) {
        try {
            UUID uuid = UUID.fromString(id);
            if (!buildRepository.existsById(uuid)) {
                throw new ResourceNotFoundException("Build", id);
            }

            build.setId(id);
            analyzeBuild(build);

            BuildEntity entity = BuildEntity.fromDomain(build);
            BuildEntity updated = buildRepository.save(entity);

            log.info("Updated build: {}", id);
            return entityToDomain(updated);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID format: " + id);
        }
    }

    /**
     * Create a build from a BuildRequest.
     */
    public Build createFromRequest(BuildRequest request) {
        Build build = Build.builder()
                .name(request.getName())
                .budget(request.getBudget())
                .selectedComponents(new EnumMap<>(ComponentCategory.class))
                .build();

        // Resolve and add components
        request.getComponents().forEach(selection -> {
            ComponentCategory category = ComponentCategory.fromString(selection.getCategory());
            if (category != null) {
                Component component = componentService.getById(selection.getComponentId());
                build.getSelectedComponents().put(category, component);
            }
        });

        return save(build);
    }

    /**
     * Add or update a component in a build.
     */
    public Build updateComponent(String buildId, ComponentCategory category, String componentId) {
        Build build = get(buildId);
        Component component = componentService.getById(componentId);

        build.getSelectedComponents().put(category, component);
        return save(build);
    }

    /**
     * Remove a component from a build.
     */
    public Build removeComponent(String buildId, ComponentCategory category) {
        Build build = get(buildId);
        build.getSelectedComponents().remove(category);
        return save(build);
    }

    /**
     * Get recent builds ordered by creation date.
     */
    @Transactional(readOnly = true)
    public List<Build> getRecentBuilds() {
        return buildRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Search builds by name.
     */
    @Transactional(readOnly = true)
    public List<Build> searchByName(String searchText) {
        return buildRepository.searchByName(searchText).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    /**
     * Get total count of builds.
     */
    @Transactional(readOnly = true)
    public long count() {
        return buildRepository.count();
    }

    /**
     * Convert BuildEntity to Build domain object.
     */
    private Build entityToDomain(BuildEntity entity) {
        // Load all components referenced in the build
        Map<String, Component> componentMap = entity.getComponents().stream()
                .map(BuildComponentEntity::getComponentId)
                .filter(id -> id != null)
                .distinct()
                .map(id -> {
                    try {
                        return componentService.getById(id);
                    } catch (ResourceNotFoundException e) {
                        log.warn("Component not found: {}", id);
                        return null;
                    }
                })
                .filter(component -> component != null)
                .collect(Collectors.toMap(Component::getId, c -> c));

        return entity.toDomain(componentMap);
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
        build.setAlerts(alerts);

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
        if (build.getSelectedComponents() == null) {
            build.setSelectedComponents(new EnumMap<>(ComponentCategory.class));
        }
        if (build.getAlerts() == null) {
            build.setAlerts(new ArrayList<>());
        }
        if (build.getRecommendations() == null) {
            build.setRecommendations(new ArrayList<>());
        }
    }

}
