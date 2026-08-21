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

    @Column(nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep", column = @Column(name = "address_postal_code", nullable = false)),
            @AttributeOverride(name = "street", column = @Column(name = "address_street", nullable = false)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "address_neighborhood", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "address_city", nullable = false)),
            @AttributeOverride(name = "state", column = @Column(name = "address_state", nullable = false)),
            @AttributeOverride(name = "number", column = @Column(name = "address_number", nullable = false))
    })
    private Address address;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Convert(converter = CPFConverter.class)
    @Column(nullable = false, unique = true, length = 11)
    private CPF CPF;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ddd", column = @Column(name = "phone_ddd", nullable = false)),
            @AttributeOverride(name = "number", column = @Column(name = "phone_number", nullable = false))
    })
    private Phone phone;

    @Column(nullable = false)
    private String password;
}
