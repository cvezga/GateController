package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for singleton enforcement and value copying in
 * {@link ConfigService}.
 */
class ConfigServiceTest {

    private ConfigRepository repository;
    private ConfigService service;

    @BeforeEach
    void setUp() {
        repository = mock(ConfigRepository.class);
        service = new ConfigService(repository);
    }

    @Test
    void findReturnsEmptyOrTheOnlyConfiguration() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.find()).isEmpty();

        Config config = new Config();
        when(repository.findAll()).thenReturn(List.of(config));
        assertThat(service.find()).containsSame(config);
    }

    @Test
    void everyOperationRejectsMultipleConfigurations() {
        List<Config> configs = List.of(new Config(), new Config());
        when(repository.findAll()).thenReturn(configs);

        assertThatThrownBy(service::find).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.save(new Config())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(service::delete).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void saveCreatesConfigurationAndCopiesAllValues() {
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Config submitted = populatedConfig();

        Config saved = service.save(submitted);

        assertThat(saved).isNotSameAs(submitted);
        assertThat(saved.getMqttBroker()).isEqualTo("tcp://broker");
        assertThat(saved.getMqttUser()).isEqualTo("mqtt-user");
        assertThat(saved.getMqttPassword()).isEqualTo("secret");
        assertThat(saved.getMqttClientId()).isEqualTo("client");
        assertThat(saved.getMqttConnectionTimeout()).isEqualTo(12);
        assertThat(saved.getMqttMessageQos()).isEqualTo(2);
        assertThat(saved.getMqttTopic()).isEqualTo("gate/topic");
        assertThat(saved.getMqttPayload()).isEqualTo("OPEN");
        assertThat(saved.getGateLongitude()).isEqualTo(-84.1);
        assertThat(saved.getGateLatitude()).isEqualTo(10.1);
        assertThat(saved.getGateMaxDistanceMeters()).isEqualTo(25);
    }

    @Test
    void saveUpdatesExistingConfigurationWithoutReplacingItsIdentity() {
        Config existing = new Config();
        existing.setId(3L);
        when(repository.findAll()).thenReturn(List.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.save(populatedConfig())).isSameAs(existing);
        assertThat(existing.getId()).isEqualTo(3L);
        assertThat(existing.getMqttBroker()).isEqualTo("tcp://broker");
    }

    @Test
    void deleteDeletesOnlyExistingConfiguration() {
        Config existing = new Config();
        when(repository.findAll()).thenReturn(List.of(existing));
        service.delete();
        verify(repository).delete(existing);

        reset(repository);
        when(repository.findAll()).thenReturn(List.of());
        service.delete();
        verify(repository, never()).delete(any());
    }

    private Config populatedConfig() {
        Config config = new Config();
        config.setMqttBroker("tcp://broker");
        config.setMqttUser("mqtt-user");
        config.setMqttPassword("secret");
        config.setMqttClientId("client");
        config.setMqttConnectionTimeout(12);
        config.setMqttMessageQos(2);
        config.setMqttTopic("gate/topic");
        config.setMqttPayload("OPEN");
        config.setGateLongitude(-84.1);
        config.setGateLatitude(10.1);
        config.setGateMaxDistanceMeters(25);
        return config;
    }
}
