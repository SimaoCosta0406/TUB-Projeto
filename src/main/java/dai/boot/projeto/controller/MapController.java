package dai.boot.projeto.controller;

import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.repository.StopRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MapController {

    private final StopRepository stopRepository;

    public MapController(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    @GetMapping("/api/stops")
    public List<Stop> getStops() {
        return stopRepository.findAll();
    }
}