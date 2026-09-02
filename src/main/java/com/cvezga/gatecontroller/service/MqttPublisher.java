package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Config;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

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

    private final ConfigService configService;

    private final Set<String> commandControlSet =  ConcurrentHashMap.newKeySet();

    public MqttPublisher(ConfigService configService) {
        this.configService = configService;
    }

    public String publishNotification() {

        String result;

        MqttAsyncClient client = null;

        try {
            
            Optional<Config> optionalConfig = configService.find();

            if (optionalConfig.isEmpty()) {
                throw new RuntimeException("No config found");
            }

            Config config = optionalConfig.get();

            client = getClient(config);

            String command = "OPEN-"+ UUID.randomUUID().toString();

            commandControlSet.add(command);

            MqttMessage message = getMessage(config,command);

            client.publish(config.getMqttTopic(), message).waitForCompletion(5000);

            result = "Command not confirmed!";


            log.info("Message published:");
            log.info("Topic   : " + config.getMqttTopic());
            log.info("Payload : " + config.getMqttPayload());

            boolean isCommandConfirmed = false;
            long start = System.currentTimeMillis();
            while(System.currentTimeMillis() - start < 5000) {
                if(!commandControlSet.contains(command)) {
                    isCommandConfirmed = true;
                    result = "Command confirmed.";
                    break;
                }
            }



        } catch (Exception e) {
            log.error("Error sending message to mqtt", e);
            result = "Error sending command";
        } finally {
            close(client);
        }

        return result;

    }
    public boolean publishOpenCommand() {

        boolean result = false;

        MqttAsyncClient client = null;

        try {

            Optional<Config> optionalConfig = configService.find();

            if (optionalConfig.isEmpty()) {
                throw new RuntimeException("No config found");
            }

            Config config = optionalConfig.get();

            client = getClient(config);

            MqttMessage message = getMessage(config, config.getMqttPayload());

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

    private MqttMessage getMessage(Config config, String textMessage){

        MqttProperties properties = new MqttProperties();
        properties.setMessageExpiryInterval(5L);

        MqttMessage message = new MqttMessage(textMessage.getBytes(StandardCharsets.UTF_8));
        message.setQos(config.getMqttMessageQos());      // Deliver at least once
        message.setRetained(false);
        message.setProperties(properties);

        return message;
    }
    private MqttAsyncClient getClient(Config config) throws MqttException {

        MqttAsyncClient client = new MqttAsyncClient(config.getMqttBroker(), config.getMqttClientId());

        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setUserName(config.getMqttUser());
        options.setPassword(config.getMqttPassword().getBytes(StandardCharsets.UTF_8));
        options.setConnectionTimeout(config.getMqttConnectionTimeout());
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);

        setCallbackTopic(config, client);

        client.connect(options).waitForCompletion(5000);

        // Subscribe
        client.subscribe(config.getMqttConfirmationTopic(), 0);

        System.out.println("Subscribed to: " + config.getMqttConfirmationTopic());

        return client;

    }

    private void setCallbackTopic(Config config, MqttAsyncClient client) {
        // Called whenever a message arrives
        client.setCallback(new MqttCallback() {

            @Override
            public void disconnected(MqttDisconnectResponse disconnectResponse) {
                System.err.println("MQTT disconnected: " + disconnectResponse.getReasonString());

            }

            @Override
            public void mqttErrorOccurred(MqttException exception) {
                System.err.println("MQTT connection lost");
                exception.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload =
                        new String(message.getPayload(), StandardCharsets.UTF_8);

                System.out.println("Message received:");
                System.out.println("Topic: " + topic);
                System.out.println("QoS: " + message.getQos());
                System.out.println("Payload: " + payload);

                commandControlSet.remove(payload);
            }

            @Override
            public void deliveryComplete(IMqttToken token) {
                System.out.println("Delivery complete: "+token.isComplete());
            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("Connected to " + serverURI);
            }

            @Override
            public void authPacketArrived(int reasonCode, MqttProperties properties) {
                System.out.println("Auth packet arrived. reasonCode: "+reasonCode+", properties "+properties);
            }


        });


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
