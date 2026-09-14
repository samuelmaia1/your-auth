package com.samuelmaia1_github.yourauth.presentation.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelmaia1_github.yourauth.presentation.dto.authconfig.AuthConfigDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.passwordconfig.PasswordConfigDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.project.UpdateProjectDTO;
import com.samuelmaia1_github.yourauth.presentation.dto.user.UpdateUserDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateDtoDeserializationTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldTrackProvidedProjectFields() throws Exception {
        UpdateProjectDTO dto = mapper.readValue("""
                {
                  "description": null
                }
                """, UpdateProjectDTO.class);

        assertThat(dto.nameProvided()).isFalse();
        assertThat(dto.descriptionProvided()).isTrue();
        assertThat(dto.description()).isNull();
    }

    @Test
    void shouldTrackProvidedUserFields() throws Exception {
        UpdateUserDTO dto = mapper.readValue("""
                {
                  "phone": null
                }
                """, UpdateUserDTO.class);

        assertThat(dto.emailProvided()).isFalse();
        assertThat(dto.phoneProvided()).isTrue();
        assertThat(dto.phone()).isNull();
    }

    @Test
    void shouldTrackProvidedAuthConfigFields() throws Exception {
        AuthConfigDTO dto = mapper.readValue("""
                {
                  "maxActiveSessions": null
                }
                """, AuthConfigDTO.class);

        assertThat(dto.sessionModeProvided()).isFalse();
        assertThat(dto.maxActiveSessionsProvided()).isTrue();
        assertThat(dto.maxActiveSessions()).isNull();
    }

    @Test
    void shouldTrackProvidedPasswordConfigFields() throws Exception {
        PasswordConfigDTO dto = mapper.readValue("""
                {
                  "numberRequired": false
                }
                """, PasswordConfigDTO.class);

        assertThat(dto.minSizeProvided()).isFalse();
        assertThat(dto.numberRequiredProvided()).isTrue();
        assertThat(dto.numberRequired()).isFalse();
    }
}
