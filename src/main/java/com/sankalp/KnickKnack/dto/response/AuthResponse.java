package com.sankalp.KnickKnack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse { //After a successful login, the frontend needs the tokens to stay authenticated and basic user info to display in the header (e.g., "Welcome, Sankalp").
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;//Auto-Refresh
    //Minimal User Info For frontend
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo{
        private String id;
        private String name;
        private String email;
        private Integer trustScore;
    }
}
