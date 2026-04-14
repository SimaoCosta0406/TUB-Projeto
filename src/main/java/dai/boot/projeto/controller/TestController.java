package dai.boot.projeto.controller;

import com.projeto.entities.Stop;
import com.projeto.entities.PanelMessage;
import com.projeto.repositories.StopRepository;
import com.projeto.repositories.PanelMessageRepository;
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

    @Autowired
    private PanelMessageRepository messageRepo;

    @GetMapping("/paragens")
    public List<Stop> testarParagens() {
        return stopRepo.findAll();
    }

    @GetMapping("/mensagens")
    public List<PanelMessage> testarMensagens() {
        return messageRepo.findAll();
    }
}
