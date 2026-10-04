package br.com.gregfabio.gamersbusiness.infrastructure.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import br.com.gregfabio.gamersbusiness.application.service.AuthService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {
    private final boolean enabled;
    private final String username;
    private final String email;
    private final String password;
    private final Environment environment;
    private final Validator validator;
    private final AuthService authService;

    public BootstrapAdminRunner(
            @Value("${BOOTSTRAP_ADMIN_ENABLED:false}") boolean enabled,
            @Value("${BOOTSTRAP_ADMIN_USERNAME:}") String username,
            @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String email,
            @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String password,
            Environment environment,
            Validator validator,
            AuthService authService) {
        this.enabled = enabled;
        this.username = username;
        this.email = email;
        this.password = password;
        this.environment = environment;
        this.validator = validator;
        this.authService = authService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (!environment.acceptsProfiles(Profiles.of("local"))) {
            throw new IllegalStateException("ADMIN bootstrap is supported only with the local profile");
        }

        BootstrapAdminCredentials credentials = new BootstrapAdminCredentials(username, email, password);
        Set<ConstraintViolation<BootstrapAdminCredentials>> violations = validator.validate(credentials);
        if (!violations.isEmpty()) {
            String invalidFields = violations.stream()
                    .map(violation -> violation.getPropertyPath().toString())
                    .collect(Collectors.toUnmodifiableSet())
                    .toString();
            throw new IllegalStateException("Invalid ADMIN bootstrap configuration: " + invalidFields);
        }
        try {
            authService.provisionAdmin(username, email, password);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("ADMIN bootstrap could not create or reuse the configured account", exception);
        }
    }
}
