package controllers;

import models.User;
import play.mvc.Before;
import play.mvc.With;

@With(Secure.class)
public class LoggedInController extends MyController {
	
	@Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byUserName", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }
	
}
