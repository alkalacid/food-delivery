package com.fooddelivery.notification.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.mail")
@Getter
@Setter
@Validated
public class EmailProperties {

    @NotBlank(message = "Email 'from' address must not be blank")
    @Email(message = "Invalid email format")
    private String from;

    @NotBlank(message = "Email 'from' name must not be blank")
    private String fromName;
}

