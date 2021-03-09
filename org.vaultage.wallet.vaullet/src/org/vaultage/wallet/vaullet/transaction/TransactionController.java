package org.vaultage.wallet.vaullet.transaction;

import static org.vaultage.wallet.vaullet.VaulletServer.accountDao;
import static org.vaultage.wallet.vaullet.VaulletServer.userDao;

import org.vaultage.wallet.vaullet.account.Account;
import org.vaultage.wallet.vaullet.user.InvalidTokenException;
import org.vaultage.wallet.vaullet.util.Currency;

import io.javalin.http.Context;

public class TransactionController {

	public static Context doTransaction(Context ctx) throws InvalidTokenException, InvalidTransactionException {
		String originParam = ctx.formParam("origin");
		String currencyParam = ctx.formParam("currency");
		String amountParam = ctx.formParam("amount");
		String destinationParam = ctx.formParam("destination");
		String accessTokenParam = ctx.formParam("token");

		Transaction transaction = null;
		if (userDao.tokenExists(accessTokenParam)) {
			Account origin = accountDao.getAccountsById(originParam);
			Account destination = accountDao.getAccountsById(destinationParam);
			Currency currency = Currency.valueOf(currencyParam);
			double amount = Double.parseDouble(amountParam);
			transaction = new Transaction(origin, destination, currency, amount);
			transaction = transaction.execute();
		}

		return ctx.json(transaction);
	};
}
