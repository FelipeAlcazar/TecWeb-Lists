package edu.uclm.esi.fakeaccountsbe.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;

@Service
public class UserService {
        
    @Autowired //incluimos esto para los DAO siempre
    private UserDao userDao;
    
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