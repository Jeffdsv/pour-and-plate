package se.svent.tasting.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // used when someone logs in
    Optional<AppUser> findByUsername(String username);

    // used to check that a username is free before signing up
    boolean existsByUsername(String username);

    // every user plus how many places they visited and want to visit
    // left join so friends with zero reviews still show up
    @Query(value = """
            SELECT u.id AS "id",
                   u.username AS "username",
                   u.display_name AS "displayName",
                   u.role AS "role",
                   COUNT(r.id) FILTER (WHERE r.status = 'VISITED') AS "visitedCount",
                   COUNT(r.id) FILTER (WHERE r.status = 'WANT_TO_VISIT') AS "wantCount"
            FROM app_user u
            LEFT JOIN restaurant r ON r.user_id = u.id
            GROUP BY u.id
            ORDER BY u.display_name
            """, nativeQuery = true)
    List<UserSummary> summarizeUsers();
}
