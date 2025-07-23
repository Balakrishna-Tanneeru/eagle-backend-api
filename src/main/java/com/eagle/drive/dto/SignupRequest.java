package com.eagle.drive.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "RIDER|DRIVER", message = "Role must be RIDER or DRIVER")
    private String role;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    private Double latitude;

    private Double longitude;
}
