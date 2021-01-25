package org.vaultage.vwallet.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDao {

	private final List<User> users = new ArrayList<User>();

	public List<User> getUsers() {
		return users;
	}

	public String getToken(String userId, String password) throws InvalidTokenException {

		User user = users.stream() //
				.filter(u -> u.getUserId().equals(userId) //
						&& u.getPassword().equals(password))//
				.findFirst().orElse(null);

		if (user == null)
			throw new InvalidTokenException();

		return user.getAccessToken();
	}

	public User getUserByToken(String accessToken) throws InvalidTokenException {

		User user = users.stream() //
				.filter(u -> u.getAccessToken().equals(accessToken)) //
				.findFirst().orElse(null);

		if (user == null)
			throw new InvalidTokenException();

		return user;
	}

	public boolean tokenExists(String accessToken) {
		return users.stream().anyMatch(u -> u.getAccessToken().equals(accessToken));
	}
}
