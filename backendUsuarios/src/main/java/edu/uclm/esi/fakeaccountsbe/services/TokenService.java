package edu.uclm.esi.fakeaccountsbe.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.model.User;

@Service
public class TokenService {

    @Autowired
    private UserService userService;

    public String validar(String token) {
        User user = userService.findByToken(token);
        if (user == null || !token.equals(user.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no corresponde");
        }
        if (!isTokenValid(user.getToken(), user.getTokenCreationTime())) {
            // Refresh the token if it has expired
            refreshToken(user);
        }
        return user.getEmail();
    }

    private boolean isTokenValid(String token, Instant tokenCreationTime) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        // Check if the token is expired (valid for 24 hours)
        Instant now = Instant.now();
        return ChronoUnit.HOURS.between(tokenCreationTime, now) < 24;
    }

    private void refreshToken(User user) {
        String newToken = UUID.randomUUID().toString();
        user.setToken(newToken);
        user.setTokenCreationTime(Instant.now());
        userService.save(user);
    }
}