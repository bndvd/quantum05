package bdn.quantum.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bdn.quantum.QuantumConstants;
import bdn.quantum.model.User;
import bdn.quantum.model.UserSession;
import bdn.quantum.service.UserService;

@RestController("userController")
@RequestMapping(QuantumConstants.REST_URL_BASE)
public class UserController {

	@Autowired
	UserService userService;
	
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	public User createUser(@RequestBody User user) {
		return userService.createUser(user);
	}

	@RequestMapping(value = "/user/{username}", method = RequestMethod.PUT)
	public User updateUser(@PathVariable(value="username") String username, @RequestBody User user) {
		user.setUsername(username);
		return userService.changePassword(user);
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public UserSession login(@RequestBody User user) {
		return userService.login(user);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logout(@RequestBody UserSession session) {
		userService.logout(session);
	}

}
