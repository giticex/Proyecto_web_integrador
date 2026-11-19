package pe.edu.utp.techzone.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
