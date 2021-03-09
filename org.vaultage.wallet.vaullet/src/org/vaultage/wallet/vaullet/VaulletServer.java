package org.vaultage.wallet.vaullet;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.vaultage.wallet.vaullet.account.AccountController;
import org.vaultage.wallet.vaullet.account.AccountDao;
import org.vaultage.wallet.vaullet.token.TokenController;
import org.vaultage.wallet.vaullet.transaction.Transaction;
import org.vaultage.wallet.vaullet.transaction.TransactionController;
import org.vaultage.wallet.vaullet.user.UserDao;
import org.vaultage.wallet.vaullet.util.Path;

import io.javalin.Javalin;

public class VaulletServer {

	public static AccountDao accountDao;
	public static UserDao userDao;

	private static Javalin app;
	private static int port = 80;
	
	public static void main(String[] args) {
		
		if (args != null) {
			port = Integer.valueOf(args[0]);
		}
		
		accountDao = new AccountDao();
		userDao = new UserDao();
		
		app = Javalin.create().start(port);

		app.routes(() -> {
			get("/", ctx -> ctx.result("Hello World"));
			post(Path.Web.TOKEN, ctx -> TokenController.getToken(ctx));
			post(Path.Web.ACCOUNTS, ctx -> AccountController.getAccounts(ctx));
			post(Path.Web.TRANSACTION, ctx -> TransactionController.doTransaction(ctx));
		});
	}
	
	public static void shutdown() {
		app.stop();
	}
	
}

