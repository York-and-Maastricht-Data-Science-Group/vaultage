package org.rdbd.demo.fairnet.web.account;

import java.util.HashMap;
import java.util.Map;

import org.rdbd.demo.fairnet.web.Application;
import org.rdbd.demo.fairnet.web.RDBDAdapter;

import spark.Request;
import spark.Response;
import spark.Route;

public class AccountController {

	public static Route serveCreateAccountPage = (Request request, Response response) -> {

		Map<String, Object> model = new HashMap<>();
		return CreateAccountView.render(request, model, CreateAccountView.template);
	};

	public static Route handleCreateAccountPost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		String username = request.queryParams("username");
		String fullname = request.queryParams("fullname");

		Account newAccount = new Account(username, fullname);
		
		// add the new account username to the internal storage of the application
		// most of the time, application provider requires the user to at least
		// save a unique identifier for each user. it does not necessarily 
		// the user's private information.
		Application.accounts.add(newAccount.getUsername());
		
		// send a message to a client/user's device to persist the user's account  
		RDBDAdapter.createAccount(newAccount);
		
		model.put("account", newAccount);
		return ViewAccountView.render(request, model, ViewAccountView.template);
	};

	public static Route serverViewAccountPage = (Request request, Response response) -> {

		Map<String, Object> model = new HashMap<>();

		return ViewAccountView.render(request, model, ViewAccountView.template);
	};
}
