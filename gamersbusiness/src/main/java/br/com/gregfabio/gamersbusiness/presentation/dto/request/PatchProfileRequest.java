package br.com.gregfabio.gamersbusiness.presentation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSetter;

public final class PatchProfileRequest {
    @Size(max = 50)
    private String username;
    @Email
    @Size(max = 254)
    private String email;
    private String currentPassword;
    private String newPassword;
    private boolean usernameProvided;
    private boolean emailProvided;
    private boolean currentPasswordProvided;
    private boolean newPasswordProvided;

    @JsonSetter("username")
    public void setUsername(String username) {
        this.usernameProvided = true;
        this.username = username;
    }

    @JsonSetter("email")
    public void setEmail(String email) {
        this.emailProvided = true;
        this.email = email;
    }

    @JsonSetter("currentPassword")
    public void setCurrentPassword(String currentPassword) {
        this.currentPasswordProvided = true;
        this.currentPassword = currentPassword;
    }

    @JsonSetter("newPassword")
    public void setNewPassword(String newPassword) {
        this.newPasswordProvided = true;
        this.newPassword = newPassword;
    }

    @AssertTrue(message = "at least one non-null profile field is required")
    public boolean isChangeValid() {
        return (usernameProvided || emailProvided || currentPasswordProvided || newPasswordProvided)
                && (!usernameProvided || hasText(username))
                && (!emailProvided || hasText(email))
                && (currentPasswordProvided == newPasswordProvided)
                && (!currentPasswordProvided || (hasText(currentPassword) && hasText(newPassword)));
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String currentPassword() {
        return currentPassword;
    }

    public String newPassword() {
        return newPassword;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
