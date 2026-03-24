package org.acme.adapters.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.acme.domain.User;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "external_id", unique = true, nullable = false)
    public String externalId;

    @Column(name = "username")
    public String username;

    @Column(name = "email")
    public String email;

    @Column(name = "created_at")
    public OffsetDateTime createdAt;

    public User toDomain() {
        return new User(id, externalId, username, email, createdAt);
    }

    public static UserEntity fromDomain(User u) {
        UserEntity e = new UserEntity();
        e.id = u.id;
        e.externalId = u.externalId;
        e.username = u.username;
        e.email = u.email;
        e.createdAt = u.createdAt;
        return e;
    }
}
