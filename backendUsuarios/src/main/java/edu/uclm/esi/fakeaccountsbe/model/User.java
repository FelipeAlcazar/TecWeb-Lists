package edu.uclm.esi.fakeaccountsbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity //Incluimos la clase en la base de datos
@Table(name = "usuario") //Cambiamos el nombre de la tabla (por defecto se crearía como User pero eso daría error al ser palabra reservada)
public class User {
	
	@Id @Column(length = 60)//especificamos la clave principal en base de datos
	private String email;
	private String pwd;
	
	@JsonIgnore @Column(length=36)
	private String token;
	
	@JsonIgnore @Column(length=36)
    private String tokenPasswordReset;

	@JsonIgnore @Transient
	private long creationTime;
	
	//@JsonIgnore
	@Transient
	private String ip;
	
	private String cookie;
	
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
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;		
	}
	
	public long getCreationTime() {
		return creationTime;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setCookie(String fakeUserId) {
		this.cookie=fakeUserId;
	}
	
	public String getCookie() {
		return cookie;
	}
}
