package edu.uclm.esi.fakeaccountsbe.services;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.CredencialesRegistro;
import edu.uclm.esi.fakeaccountsbe.model.User;

@Service
public class UserService {
        
    @Autowired
    private UserDao userDao;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public Map<String, String> registrar1(HttpServletRequest req, HttpServletResponse res, CredencialesRegistro cr) {
        cr.comprobar();
        
        // Check if the email is already in the database
        if (userDao.findById(cr.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ya existe un usuario con ese correo electrónico");
        }
    
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
    
        // Save user using UserService
        save(user);
    
        // Return token in JSON format
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    public Map<String, String> login1(HttpServletResponse response, User user) {
        user = find(user.getEmail(), user.getPwd());

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
        save(user);

        // Return token in JSON format
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", user.getToken());
        return responseBody;
    }

    public Map<String, String> checkCookie(HttpServletRequest request) {
        String userId = findCookie(request, "userId");
        if (userId != null) {
            User user = findByCookie(userId);
            if (user != null) {
                Map<String, String> response = new HashMap<>();
                response.put("token", user.getToken());
                return response;
            } else {
                System.out.println("User not found for cookie: " + userId);
            }
        }
        return null;
    }

    public Map<String, Boolean> getHasPaid(String token) {
        User user = findByToken(token);
        if (user == null || !token.equals(user.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o no corresponde");
        }
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPaid", user.isHasPaid());
        return response;
    }

    public Map<String, String> sendRecoveryEmail(String email) {
        User user = findByEmail(email);

        // Log the email address
        System.out.println("Sending recovery email to: " + email);

        // Generate a token for the user
        String tokenPasswordReset = UUID.randomUUID().toString();
        user.setTokenPasswordReset(tokenPasswordReset);
        save(user);

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
                return responseBody;
            } else {
                System.out.println("Failed to send recovery email to: " + email);
                responseBody.put("message", "Failed to send recovery email");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send recovery email");
            }
        } catch (IOException e) {
            System.out.println("Exception occurred while sending recovery email to: " + email);
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send recovery email", e);
        }
    }

    public Map<String, String> resetPassword(String tokenPasswordReset, String newPassword) {
        User user = findByTokenPasswordReset(tokenPasswordReset);

        user.setPwd(newPassword);
        user.setTokenPasswordReset(null); // Invalidate the reset token
        save(user);

        System.out.println("Password reset successful for user: " + user.getEmail());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Password reset successful");
        return responseBody;
    }

    public Map<String, String> sendConfirmationEmail(String email) {
        User user = findByEmail(email);

        System.out.println("Sending confirmation email to: " + email);

        // Use the existing token
        String confirmationToken = user.getToken();

        // Create the email content
        String confirmationLink = "http://localhost:4200/confirmEmail?token=" + confirmationToken;
        String emailContent = "<html>"
                + "<body>"
                + "<h1>Email Confirmation</h1>"
                + "<p>Hi " + user.getEmail() + ",</p>"
                + "<p>Please click the link below to confirm your email address:</p>"
                + "<p><a href=\"" + confirmationLink + "\">Confirm Email</a></p>"
                + "<p>If you did not request this, please ignore this email.</p>"
                + "<p>Thank you,</p>"
                + "<p>Felipe y Alonso</p>"
                + "</body>"
                + "</html>";

        // Send the email using SendGrid API
        try {
            Email from = new Email("felipeatle@hotmail.com", "Felipe Alcázar y Alonso Crespo");
            String subject = "Email Confirmation";
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
                System.out.println("Confirmation email sent successfully to: " + email);
                responseBody.put("message", "Confirmation email sent successfully");
                return responseBody;
            } else {
                System.out.println("Failed to send confirmation email to: " + email);
                responseBody.put("message", "Failed to send confirmation email");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send confirmation email");
            }
        } catch (IOException e) {
            System.out.println("Exception occurred while sending confirmation email to: " + email);
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send confirmation email", e);
        }
    }

    public Map<String, String> confirmEmail(String confirmationToken) {
        User user = findByToken(confirmationToken);

        user.setConfirmed(true);
        save(user);

        System.out.println("Email confirmation successful for user: " + user.getEmail());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Email confirmation successful");
        return responseBody;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the session or token on the server
        String userId = findCookie(request, "userId");
        if (userId != null) {
            User user = findByCookie(userId);
            if (user != null) {
                user.setToken(null);
                user.setCookie(null);
                user.setTokenCreationTime(null);
                save(user);
            }
        }

        // Remove the cookie
        Cookie cookie = new Cookie("userId", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
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

    public void registrar(String ip, User user) {
        if(this.userDao.findById(user.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ya existe un usuario con ese correo electrónico");
        
        user.setIp(ip);
        user.setTokenCreationTime(Instant.now()); // Set token creation time
        this.userDao.save(user);
    }

    public void login(User tryingUser) {
        this.find(tryingUser.getEmail(), tryingUser.getPwd());
    }

    public void clearAll() {
        this.userDao.deleteAll();
    }

    public Iterable<User> getAllUsers() {
        return this.userDao.findAll();
    }

    public User find(String email, String pwd) {
        Optional<User> optUser = this.userDao.findById(email);
        
        if (!optUser.isPresent())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El email proporcionado no se encuentra registrado.");
        
        User user = optUser.get();
        if (!user.getPwd().equals(pwd))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Las credenciales proporcionadas son incorrectas.");

        return user;
    }

    public User findByToken(String token) {
        Optional<User> userOptional = this.userDao.findByToken(token);
        if (!userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido");
        }
        return userOptional.get();
    }

    public User findByEmail(String email) {
        Optional<User> userOptional = userDao.findById(email);
        if (!userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return userOptional.get();
    }

    public User findByCookie(String cookie) {
        User user = userDao.findByCookie(cookie);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cookie inválido");
        }
        return user;
    }

    public User findByTokenPasswordReset(String tokenPasswordReset) {
        Optional<User> userOptional = userDao.findByTokenPasswordReset(tokenPasswordReset);
        if (!userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token de restablecimiento de contraseña inválido");
        }
        return userOptional.get();
    }

    public void save(User user) {
        userDao.save(user);
    }

    public void delete(String email) {
        this.userDao.deleteById(email);
    }

    public synchronized void clearOld() {
        // Implement logic to clear old users if needed
    }

    public void updateTokenCreationTime(User user) {
        user.setTokenCreationTime(Instant.now());
        userDao.save(user);
    }
}