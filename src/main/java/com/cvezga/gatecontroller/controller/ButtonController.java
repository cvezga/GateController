package com.cvezga.gatecontroller.controller;


import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.entity.CommandValidation;
import com.cvezga.gatecontroller.service.CommandValidationService;
import com.cvezga.gatecontroller.service.ConfigService;
import com.cvezga.gatecontroller.service.EventService;
import com.cvezga.gatecontroller.service.MqttPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.http.HttpRequest;
import java.util.Optional;

/**
 * Serves the gate-control and event-history pages.
 *
 * <p>Gate commands are accepted only when the requesting device is within the
 * configured maximum distance from the gate. Every command attempt is recorded
 * before the MQTT message is published.</p>
 */
@Slf4j
@Controller
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ButtonController {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private final MqttPublisher mqttPublisher;
    private final ConfigService configService;

    private final EventService eventService;
    private final CommandValidationService commandValidationService;

    public ButtonController(
            MqttPublisher mqttPublisher,
            ConfigService configService,
            EventService eventService,
            CommandValidationService commandValidationService) {
        this.mqttPublisher = mqttPublisher;
        this.configService = configService;
        this.eventService = eventService;
        this.commandValidationService = commandValidationService;
    }

    @GetMapping("/button")
    public String buttonPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "button";
    }

    @GetMapping("/events")
    public String eventsPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("events", eventService.findAllNewestFirst());
        return "events";
    }

    @PostMapping("/button")
    public String sendCommand(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double accuracy,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {


        model.addAttribute("username", authentication.getName());

        String remoteAddr = request.getRemoteAddr();

        String logMessage = String.format("sendCommand from user=%s, ip=%s, latitude=%s, longitude=%s (accuracy=%s meters)",
                authentication.getName(),
                remoteAddr,
                latitude,
                longitude,
                accuracy);

        log.info("/button {}", logMessage);

        eventService.saveEvent(authentication.getName(), "button", logMessage);

        String message = "Unknown";

        code:
        {
            try {

                Optional<Config> optionalConfig = configService.find();
                if (optionalConfig.isEmpty()) {
                    message = "No config found";
                    break code;
                }
                Config config = optionalConfig.get();

                double distance = calculateDistanceMeters(
                        latitude,
                        longitude,
                        config.getGateLatitude(),
                        config.getGateLongitude()
                );

                if (!Double.isFinite(distance) || distance > config.getGateMaxDistanceMeters()) {
                    message = "ERROR: You must be within " + config.getGateMaxDistanceMeters() + " meters of the gate";
                    log.error(message);
                    model.addAttribute("message", message);
                    return "button";
                }

                String response = mqttPublisher.publishNotification();


                if (response.equals("Command confirmed.")) {
                    message = "Command published successfully";
                    log.info(message);
                    //CommandValidation commandValidation = commandValidationService.saveEvent(authentication.getName(), config.getMqttPayload());
                    //return "redirect:/command-confirmation/" + commandValidation.getId();
                } else {
                    log.error("ERROR: " + response);
                }

            } catch (Exception e) {
                log.error("Error /button sendCommand", e);
                message = "ERROR: Server error";
            }
        }

        model.addAttribute("message", message);

        return "button"; //(publish ? ResponseEntity.ok() : ResponseEntity.internalServerError()).build();
    }

    private static double calculateDistanceMeters(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude) {
        double latitudeDifference = Math.toRadians(endLatitude - startLatitude);
        double longitudeDifference = Math.toRadians(endLongitude - startLongitude);
        double startLatitudeRadians = Math.toRadians(startLatitude);
        double endLatitudeRadians = Math.toRadians(endLatitude);

        double haversine = Math.pow(Math.sin(latitudeDifference / 2), 2)
                + Math.cos(startLatitudeRadians)
                * Math.cos(endLatitudeRadians)
                * Math.pow(Math.sin(longitudeDifference / 2), 2);

        double angularDistance = 2 * Math.atan2(
                Math.sqrt(haversine),
                Math.sqrt(1 - haversine)
        );

        return EARTH_RADIUS_METERS * angularDistance;
    }
}
