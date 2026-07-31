package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.repository.ConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Manages the application's singleton configuration.
 *
 * <p>All operations validate that the database contains at most one
 * configuration record. Saving updates that record in place or creates it when
 * absent.</p>
 */
@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Config> find() {
        List<Config> configs = configRepository.findAll();
        ensureSingleton(configs);
        return configs.stream().findFirst();
    }

    @Transactional
    public synchronized Config save(Config submittedConfig) {
        List<Config> configs = configRepository.findAll();
        ensureSingleton(configs);

        Config config = configs.stream().findFirst().orElseGet(Config::new);
        copyValues(submittedConfig, config);
        return configRepository.save(config);
    }

    @Transactional
    public synchronized void delete() {
        List<Config> configs = configRepository.findAll();
        ensureSingleton(configs);
        configs.stream().findFirst().ifPresent(configRepository::delete);
    }

    private void ensureSingleton(List<Config> configs) {
        if (configs.size() > 1) {
            throw new IllegalStateException(
                    "The config table contains more than one record"
            );
        }
    }

    private void copyValues(Config source, Config target) {
        target.setMqttBroker(source.getMqttBroker());
        target.setMqttUser(source.getMqttUser());
        target.setMqttPassword(source.getMqttPassword());
        target.setMqttClientId(source.getMqttClientId());
        target.setMqttConnectionTimeout(source.getMqttConnectionTimeout());
        target.setMqttMessageQos(source.getMqttMessageQos());
        target.setMqttTopic(source.getMqttTopic());
        target.setMqttPayload(source.getMqttPayload());
        target.setGateLongitude(source.getGateLongitude());
        target.setGateLatitude(source.getGateLatitude());
        target.setGateMaxDistanceMeters(source.getGateMaxDistanceMeters());
    }
}
