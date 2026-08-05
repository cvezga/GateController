package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.CommandValidation;
import com.cvezga.gatecontroller.service.CommandValidationService;
import com.cvezga.gatecontroller.service.GateNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.Map;

/**
 * Displays and processes the two-digit command confirmation form.
 */
@Controller
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class CommandConfirmationController {

    private final CommandValidationService commandValidationService;
    private final GateNotificationService gateNotificationService;

    public CommandConfirmationController(
            CommandValidationService commandValidationService,
            GateNotificationService gateNotificationService) {
        this.commandValidationService = commandValidationService;
        this.gateNotificationService = gateNotificationService;
    }

    @GetMapping("/command-confirmation/{id}")
    public String confirmationPage(@PathVariable UUID id, Authentication authentication, Model model) {
        CommandValidation commandValidation = findForAuthenticatedUser(id, authentication);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("commandValidation", commandValidation);
        gateNotificationService.consumeForUser(authentication.getName())
                .ifPresent(number -> model.addAttribute("notificationNumber", number));
        return "command-confirmation";
    }

    @ResponseBody
    @GetMapping("/command-confirmation/notification")
    public ResponseEntity<Map<String, String>> notification(Authentication authentication) {
        return gateNotificationService.consumeForUser(authentication.getName())
                .map(number -> ResponseEntity.ok(Map.of("number", number)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/command-confirmation")
    public String confirm(
            @RequestParam UUID id,
            @RequestParam int randomInt,
            @RequestParam int number,
            Authentication authentication,
            Model model) {

        boolean confirmed = commandValidationService.validateCommand(authentication.getName(), id, randomInt, number);

        model.addAttribute("message", confirmed
                ? "Command confirmed successfully"
                : "ERROR: Incorrect confirmation number");

        return "button";
    }


}
