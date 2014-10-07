package ndphu.app.gae.openthis.rest.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;

public class SharedLinkResource extends AbstractResource {
	@Get
	public JsonRepresentation getMyLink() throws JSONException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		User currentUser = getCurrentUser();
		String email = currentUser.getEmail();
		JSONArray linkArr = new JSONArray();
		Filter filter = CompositeFilterOperator.and(new FilterPredicate(
				"email", FilterOperator.NOT_EQUAL, email), new FilterPredicate(
				"shared", FilterOperator.EQUAL, Boolean.TRUE));
		Map<String, SortDirection> sorting = new LinkedHashMap<String, Query.SortDirection>();
		sorting.put("email", SortDirection.ASCENDING);
		sorting.put("timeStamp", SortDirection.DESCENDING);
		List<Link> links = getLinkDao().getAll(filter, sorting);
		for (Link link : links) {
			JSONObject obj = Utils.toJSONObject(link);
			linkArr.put(obj);
		}
		return new JsonRepresentation(linkArr);

	}
}
