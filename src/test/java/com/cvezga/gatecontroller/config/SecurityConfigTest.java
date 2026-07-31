package com.cvezga.gatecontroller.config;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.entity.User;
import com.cvezga.gatecontroller.repository.ConfigRepository;
import com.cvezga.gatecontroller.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for security-related beans and initial-data creation in
 * {@link SecurityConfig}.
 */
class SecurityConfigTest {

    @Test
    void passwordEncoderCreatesBcryptHashes() {
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

        String hash = encoder.encode("secret");

        assertThat(hash).isNotEqualTo("secret");
        assertThat(encoder.matches("secret", hash)).isTrue();
    }

    @Test
    void userDetailsServiceCreatesDefaultAdminWhenRepositoryIsEmpty() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.count()).thenReturn(0L);
        when(encoder.encode("admin")).thenReturn("encoded-admin");

        new SecurityConfig().userDetailsService(repository, encoder);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getPassword()).isEqualTo("encoded-admin");
        assertThat(admin.getEmail()).isEqualTo("admin@localhost");
        assertThat(admin.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void userDetailsServiceTrimsLookupAndNormalizesRoles() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.count()).thenReturn(1L);
        User user = user("alice", "encoded", " admin ");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = new SecurityConfig()
                .userDetailsService(repository, mock(PasswordEncoder.class))
                .loadUserByUsername(" alice ");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        verify(repository, never()).save(any());
    }

    @Test
    void userDetailsServiceDefaultsBlankRoleAndPreservesRolePrefix() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.count()).thenReturn(1L);
        User blankRole = user("blank", "one", " ");
        User prefixedRole = user("prefixed", "two", "role_user");
        when(repository.findByUsername("blank")).thenReturn(Optional.of(blankRole));
        when(repository.findByUsername("prefixed")).thenReturn(Optional.of(prefixedRole));
        var service = new SecurityConfig().userDetailsService(repository, mock(PasswordEncoder.class));

        assertThat(service.loadUserByUsername("blank").getAuthorities())
                .extracting("authority").containsExactly("ROLE_USER");
        assertThat(service.loadUserByUsername("prefixed").getAuthorities())
                .extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void userDetailsServiceThrowsWhenUserDoesNotExist() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.count()).thenReturn(1L);
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());
        var service = new SecurityConfig().userDetailsService(repository, mock(PasswordEncoder.class));

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: missing");
    }

    @Test
    void initializeConfigDoesNothingWhenConfigAlreadyExists() throws Exception {
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.count()).thenReturn(1L);

        new SecurityConfig().initializeConfig(repository).run(null);

        verify(repository, never()).save(any());
    }

    @Test
    void initializeConfigPersistsConfiguredDefaults() throws Exception {
        SecurityConfig security = new SecurityConfig();
        setField(security, "broker", "tcp://broker");
        setField(security, "user", "mqtt-user");
        setField(security, "password", "mqtt-password");
        setField(security, "clientId", "client");
        setField(security, "connectionTimeout", 15);
        setField(security, "messageQos", 2);
        setField(security, "topic", "gate/topic");
        setField(security, "payload", "OPEN");
        setField(security, "longitude", -84.1);
        setField(security, "latitude", 10.1);
        setField(security, "maxDistanceMeters", 20);
        ConfigRepository repository = mock(ConfigRepository.class);
        when(repository.count()).thenReturn(0L);

        security.initializeConfig(repository).run(null);

        ArgumentCaptor<Config> captor = ArgumentCaptor.forClass(Config.class);
        verify(repository).save(captor.capture());
        Config config = captor.getValue();
        assertThat(config.getMqttBroker()).isEqualTo("tcp://broker");
        assertThat(config.getMqttUser()).isEqualTo("mqtt-user");
        assertThat(config.getMqttPassword()).isEqualTo("mqtt-password");
        assertThat(config.getMqttClientId()).isEqualTo("client");
        assertThat(config.getMqttConnectionTimeout()).isEqualTo(15);
        assertThat(config.getMqttMessageQos()).isEqualTo(2);
        assertThat(config.getMqttTopic()).isEqualTo("gate/topic");
        assertThat(config.getMqttPayload()).isEqualTo("OPEN");
        assertThat(config.getGateLongitude()).isEqualTo(-84.1);
        assertThat(config.getGateLatitude()).isEqualTo(10.1);
        assertThat(config.getGateMaxDistanceMeters()).isEqualTo(20);
    }

    private User user(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    private void setField(SecurityConfig target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }
}
