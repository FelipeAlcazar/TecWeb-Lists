package edu.uclm.esi.fakeaccountsbe.http;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

@RestController
@RequestMapping("tokens")
public class TokenController {

    @Autowired
    private UserService userService;

    @PutMapping("/validar")
    public String validar(@RequestBody String token) {
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