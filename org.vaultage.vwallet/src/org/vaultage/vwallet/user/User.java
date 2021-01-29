package org.vaultage.vwallet.user;

public class User {

	private String userId;
	private String userSecret;
	private String accessToken;

	public User(String userId, String password, String accessToken) {
		this.userId = userId;
		this.userSecret = password;
		this.accessToken = accessToken;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return userSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	
}
