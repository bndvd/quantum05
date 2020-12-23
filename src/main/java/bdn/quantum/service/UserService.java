package bdn.quantum.service;

import bdn.quantum.model.User;
import bdn.quantum.model.UserSession;

public interface UserService {

	public UserSession login(User user);
	public void logout(UserSession session);
	public User changePassword(User user);
	public User createUser(User user);
	
}
