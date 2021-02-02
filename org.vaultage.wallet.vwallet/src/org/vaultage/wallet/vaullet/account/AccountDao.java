package org.vaultage.wallet.vaullet.account;

import static org.vaultage.wallet.vaullet.VaulletServer.userDao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.vaultage.wallet.vaullet.user.InvalidTokenException;
import org.vaultage.wallet.vaullet.user.User;

public class AccountDao {

	// user : account = one-to-many
	private final List<Account> accounts = new ArrayList<Account>();

	public List<Account> getAccountsByToken(String accessToken) throws InvalidTokenException {
		User user = userDao.getUserByToken(accessToken);
		return accounts.stream().filter(a -> a.getUserId().equals(user.getUserId()))
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	public List<Account> getAccounts() {
		return this.accounts;
	}

	public Account getAccountsById(String destination) {
		return accounts.stream().filter(a -> a.getAccountId().equals(destination)).findFirst().orElse(null);
	}
}
