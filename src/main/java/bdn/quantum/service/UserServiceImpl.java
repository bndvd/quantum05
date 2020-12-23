package bdn.quantum.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bdn.quantum.QuantumProperties;
import bdn.quantum.model.KeyvalEntity;
import bdn.quantum.model.User;
import bdn.quantum.model.UserSession;

@Service("userService")
public class UserServiceImpl implements UserService {

	private HashMap<String, UserSession> sessionMap = new HashMap<>();
	
	@Autowired
	KeyvalService keyvalService;
	
	
	@Override
	public User createUser(User user) {
		if (user == null || user.getUsername() == null || user.getUsername().trim().equals("") ||
				user.getWp1() == null || user.getWp1().trim().equals("")) {
			return null;
		}
		
		String propKey = QuantumProperties.PROP_PREFIX + QuantumProperties.USER_CREDENTIALS + user.getUsername();
		String propVal = keyvalService.getKeyvalStr(propKey);
		// if user already exists, error out
		if (propVal != null && ! propVal.trim().equals("")) {
			System.err.println("UserService::createUser FAILED. User " + user.getUsername() + " already exists.");
			return null;
		}
		
		propVal = convertWpString(user.getWp1());
		KeyvalEntity ke = new KeyvalEntity(propKey, propVal);
		keyvalService.setKeyval(ke);
		
		// null out passwords for security reasons
		user.setWp1(null);
		user.setWp2(null);
		return user;
	}

	@Override
	public User changePassword(User user) {
		if (user == null || user.getUsername() == null || user.getUsername().trim().equals("") ||
				user.getWp1() == null || user.getWp1().trim().equals("") ||
				user.getWp2() == null || user.getWp2().trim().equals("")) {
			return null;
		}
		
		boolean authenticated = authenticate(user.getUsername(), user.getWp1());
		if (!authenticated) {
			System.err.println("UserService::changePassword FAILED. User " + user.getUsername() + " could not be authenticated.");
			return null;
		}
		
		String propKey = QuantumProperties.PROP_PREFIX + QuantumProperties.USER_CREDENTIALS + user.getUsername();
		String propVal = convertWpString(user.getWp2());
		KeyvalEntity ke = new KeyvalEntity(propKey, propVal);
		keyvalService.setKeyval(ke);
		
		// null out passwords for security reasons
		user.setWp1(null);
		user.setWp2(null);
		return user;
	}

	@Override
	public UserSession login(User user) {
		if (user == null || user.getUsername() == null || user.getUsername().trim().equals("") ||
				user.getWp1() == null || user.getWp1().trim().equals("")) {
			System.err.println("UserService::login FAILED. User/password info was null/empty.");
			return null;
		}
		
		// check if valid session in cache
		UserSession session = sessionMap.get(user.getUsername());
		if (session != null) {
			if (session.isValid()) {
				return session;
			}
			else {
				sessionMap.remove(user.getUsername());
			}
		}
		
		boolean authenticated = authenticate(user.getUsername(), user.getWp1());
		if (!authenticated) {
			System.err.println("UserService::login FAILED. User " + user.getUsername() + " could not be authenticated.");
			return null;
		}
		
		session = new UserSession(user.getUsername());
		sessionMap.put(user.getUsername(), session);
		return session;
	}

	@Override
	public void logout(UserSession session) {
		if (session != null) {
			sessionMap.remove(session.getUsername());
		}
	}
	
	private String convertWpString(String raw) {
		if (raw == null) {
			return null;
		}
		
		char[] charsIn = raw.toCharArray();
		StringBuffer charsOut = new StringBuffer();
		
		for (char c : charsIn) {
			charsOut.append(Character.getNumericValue(c) + 1);
		}
		
		return charsOut.toString();
	}
	
	private boolean authenticate(String username, String wp) {
		String propKey = QuantumProperties.PROP_PREFIX + QuantumProperties.USER_CREDENTIALS + username;
		String propVal = keyvalService.getKeyvalStr(propKey);
		// user does not exist
		if (propVal == null || propVal.trim().equals("")) {
			return false;
		}

		// stored password does not match passed in existing password
		String passedInWp = convertWpString(wp);
		if (! propVal.equals(passedInWp)) {
			return false;
		}
		
		return true;
	}
	
}
