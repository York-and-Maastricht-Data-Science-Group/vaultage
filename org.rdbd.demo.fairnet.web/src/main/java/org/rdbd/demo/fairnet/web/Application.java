package org.rdbd.demo.fairnet.web;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.debug.DebugScreen.enableDebugScreen;

import java.util.HashSet;
import java.util.Set;

import org.rdbd.demo.fairnet.web.account.AccountController;
import org.rdbd.demo.fairnet.web.index.IndexController;
import org.rdbd.demo.fairnet.web.util.Filters;

public class Application {

	// add the new account username to the internal storage of the application
	// most of the time, application provider requires the user to at least
	// save a unique identifier for each user. it does not necessarily
	// the user's private information.
	public static Set<String> accounts = new HashSet<>();

	public static void main(String[] args) {

		// Configure Spark
		port(4567);
		staticFiles.location("/public");
		staticFiles.expireTime(600L);
		enableDebugScreen();

		// Set up before-filters (called before each get/post)
		before("*", Filters.addTrailingSlashes);
		before("*", Filters.handleLocaleChange);

		// Set up routes
		get("", IndexController.serveIndexPage);
		get("/", IndexController.serveIndexPage);
		get("/index/", IndexController.serveIndexPage);
		get("/account/create/", AccountController.serveCreateAccountPage);
		post("/account/create/", AccountController.handleCreateAccountPost);
		get("/account/view/", AccountController.serverViewAccountPage);

		// Set up after-filters (called after each get/post)
		after("*", Filters.addGzipHeader);

	}
}
