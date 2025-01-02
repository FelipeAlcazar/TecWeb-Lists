package edu.uclm.esi.fakeaccountsbe.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import edu.uclm.esi.fakeaccountsbe.model.User;

public interface UserDao extends CrudRepository<User, String> {
    User findByCookie(String cookie);
    Optional<User> findByToken(String token);
    Optional<User> findByTokenPasswordReset(String tokenPasswordReset);
}
