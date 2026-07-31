package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Config;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Publishes the configured gate command to an MQTT broker.
 *
 * <p>A client is created for each publish request and is disconnected and
 * closed afterward. Missing configuration or connection/publish failures are
 * reported to callers as {@code false}.</p>
 */
@Slf4j
@Service
public class MqttPublisher {

//    @Value("${mqtt.broker}")
//    private String broker;
//    @Value("${mqtt.user}")
//    private String user;
//    @Value("${mqtt.password}")
//    private String password;
//    @Value("${mqtt.clientId}")
//    private String clientId;
//    @Value("${mqtt.connectionTimeout}")
//    private int connectionTimeout;
//    @Value("${mqtt.messageQos}")
//    private int messageQos;
//    @Value("${mqtt.topic}")
//    private String topic;
//    @Value("${mqtt.payload}")
//    private String payload;

    private final ConfigService configService;

    public MqttPublisher(ConfigService configService) {
        this.configService = configService;
    }

    public boolean publish() {

        boolean result = false;

        MqttAsyncClient client = null;

        try {

            Optional<Config> optionalConfig = configService.find();

            if (optionalConfig.isEmpty()) {
                return false;
            }

            Config config = optionalConfig.get();


            client = new MqttAsyncClient(config.getMqttBroker(), config.getMqttClientId());

            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setUserName(config.getMqttUser());
            options.setPassword(config.getMqttPassword().getBytes(StandardCharsets.UTF_8));
            options.setConnectionTimeout(config.getMqttConnectionTimeout());
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);

            client.connect(options).waitForCompletion(5000);

            MqttMessage message = new MqttMessage(config.getMqttPayload().getBytes(StandardCharsets.UTF_8));
            message.setQos(config.getMqttMessageQos());      // Deliver at least once
            message.setRetained(false);

            MqttProperties properties = new MqttProperties();
            properties.setMessageExpiryInterval(5L);
            message.setProperties(properties);

            client.publish(config.getMqttTopic(), message).waitForCompletion(5000);

            log.info("Message published:");
            log.info("Topic   : " + config.getMqttTopic());
            log.info("Payload : " + config.getMqttPayload());

            result = true;

        } catch (Exception e) {
            log.error("Error sending message to mqtt", e);
        } finally {
            close(client);
        }

        return result;
    }

    private void close(MqttAsyncClient client) {
        if (client != null) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                log.error("Error disconnecting MQTT client", e);
                throw new RuntimeException(e);
            }
            try {
                client.close();
            } catch (MqttException e) {
                log.error("Error closing MQTT client", e);
                throw new RuntimeException(e);
            }
        }
    }
}
