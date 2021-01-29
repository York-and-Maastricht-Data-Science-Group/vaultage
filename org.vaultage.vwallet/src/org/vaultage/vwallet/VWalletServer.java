package org.vaultage.vwallet;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.vaultage.vwallet.account.AccountController;
import org.vaultage.vwallet.account.AccountDao;
import org.vaultage.vwallet.token.TokenController;
import org.vaultage.vwallet.transaction.Transaction;
import org.vaultage.vwallet.transaction.TransactionController;
import org.vaultage.vwallet.user.UserDao;
import org.vaultage.vwallet.util.Path;

import io.javalin.Javalin;

public class VWalletServer {

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

