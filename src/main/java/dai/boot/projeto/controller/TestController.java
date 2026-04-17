package dai.boot.projeto.controller;

import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.repository.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/teste")
public class TestController {

    @Autowired
    private StopRepository stopRepo;

    @GetMapping("/paragens")
    public List<Stop> testarParagens() {
        return stopRepo.findAll();
    }
}