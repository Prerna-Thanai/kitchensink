package com.kitchensink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * The Class KitchensinkApplication.
 *
 * @author prerna
 */
@SpringBootApplication
@EnableMethodSecurity
public class KitchensinkApplication {

    /**
     * Application startup
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(KitchensinkApplication.class, args);
    }

}
