package com.samuelmaia1_github.yourauth.domain.invite;

import com.samuelmaia1_github.yourauth.domain.projectmember.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invite {
    private String id;
    private String senderAccountId;
    private String senderAccountEmail;
    private String recipientAccountId;
    private String recipientAccountEmail;
    private ProjectMemberRole role;
    private InviteStatus status;
    private LocalDateTime sentAt;
    private String projectId;
    private String projectName;
    private String projectDescription;

    public void accept() {
        this.status = InviteStatus.ACCEPTED;
    }

    public void refuse() {
        this.status = InviteStatus.REFUSED;
    }
}
