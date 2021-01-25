package org.vaultage.vwallet.transaction;

import static org.vaultage.vwallet.VWalletServer.accountDao;
import static org.vaultage.vwallet.VWalletServer.userDao;

import org.vaultage.vwallet.account.Account;
import org.vaultage.vwallet.user.InvalidTokenException;
import org.vaultage.vwallet.util.Currency;

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
