package ndphu.app.gae.openthis.rest.app;

import ndphu.app.gae.openthis.rest.resource.CurrentUser;
import ndphu.app.gae.openthis.rest.resource.LinkResource;
import ndphu.app.gae.openthis.rest.resource.MyLinkResource;
import ndphu.app.gae.openthis.rest.resource.SharedLinkResource;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestApplication extends Application {

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/current_user", CurrentUser.class);
		router.attach("/link", LinkResource.class);
		router.attach("/link/{id}", LinkResource.class);
		router.attach("/mylink", MyLinkResource.class);
		router.attach("/shared_link", SharedLinkResource.class);
		return router;
	}
}