package dev.springvue.config;

import dev.springvue.entity.User;
import dev.springvue.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataLoader {

    @Bean
    public ApplicationRunner loadDevUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPasswordHash(passwordEncoder.encode("password"));
                userRepository.save(user);
            }
        };
    }
}
