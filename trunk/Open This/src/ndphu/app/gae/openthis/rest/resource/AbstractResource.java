package ndphu.app.gae.openthis.rest.resource;

import ndphu.app.gae.openthis.dao.LinkDao;
import ndphu.app.gae.openthis.dao.gae.LinkDaoImpl;

import org.restlet.resource.ServerResource;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class AbstractResource extends ServerResource {
	
	private static LinkDao linkDao = null;
	
	static {
		setLinkDao(new LinkDaoImpl());
	}

	public User getCurrentUser() {
		UserService userService = UserServiceFactory.getUserService();
		User currentUser = userService.getCurrentUser();
		if (currentUser == null) {
			throw new RuntimeException("User not logged in!");
		}
		return currentUser;
	}

	public static LinkDao getLinkDao() {
		return linkDao;
	}

	public static void setLinkDao(LinkDao linkDao) {
		AbstractResource.linkDao = linkDao;
	}
}
