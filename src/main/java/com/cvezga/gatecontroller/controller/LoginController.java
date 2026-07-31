package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.model.LoginForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Renders the login page and translates authentication or logout query
 * parameters into user-facing status messages.
 */
@Controller
public class LoginController {

    @GetMapping("/")
    public String home(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        model.addAttribute("pageTitle", "Gate Controller");
        model.addAttribute("applicationName", "Gate Controller");
        model.addAttribute("username", "");
        model.addAttribute("loginForm", new LoginForm());

        if (error != null) {
            model.addAttribute("message", "Invalid username or password.");
        } else if (logout != null) {
            model.addAttribute("message", "You have been signed out.");
        }

        return "login";
    }

}
