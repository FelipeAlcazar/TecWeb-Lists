package edu.uclm.esi.fakeaccountsbe.http;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.CredencialesRegistro;
import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.Response;

@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/registrar1")
    public ResponseEntity<Map<String, String>> registrar1(HttpServletRequest req, HttpServletResponse res, @RequestBody CredencialesRegistro cr) {
        Map<String, String> response = userService.registrar1(req, res, cr);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/login1")
    public ResponseEntity<Map<String, String>> login1(HttpServletResponse response, @RequestBody User user) {
        try {
            Map<String, String> responseBody = userService.login1(response, user);
            return ResponseEntity.ok(responseBody);
        } catch (ResponseStatusException e) {
            // Return error response if user not found or credentials are incorrect
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        }
    }

    @GetMapping("/checkCookie")
    public ResponseEntity<Map<String, String>> checkCookie(HttpServletRequest request) {
        Map<String, String> response = userService.checkCookie(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/haspaid")
    public ResponseEntity<Map<String, Boolean>> getHasPaid(@RequestHeader("token") String token) {
        Map<String, Boolean> response = userService.getHasPaid(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sendRecoveryEmail")
    public ResponseEntity<Map<String, String>> sendRecoveryEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> responseBody = userService.sendRecoveryEmail(email);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String tokenPasswordReset = request.get("token");
        String newPassword = request.get("password");
        Map<String, String> responseBody = userService.resetPassword(tokenPasswordReset, newPassword);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/sendConfirmationEmail")
    public ResponseEntity<Map<String, String>> sendConfirmationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> responseBody = userService.sendConfirmationEmail(email);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/confirmEmail")
    public ResponseEntity<Map<String, String>> confirmEmail(@RequestBody Map<String, String> request) {
        String confirmationToken = request.get("token");
        Map<String, String> responseBody = userService.confirmEmail(confirmationToken);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}