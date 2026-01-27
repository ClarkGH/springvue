package dev.springvue;

import dev.springvue.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class SpringvueApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringvueApplication.class, args);
    }
}
