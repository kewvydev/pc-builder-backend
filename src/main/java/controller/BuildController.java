package controller;

import com.pcBuilder.backend.model.build.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.BuildService;

import java.util.List;

@RestController
@RequestMapping("/api/build")
public class BuildController {

    @Autowired
    private BuildService buildService;

    @PostMapping
    public Build createBuild(@RequestBody Build build) {
        return buildService.save(build);
    }

    @GetMapping("/{id}")
    public Build getBuild(@PathVariable String id) {
        return buildService.get(id);
    }

    @GetMapping
    public List<Build> getAll() {
        return buildService.getAll();
    }
}
