package se.svent.tasting.place;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

// one food or drink place in sweden, imported from overture maps
// read only: the app never changes these rows, only the import script does
@Entity
@Immutable
@Table(name = "place")
public class Place {

    // the overture id, not a number postgres creates
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private String address;
    private String city;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    private String website;
    private String phone;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    // empty constructor jpa needs to load rows
    protected Place() {
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public String getWebsite() { return website; }
    public String getPhone() { return phone; }
    public Instant getImportedAt() { return importedAt; }
}
