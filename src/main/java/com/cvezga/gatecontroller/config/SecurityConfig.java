package com.cvezga.gatecontroller.config;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.entity.User;
import com.cvezga.gatecontroller.repository.ConfigRepository;
import com.cvezga.gatecontroller.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

/**
 * Configures HTTP authorization, form login, password encoding, user lookup,
 * and initial application data.
 *
 * <p>On first startup, this configuration creates a default administrator and
 * persists the property-based MQTT and gate settings when their respective
 * database tables are empty.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${mqtt.broker}")
    private String broker;
    @Value("${mqtt.user}")
    private String user;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.clientId}")
    private String clientId;
    @Value("${mqtt.connectionTimeout}")
    private int connectionTimeout;
    @Value("${mqtt.messageQos}")
    private int messageQos;
    @Value("${mqtt.topic}")
    private String topic;
    @Value("${mqtt.payload}")
    private String payload;

    @Value("${gate.longitude}")
    private double longitude;
    @Value("${gate.latitude}")
    private double latitude;
    @Value("${gate.max_distance_metters:10}")
    private int maxDistanceMeters;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(authorize -> authorize
//                        //.requestMatchers("/*").permitAll());
//                        .requestMatchers("/", "/login", "/images/**", "/error").permitAll());
                http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users", "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/config", "/config/**").hasRole("ADMIN")
                        .requestMatchers("/button", "/button/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/events", "/events/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/", "/login", "/images/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .usernameParameter("user")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/button", true)
                        .failureUrl("/?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository,
                                                 PasswordEncoder passwordEncoder) {

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@localhost");
            admin.setRole("ADMIN");

            userRepository.save(admin);
        }


        return username -> userRepository.findByUsername(username.trim())
                .map(this::toUserDetails)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApplicationRunner initializeConfig(ConfigRepository configRepository) {
        return args -> {
            if (configRepository.count() != 0) {
                return;
            }

            Config config = new Config();
            config.setMqttBroker(broker);
            config.setMqttUser(user);
            config.setMqttPassword(password);
            config.setMqttClientId(clientId);
            config.setMqttConnectionTimeout(connectionTimeout);
            config.setMqttMessageQos(messageQos);
            config.setMqttTopic(topic);
            config.setMqttPayload(payload);
            config.setGateLongitude(longitude);
            config.setGateLatitude(latitude);
            config.setGateMaxDistanceMeters(maxDistanceMeters);

            configRepository.save(config);
        };
    }

    private org.springframework.security.core.userdetails.User toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(toAuthority(user))
        );
    }

    private String toAuthority(User user) {
        String role = user.getRole();

        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }

        role = role.trim().toUpperCase(Locale.ROOT);
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
