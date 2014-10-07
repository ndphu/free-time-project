package ndphu.app.gae.openthis.rest.resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class CurrentUser extends ServerResource {

	@Get
	public JsonRepresentation handleGet() throws JSONException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user == null) {
			return new JsonRepresentation(JSONObject.NULL);
		} else {
			String email = user.getEmail();
			String logoutUrl = userService.createLogoutURL("/login");
			JSONObject obj = new JSONObject();
			obj.put("email", email);
			obj.put("logoutUrl", logoutUrl);
			return new JsonRepresentation(obj);
		}
	}

}