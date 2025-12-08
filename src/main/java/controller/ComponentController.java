package controller;

import com.pcBuilder.backend.model.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.ComponentService;

import java.util.List;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @GetMapping
    public List<Component> getAll() {
        return componentService.getAll();
    }

    @GetMapping("/{type}")
    public List<Component> getByType(@PathVariable String type) {
        return componentService.getByType(type);
    }

    @GetMapping("/{type}/{id}")
    public Component getOne(@PathVariable String type, @PathVariable String id) {
        return componentService.getOne(type, id);
    }
}
