package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.CommandValidation;
import com.cvezga.gatecontroller.repository.CommandValidationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Records user activity and retrieves the audit history in reverse
 * chronological order.
 */
@Slf4j
@Service
public class CommandValidationService {

    private final Random r = new Random();

    private final CommandValidationRepository commandValidationRepository;

    private final MqttPublisher mqttPublisher;
    private final ZoneId timezone;

    public CommandValidationService(
            CommandValidationRepository commandValidationRepository, MqttPublisher mqttPublisher,
            @Value("${timezone:UTC}") String timezone) {
        this.commandValidationRepository = commandValidationRepository;
        this.mqttPublisher = mqttPublisher;
        this.timezone = ZoneId.of(timezone);
    }

    public CommandValidation saveEvent(String username, String command) {
        CommandValidation commandValidation = new CommandValidation();
        commandValidation.setId(UUID.randomUUID());
        commandValidation.setRandomInt(r.nextInt(100_000));
        commandValidation.setConfirmationNumber(r.nextInt(99) + 1);
        commandValidation.setDateTime(LocalDateTime.now(timezone));
        commandValidation.setUsername(username);
        commandValidation.setCommand(command);
        commandValidation.setStatus("request");
        return commandValidationRepository.save(commandValidation);
    }

    public Optional<CommandValidation> findById(UUID id) {
        return commandValidationRepository.findById(id);
    }

    public CommandValidation save(CommandValidation commandValidation) {
        return commandValidationRepository.save(commandValidation);
    }

    public boolean validateCommand(String user, UUID commandUUID, int randomNumber, int userConfirmationNumber) {

        boolean confirmed = false;

        CommandValidation commandValidation = findForAuthenticatedUser(commandUUID, user);

        if (commandValidation.getUsername().equals(user) &&
                commandValidation.getRandomInt() == randomNumber &&
                commandValidation.getConfirmationNumber() == userConfirmationNumber) {

            boolean commandPublished = mqttPublisher.publishOpenCommand();
            if (!commandPublished) {
                throw new RuntimeException("Publish command failed");
            }

            confirmed = true;

        } else {
            log.warn("Command validation not valid: commandUUID={}, user={}, randomInt={}, confirmationNumber={}",
                    commandUUID, user + "/" + commandValidation.getUsername(),
                    randomNumber + "/" + commandValidation.getRandomInt(),
                    userConfirmationNumber + "/" + commandValidation.getConfirmationNumber()
            );
        }

        return confirmed;
    }

    private CommandValidation findForAuthenticatedUser(UUID id, String username) {
        CommandValidation commandValidation = commandValidationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!username.equals(commandValidation.getUsername())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return commandValidation;
    }

}
