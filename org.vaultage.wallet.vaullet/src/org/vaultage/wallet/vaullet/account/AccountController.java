package org.vaultage.wallet.vaullet.account;


import static org.vaultage.wallet.vaullet.VaulletServer.accountDao;

import java.util.List;

import org.vaultage.wallet.vaullet.user.InvalidTokenException;

import io.javalin.http.Context;

public class AccountController {

	public static Context getAccounts(Context ctx) throws InvalidTokenException {
		String accessToken = ctx.formParam("token");
		List<Account> wallet = accountDao.getAccountsByToken(accessToken);
		
		return ctx.json(wallet);
    };
}
