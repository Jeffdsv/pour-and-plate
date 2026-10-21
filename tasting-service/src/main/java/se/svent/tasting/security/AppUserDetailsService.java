package se.svent.tasting.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.svent.tasting.user.AppUser;
import se.svent.tasting.user.AppUserRepository;

// spring security calls this at login to load a user from my database
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // turns my AppUser into the format spring security understands
        // an account without a password is disabled, so nobody can log in to it
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .disabled(user.getPasswordHash() == null)
                .roles(user.getRole().name())
                .build();
    }
}