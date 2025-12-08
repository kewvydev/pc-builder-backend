package service;

import com.pcBuilder.backend.model.component.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComponentService {
    private List<Component> components = new ArrayList<>();

    public List<Component> getAll() {
        return components;
    }

    public List<Component> getByType(String type) {
        return components.stream()
                .filter(c -> c.getCategory() != null && c.getCategory().getSlug().equalsIgnoreCase(type))
                .toList();
    }

    public Component getOne(String type, String id) {
        return components.stream()
                .filter(c -> c.getCategory() != null && c.getCategory().getSlug().equalsIgnoreCase(type)
                        && c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void setComponents(List<Component> newList) {
        this.components = newList;
    }
}
