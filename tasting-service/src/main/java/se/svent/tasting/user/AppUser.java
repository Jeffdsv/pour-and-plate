package se.svent.tasting.user;

import jakarta.persistence.*;
import se.svent.tasting.common.BaseEntity;

// one person who can log in, me as admin or a friend
// called AppUser because User is a reserved word in postgres
@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    // only the hash is stored, never the real password
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    protected AppUser() {
    }

    public AppUser(String username, String displayName, String passwordHash, Role role) {
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // takes an already hashed password, this class never sees the real one
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
}
