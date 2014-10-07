package ndphu.app.gae.openthis.rest.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ndphu.app.gae.openthis.Utils;
import ndphu.app.gae.openthis.model.Link;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;

public class MyLinkResource extends AbstractResource {
	@Get
	public JsonRepresentation getMyLink() throws JSONException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		User currentUser = getCurrentUser();
		String email = currentUser.getEmail();
		JSONArray linkArr = new JSONArray();
		Filter filter = new FilterPredicate("email", FilterOperator.EQUAL,
				email);
		Map<String, SortDirection> sorting = new HashMap<String, Query.SortDirection>();
		sorting.put("timeStamp", SortDirection.DESCENDING);
		List<Link> links = getLinkDao().getAll(filter, sorting);
		for (Link link : links) {
			JSONObject obj = Utils.toJSONObject(link);
			linkArr.put(obj);
		}
		return new JsonRepresentation(linkArr);

	}
}
