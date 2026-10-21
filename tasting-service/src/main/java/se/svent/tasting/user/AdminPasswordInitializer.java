package se.svent.tasting.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// runs once every time the app starts
// gives the admin account a password if it does not have one yet
@Component
public class AdminPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    // the two values come from application.yaml, which reads the password from the environment
    public AdminPasswordInitializer(AppUserRepository users,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${app.admin.username}") String adminUsername,
                                    @Value("${app.admin.password}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppUser admin = users.findByUsername(adminUsername).orElse(null);
        if (admin == null) {
            log.warn("admin user {} not found", adminUsername);
            return;
        }

        // already has a password, never overwrite it
        if (admin.getPasswordHash() != null) {
            return;
        }

        if (adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD is not set, the admin cannot log in yet");
            return;
        }

        admin.changePasswordHash(passwordEncoder.encode(adminPassword));
        log.info("password set for admin {}", adminUsername);
    }
}
