package com.samuelmaia1_github.yourauth.domain.account;

import com.samuelmaia1_github.yourauth.domain.shared.Address;
import com.samuelmaia1_github.yourauth.domain.shared.Phone;
import com.samuelmaia1_github.yourauth.domain.valueobjects.CPF;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Address address;
    private Phone phone;
    private CPF CPF;
    private String avatarUrl;

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateAvatarUrl(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            this.avatarUrl = avatarUrl;
        }
    }

    public boolean isProfileComplete() {
        return isPresent(name)
                && isPresent(lastName)
                && isPresent(password)
                && CPF != null
                && isAddressComplete()
                && isPhoneComplete();
    }

    private boolean isAddressComplete() {
        return address != null
                && isPresent(address.getCep())
                && isPresent(address.getStreet())
                && isPresent(address.getNeighborhood())
                && isPresent(address.getCity())
                && isPresent(address.getState())
                && isPresent(address.getNumber());
    }

    private boolean isPhoneComplete() {
        return phone != null
                && isPresent(phone.getDdd())
                && isPresent(phone.getNumber());
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
