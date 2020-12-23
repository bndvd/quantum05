package bdn.quantum.model;

import java.util.Date;

import bdn.quantum.QuantumConstants;

public class UserSession {
	
	private String username;
	private boolean valid;
	private Date expiration;
	
	public UserSession() {
	}
	
	public UserSession(String username) {
		this.username = username;
		this.valid = true;
		Date current = new Date();
		this.expiration = new Date(current.getTime() + QuantumConstants.USER_SESSION_LIFE_MILLIS);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isValid() {
		updateValid();
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	private void updateValid() {
		if (valid) {
			Date current = new Date();
			valid = current.before(expiration);
		}
	}

}
