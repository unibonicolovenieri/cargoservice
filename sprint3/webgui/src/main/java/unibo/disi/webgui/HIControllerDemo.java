package unibo.disi.webgui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Passo "HIControllerDemo" del prof:
// Annotazione @Controller + @GetMapping("/") + Model per Thymeleaf
@Controller
public class HIControllerDemo {

    @Value("${spring.application.name}")
    String appName;

    // Serve la pagina welcome.html (Thymeleaf template)
    // Accessibile su: http://sprint1:8085/
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("arg", appName);
        return "welcome";
    }

    // Serve direttamente index.html (WebSocket GUI)
    // Accessibile su: http://sprint1:8085/index.html
    // (quando il controller è attivo, Spring non usa automaticamente static/index.html)
}