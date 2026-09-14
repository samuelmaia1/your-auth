package com.samuelmaia1_github.yourauth.infra.repository.entity;

import com.samuelmaia1_github.yourauth.domain.shared.Address;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import com.samuelmaia1_github.yourauth.infra.persistence.CPFConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Table(name = "accounts")
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private String id;

    @Column
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep", column = @Column(name = "address_postal_code")),
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "address_neighborhood")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "address_state")),
            @AttributeOverride(name = "number", column = @Column(name = "address_number"))
    })
    private Address address;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Convert(converter = CPFConverter.class)
    @Column(unique = true, length = 11)
    private CPF CPF;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ddd", column = @Column(name = "phone_ddd")),
            @AttributeOverride(name = "number", column = @Column(name = "phone_number"))
    })
    private Phone phone;

    @Column
    private String password;
}
