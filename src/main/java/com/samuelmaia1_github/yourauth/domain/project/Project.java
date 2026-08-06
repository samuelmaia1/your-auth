package com.samuelmaia1_github.yourauth.domain.project;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Project {
    private String id;
    private String name;
    private String description;
    private String ownerAccountId;
    private ProjectStatus status;
    private ProjectEnvironment environment;
    private String tokenAudience;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
