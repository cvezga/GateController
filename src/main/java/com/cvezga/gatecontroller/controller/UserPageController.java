package com.cvezga.gatecontroller.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the administrator-only user-management page.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class UserPageController {

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("applicationName", "Gate Controller");
        return "users";
    }
}
