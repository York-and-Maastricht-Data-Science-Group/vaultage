package org.vaultage.vwallet.token;

import static org.vaultage.vwallet.VWalletServer.userDao;

import org.vaultage.vwallet.user.InvalidTokenException;

import io.javalin.http.Context;

public class TokenController {

	public static Context getToken(Context ctx) throws InvalidTokenException {
		String userId = ctx.formParam("userId");
		String password = ctx.formParam("password");
		String token = userDao.getToken(userId, password);
		return ctx.result(token);
	};
}
