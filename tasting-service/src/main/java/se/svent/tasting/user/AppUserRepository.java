package se.svent.tasting.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // used when someone logs in
    Optional<AppUser> findByUsername(String username);

    // used to check that a username is free before signing up
    boolean existsByUsername(String username);
}
