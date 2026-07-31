package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.service.ConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Provides administrator-only pages for viewing, saving, and deleting the
 * singleton application configuration.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public String page(Model model) {
        var existingConfig = configService.find();

        model.addAttribute("pageTitle", "Configuration");
        model.addAttribute("applicationName", "Gate Controller");
        model.addAttribute("config", existingConfig.orElseGet(Config::new));
        model.addAttribute("configExists", existingConfig.isPresent());

        return "config";
    }

    @PostMapping("/config")
    public String save(
            @ModelAttribute Config config,
            RedirectAttributes redirectAttributes) {
        configService.save(config);
        redirectAttributes.addFlashAttribute(
                "message",
                "Configuration saved successfully"
        );
        return "redirect:/config";
    }

    @PostMapping("/config/delete")
    public String delete(RedirectAttributes redirectAttributes) {
        configService.delete();
        redirectAttributes.addFlashAttribute(
                "message",
                "Configuration deleted successfully"
        );
        return "redirect:/config";
    }
}
