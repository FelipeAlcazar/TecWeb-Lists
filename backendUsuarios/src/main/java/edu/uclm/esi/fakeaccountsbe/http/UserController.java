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

    @Autowired
    private UserDao userDao;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @PostMapping("/registrar1")
    public ResponseEntity<Map<String, String>> registrar1(HttpServletRequest req, HttpServletResponse res, @RequestBody CredencialesRegistro cr) {
        cr.comprobar();
        User user = new User();
        user.setEmail(cr.getEmail());
        user.setPwd(cr.getPwd1());

        // Generate token
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        user.setTokenCreationTime(Instant.now());

        // Generate cookie value
        String cookieValue = UUID.randomUUID().toString();
        user.setCookie(cookieValue);

        // Set cookie
        Cookie cookie = new Cookie("userId", cookieValue);
        cookie.setMaxAge(3600 * 24 * 365); // 1 year
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");

        res.addCookie(cookie);

        // Save user
        userDao.save(user);

        // Return token in JSON format
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/login1")
    public ResponseEntity<Map<String, String>> login1(HttpServletResponse response, @RequestBody User user) {
        try {
            user = this.userService.find(user.getEmail(), user.getPwd());
        } catch (ResponseStatusException e) {
            // Return error response if user not found or credentials are incorrect
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        }

        String userId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("userId", userId);
        cookie.setMaxAge(3600 * 24 * 365); // 1 year
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);

        response.addCookie(cookie);

        user.setCookie(userId);
        user.setToken(UUID.randomUUID().toString());
        user.setTokenCreationTime(Instant.now());
        this.userDao.save(user);

        // Return token in JSON format
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", user.getToken());
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/checkCookie")
    public ResponseEntity<Map<String, String>> checkCookie(HttpServletRequest request) {
        String userId = this.findCookie(request, "userId");
        if (userId != null) {
            User user = this.userDao.findByCookie(userId);
            if (user != null) {
                Map<String, String> response = new HashMap<>();
                response.put("token", user.getToken());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("User not found for cookie: " + userId);
            }
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/haspaid")
    public ResponseEntity<Map<String, Boolean>> getHasPaid(@RequestHeader("token") String token) {
        User user = userService.findByToken(token);
        if (user == null || !token.equals(user.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no corresponde");
        }
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPaid", user.isHasPaid());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sendRecoveryEmail")
    public ResponseEntity<Map<String, String>> sendRecoveryEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userDao.findById(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Log the email address
        System.out.println("Sending recovery email to: " + email);

        // Generate a token for the user
        String tokenPasswordReset = UUID.randomUUID().toString();
        user.setTokenPasswordReset(tokenPasswordReset);
        userDao.save(user);

        // Create the email content
        String recoveryLink = "http://localhost:4200/resetPassword?token=" + tokenPasswordReset;
        String emailContent = "<html>"
                + "<body>"
                + "<h1>Password Recovery</h1>"
                + "<p>Hi " + user.getEmail() + ",</p>"
                + "<p>You have received this email because you requested to reset your password. Please click the link below to reset your password:</p>"
                + "<p><a href=\"" + recoveryLink + "\">Reset Password</a></p>"
                + "<p>If you did not request a password reset, please ignore this email.</p>"
                + "<p>Thank you,</p>"
                + "<p>Felipe y Alonso</p>"
                + "</body>"
                + "</html>";

        // Send the email using SendGrid API
        try {
            Email from = new Email("felipeatle@hotmail.com", "Felipe Alcázar y Alonso Crespo");
            String subject = "Password Recovery";
            Email to = new Email(email);
            Content content = new Content("text/html", emailContent);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request2 = new Request();
            request2.setMethod(Method.POST);
            request2.setEndpoint("mail/send");
            request2.setBody(mail.build());
            Response response = sg.api(request2);

            System.out.println("SendGrid response status code: " + response.getStatusCode());
            System.out.println("SendGrid response body: " + response.getBody());
            System.out.println("SendGrid response headers: " + response.getHeaders());

            Map<String, String> responseBody = new HashMap<>();
            if (response.getStatusCode() == 202) {
                System.out.println("Recovery email sent successfully to: " + email);
                responseBody.put("message", "Recovery email sent successfully");
                return ResponseEntity.ok(responseBody);
            } else {
                System.out.println("Failed to send recovery email to: " + email);
                responseBody.put("message", "Failed to send recovery email");
                return ResponseEntity.status(response.getStatusCode()).body(responseBody);
            }
        } catch (IOException e) {
            System.out.println("Exception occurred while sending recovery email to: " + email);
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send recovery email", e);
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String tokenPasswordReset = request.get("token");
        String newPassword = request.get("password");
        User user = userDao.findByTokenPasswordReset(tokenPasswordReset).orElseThrow(() -> {
            System.out.println("Invalid token: " + tokenPasswordReset);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token");
        });

        user.setPwd(newPassword);
        user.setTokenPasswordReset(null); // Invalidate the reset token
        userDao.save(user);

        System.out.println("Password reset successful for user: " + user.getEmail());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Password reset successful");
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the session or token on the server
        String userId = this.findCookie(request, "userId");
        if (userId != null) {
            User user = this.userDao.findByCookie(userId);
            if (user != null) {
                user.setToken(null);
                user.setCookie(null);
                user.setTokenCreationTime(null);
                this.userDao.save(user);
            }
        }

        // Remove the cookie
        Cookie cookie = new Cookie("userId", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    private String findCookie(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}