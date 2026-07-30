package com.samuelmaia1_github.yourauth.domain.user;

import com.samuelmaia1_github.yourauth.infra.interfaces.IPasswordEncoder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserPolicy policy;
    private final IPasswordEncoder encoder;

    @Transactional
    public User create(User user) {
        policy.ensureCanCreate(user);

        user.updatePassword(encoder.encode(user.getPassword()));

        return repository.save(user);
    }
}
