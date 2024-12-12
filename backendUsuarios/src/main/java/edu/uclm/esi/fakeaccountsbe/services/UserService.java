package edu.uclm.esi.fakeaccountsbe.services;

import java.util.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
	
	//private Map<String, User> users = new ConcurrentHashMap<>();
	//private Map<String, List<User>> usersByIp = new ConcurrentHashMap<>();

	public void registrar(String ip, User user) {
		if(this.userDao.findById(user.getEmail()).isPresent())
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ya existe un usuario con ese correo electrónico");
		
		user.setIp(ip);
		user.setCreationTime(System.currentTimeMillis());
		this.userDao.save(user);
	}

	public void login(User tryingUser) {
		this.find(tryingUser.getEmail(), tryingUser.getPwd());
	}

	public void clearAll() {
		//this.usersByIp.clear();
		//this.users.clear();
		this.userDao.deleteAll();
	}

	public Iterable<User> getAllUsers() {
		//return this.users.values();
		return this.userDao.findAll();
	}

	public User find(String email, String pwd) {
	    Optional<User> optUser = this.userDao.findById(email);
	    
	    if (!optUser.isPresent())
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Credenciales incorrectas");
	    
	    User user = optUser.get();
	    if (!user.getPwd().equals(pwd))
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Credenciales incorrectas");

	    return user;
	}

	public User findByToken(String token) {
	    Optional<User> userOptional = this.userDao.findByToken(token);
	    if (!userOptional.isPresent()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido");
	    }
	    return userOptional.get();
	}

	public void delete(String email) {
		//User user = this.users.remove(email);
		//List<User> users = this.usersByIp.get(user.getIp());
		//users.remove(user);
		//if (users.isEmpty())
		//	this.usersByIp.remove(user.getIp());
		
		this.userDao.deleteById(email);
	}

	public synchronized void clearOld() {
		//long time = System.currentTimeMillis();
		//for (User user : this.users.values())
		//	if (time> 600_000 + user.getCreationTime())
		//		this.delete(user.getEmail());
	}

}














