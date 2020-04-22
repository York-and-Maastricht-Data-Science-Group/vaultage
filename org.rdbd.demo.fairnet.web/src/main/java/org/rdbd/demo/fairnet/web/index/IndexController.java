package org.rdbd.demo.fairnet.web.index;

import java.util.HashMap;
import java.util.Map;

import org.rdbd.demo.fairnet.web.account.CreateAccountView;

import spark.Request;
import spark.Response;
import spark.Route;

public class IndexController {
	
	
    public static Route serveIndexPage = (Request request, Response response) -> {
    	
        Map<String, Object> model = new HashMap<>();
        return IndexView.render(request, model, IndexView.template);
    };
}
