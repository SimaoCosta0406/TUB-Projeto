package dai.boot.projeto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/mapa")
    public String mapa() {
        return "forward:/mapa.html";
    }

    @GetMapping("/veiculos")
    public String veiculos() {
        return "forward:/veiculos.html";
    }

    @GetMapping("/rotas")
    public String rotas() {
        return "forward:/rotas.html";
    }
}