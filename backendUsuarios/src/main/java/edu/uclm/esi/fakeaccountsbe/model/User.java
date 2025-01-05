package edu.uclm.esi.fakeaccountsbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;

@Entity
@Table(name = "usuario")
public class User {

    @Id @Column(length = 60)
    private String email;
    private String pwd;

    @JsonIgnore @Column(length=36)
    private String token;

    @JsonIgnore @Column(length=36)
    private String tokenPasswordReset;

    @JsonIgnore
    private Instant tokenCreationTime;

    @Column(nullable = false)
    private boolean hasPaid;

    @Column(nullable = false)
    private boolean isConfirmed;

    @Transient
    private String ip;

    private String cookie;

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenPasswordReset() {
        return tokenPasswordReset;
    }

    public void setTokenPasswordReset(String tokenPasswordReset) {
        this.tokenPasswordReset = tokenPasswordReset;
    }

    public Instant getTokenCreationTime() {
        return tokenCreationTime;
    }

    public void setTokenCreationTime(Instant tokenCreationTime) {
        this.tokenCreationTime = tokenCreationTime;
    }

    public boolean isHasPaid() {
        return hasPaid;
    }

    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setConfirmed(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setCookie(String fakeUserId) {
        this.cookie = fakeUserId;
    }

    public String getCookie() {
        return cookie;
    }
}