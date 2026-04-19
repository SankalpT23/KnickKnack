package com.sankalp.KnickKnack.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email
    private String email;
    @NotBlank(message = "Password Required")
    @Size(min = 8)
    private String passwordHash;
    private String name;
    @Pattern(regexp = "\\\\d{10}|(?:\\\\d{3}-){2}\\\\d{4}|\\\\(\\\\d{3}\\\\)\\\\d{3}-?\\\\d{4}",
            message = "Invalid Phone Number Format")
    private String phone;
    private String campusId;
}
