package edu.uclm.esi.fakeaccountsbe.http;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
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
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.sendgrid.Response; // Add this import

@RestController
@RequestMapping("users")
@CrossOrigin(origins = { "http://localhost:4200" }, allowCredentials = "true")
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
        this.userDao.save(user);

        // Return token in JSON format
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("token", user.getToken());
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/sendRecoveryEmail")
    public ResponseEntity<Map<String, String>> sendRecoveryEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        User user = userDao.findById(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    
        // Log the email address
        System.out.println("Sending recovery email to: " + email);
    
        // Generate a token for the user
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userDao.save(user);
    
        // Create the email content
        String recoveryLink = "http://localhost:4200/resetPassword?token=" + token;
        String emailContent = "<p>Click <a href=\"" + recoveryLink + "\">here</a> to reset your password.</p>";
    
        // Send the email using SendGrid API
        try {
            Email from = new Email("felipe.alcazar@alu.uclm.es", "Felipe");
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
        String token = request.get("token");
        String newPassword = request.get("password");
        User user = userDao.findByToken(token).orElseThrow(() -> {
            System.out.println("Invalid token: " + token);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token");
        });
    
        user.setPwd(newPassword);
        userDao.save(user);
    
        System.out.println("Password reset successful for user: " + user.getEmail());
    
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Password reset successful");
        return ResponseEntity.ok(responseBody);
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
	
	@GetMapping("/login2")
	public User login2(HttpServletResponse response, @RequestParam String email, @RequestParam String pwd) {
		User user = this.userService.find(email, pwd);
		user.setToken(UUID.randomUUID().toString());
		response.setHeader("token", user.getToken());
		return user;
	}
	
	@GetMapping("/login3/{email}")
	public User login3(HttpServletResponse response, @PathVariable String email, @RequestParam String pwd) {
		return this.login2(response, email, pwd);
	}
	
	@GetMapping("/getAllUsers")
	public Iterable<User>  getAllUsers() {
		return this.userService.getAllUsers();
	}
	
	@DeleteMapping("/delete")
	public void delete(HttpServletRequest request, @RequestParam String email, @RequestParam String pwd) {
		User user = this.userService.find(email, pwd);
		
		String token = request.getHeader("token");
		if (!token.equals(user.getToken()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token " + token + " inválido");
		
		this.userService.delete(email);
	}
	
	@GetMapping("/checkCookie")
	public String checkCookie(HttpServletRequest request) {
		String fakeUserId = this.findCookie(request, "fakeUserId");
		if (fakeUserId!=null) {
			User user = this.userDao.findByCookie(fakeUserId);
			if (user!=null) {
				user.setToken(UUID.randomUUID().toString());
				this.userDao.save(user);
				return user.getToken();
			}
		}
		return null;
	}
	
	@DeleteMapping("/clearAll")
	public void clearAll(HttpServletRequest request) {
		String sToken = request.getHeader("prime");
		Integer token = Integer.parseInt(sToken);
		if (!isPrime(token.intValue()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes pasar un número primo en la cabecera");
		if (sToken.length()!=3)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El nº primo debe tener tres cifras");
		this.userService.clearAll();
	}
	
	private boolean isPrime(int n) {
	    if (n <= 1) return false;
	    for (int i = 2; i <= Math.sqrt(n); i++) {
	        if (n % i == 0) return false;
	    }
	    return true;
	}
}
















