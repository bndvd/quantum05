package bdn.quantum.model;

public class User {

	private String username;
	private String wp1;
	private String wp2;
	
	
	public User() {
	}
	
	public User(String username, String wp1, String wp2) {
		this.username = username;
		this.wp1 = wp1;
		this.wp2 = wp2;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getWp1() {
		return wp1;
	}

	public void setWp1(String wp1) {
		this.wp1 = wp1;
	}

	public String getWp2() {
		return wp2;
	}

	public void setWp2(String wp2) {
		this.wp2 = wp2;
	}
	
}
