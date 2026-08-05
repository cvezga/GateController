package com.cvezga.gatecontroller.entity;

import jakarta.persistence.*;

/**
 * Persistent singleton containing MQTT connection details and the gate's
 * geographic access constraints.
 *
 * <p>The application enforces the single-record invariant through
 * {@code ConfigService}.</p>
 */
@Entity
@Table(name = "config")
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mqtt_broker", length = 100, nullable = false)
    private String mqttBroker; // "tcp://45.77.206.86:1883"
    @Column(name = "mqtt_user", nullable = false)
    private String mqttUser; //: ${mqtt_user}
    @Column(name = "mqtt_password", nullable = false)
    private String mqttPassword; //  ${mqtt_pass}
    @Column(name = "mqtt_clientId", nullable = false)
    private String mqttClientId; //"java-publisher"
    @Column(name = "mqtt_connectionTimeout", nullable = false)
    private int mqttConnectionTimeout; // 10
    @Column(name = "mqtt_messageQos", nullable = false)
    private int mqttMessageQos; // 1
    @Column(name = "mqtt_topic", nullable = false)
    private String mqttTopic; // "garage/gate/command"
    @Column(name = "mqtt_notification_topic", nullable = false)
    private String mqttNotificationTopic; // "garage/gate/notification"
    @Column(name = "mqtt_payload", nullable = false)
    private String mqttPayload; // "OPEN"
    @Column(name = "gate_longitude", nullable = false)
    private double gateLongitude; // -84.083306
    @Column(name = "gate_latitude", nullable = false)
    private double gateLatitude; //10.074889
    @Column(name = "gate_max_distance_meters", nullable = false)
    private int gateMaxDistanceMeters; // 20

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMqttBroker() {
        return mqttBroker;
    }

    public void setMqttBroker(String mqttBroker) {
        this.mqttBroker = mqttBroker;
    }

    public String getMqttUser() {
        return mqttUser;
    }

    public void setMqttUser(String mqttUser) {
        this.mqttUser = mqttUser;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public void setMqttPassword(String mqttPassword) {
        this.mqttPassword = mqttPassword;
    }

    public String getMqttClientId() {
        return mqttClientId;
    }

    public void setMqttClientId(String mqttClientId) {
        this.mqttClientId = mqttClientId;
    }

    public int getMqttConnectionTimeout() {
        return mqttConnectionTimeout;
    }

    public void setMqttConnectionTimeout(int mqttConnectionTimeout) {
        this.mqttConnectionTimeout = mqttConnectionTimeout;
    }

    public int getMqttMessageQos() {
        return mqttMessageQos;
    }

    public void setMqttMessageQos(int mqttMessageQos) {
        this.mqttMessageQos = mqttMessageQos;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }

    public String getMqttNotificationTopic() {
        return mqttNotificationTopic;
    }

    public void setMqttNotificationTopic(String mqttNotificationTopic) {
        this.mqttNotificationTopic = mqttNotificationTopic;
    }

    public String getMqttPayload() {
        return mqttPayload;
    }

    public void setMqttPayload(String mqttPayload) {
        this.mqttPayload = mqttPayload;
    }

    public double getGateLongitude() {
        return gateLongitude;
    }

    public void setGateLongitude(double gateLongitude) {
        this.gateLongitude = gateLongitude;
    }

    public double getGateLatitude() {
        return gateLatitude;
    }

    public void setGateLatitude(double gateLatitude) {
        this.gateLatitude = gateLatitude;
    }

    public int getGateMaxDistanceMeters() {
        return gateMaxDistanceMeters;
    }

    public void setGateMaxDistanceMeters(int gateMaxDistanceMeters) {
        this.gateMaxDistanceMeters = gateMaxDistanceMeters;
    }

}
