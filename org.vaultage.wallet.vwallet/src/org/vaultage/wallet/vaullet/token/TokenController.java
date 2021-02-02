package org.vaultage.wallet.vaullet.token;

import static org.vaultage.wallet.vaullet.VaulletServer.userDao;

import org.vaultage.wallet.vaullet.user.InvalidTokenException;

import io.javalin.http.Context;

public class TokenController {

	public static Context getToken(Context ctx) throws InvalidTokenException {
		String userId = ctx.formParam("userId");
		String password = ctx.formParam("password");
		String token = userDao.getToken(userId, password);
		return ctx.result(token);
	};
}
