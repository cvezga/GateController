package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Config;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Receives user-specific two-digit notifications from MQTT. */
@Slf4j
@Service
public class GateNotificationService {

    public static final String TOPIC = "gate_notification";
    private static final Pattern USER_PATTERN = Pattern.compile("\\\"user\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\\"number\\\"\\s*:\\s*(?:\\\"(\\d+)\\\"|(\\d+))");

    private final ConfigService configService;
    private final ConcurrentMap<String, String> pendingNumbers = new ConcurrentHashMap<>();
    private MqttAsyncClient client;

    public GateNotificationService(ConfigService configService) {
        this.configService = configService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        Optional<Config> optionalConfig = configService.find();
        if (optionalConfig.isEmpty()) {
            log.warn("Cannot subscribe to {}: MQTT configuration is missing", TOPIC);
            return;
        }

        try {
            Config config = optionalConfig.get();
            client = new MqttAsyncClient(config.getMqttBroker(), config.getMqttClientId() + "-notifications");

            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setUserName(config.getMqttUser());
            options.setPassword(config.getMqttPassword().getBytes(StandardCharsets.UTF_8));
            options.setConnectionTimeout(config.getMqttConnectionTimeout());
            options.setAutomaticReconnect(true);
            options.setCleanStart(false);

            client.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    if (TOPIC.equals(topic)) {
                        handlePayload(new String(message.getPayload(), StandardCharsets.UTF_8));
                    }
                }

                @Override
                public void disconnected(MqttDisconnectResponse disconnectResponse) {
                    log.warn("Disconnected from MQTT notification topic {}", TOPIC);
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    log.error("MQTT notification client error", exception);
                }

                @Override
                public void deliveryComplete(IMqttToken token) {
                    // This client only subscribes; it does not publish messages.
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    log.info("MQTT notification client connected to {}", serverURI);
                }

                @Override
                public void authPacketArrived(int reasonCode, MqttProperties properties) {
                    // No enhanced-authentication exchange is used.
                }
            });

            client.connect(options).waitForCompletion(5000);
            client.subscribe(TOPIC, config.getMqttMessageQos()).waitForCompletion(5000);
            log.info("Subscribed to MQTT topic {}", TOPIC);
        } catch (Exception exception) {
            log.error("Unable to subscribe to MQTT topic {}", TOPIC, exception);
            closeClient();
        }
    }

    void handlePayload(String payload) {
        try {
            Matcher userMatcher = USER_PATTERN.matcher(payload);
            Matcher numberMatcher = NUMBER_PATTERN.matcher(payload);
            if (!userMatcher.find() || !numberMatcher.find()) {
                throw new IllegalArgumentException("Required notification fields are missing");
            }
            String user = userMatcher.group(1).trim();
            String number = numberMatcher.group(1) != null ? numberMatcher.group(1) : numberMatcher.group(2);
            if (number.length() == 1) {
                number = "0" + number;
            }

            if (user.isEmpty() || !number.matches("\\d{2}")) {
                log.warn("Ignoring invalid notification payload on {}", TOPIC);
                return;
            }

            pendingNumbers.put(user, number);
            log.info("Received gate notification for user={}", user);
        } catch (Exception exception) {
            log.warn("Ignoring malformed notification payload on {}", TOPIC, exception);
        }
    }

    public Optional<String> consumeForUser(String username) {
        return Optional.ofNullable(pendingNumbers.remove(username));
    }

    @PreDestroy
    public void closeClient() {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(2000);
            }
            client.close();
        } catch (Exception exception) {
            log.warn("Unable to close MQTT notification client cleanly", exception);
        } finally {
            client = null;
        }
    }
}
