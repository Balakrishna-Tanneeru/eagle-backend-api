package com.eagle.drive.dto;

import com.eagle.drive.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDto user;

    public AuthResponse(String token, String refreshToken, User user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = new UserDto(user);
    }
}
