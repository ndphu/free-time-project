package ndphu.app.gae.openthis.rest.resource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import ndphu.app.gae.openthis.Utils;
import ndphu.app.gae.openthis.model.Link;
import ndphu.app.gae.zd.ZingParser;
import ndphu.app.gae.zd.model.Song;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.users.User;

public class LinkResource extends AbstractResource {

	@Post
	public JsonRepresentation addLink(JsonRepresentation json)
			throws JSONException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, IOException {
		User user = getCurrentUser();
		JSONObject input = json.getJsonObject();
		Link link = new Link();
		link.setName(input.getString("name"));
		link.setLink(input.getString("link"));
		link.setDesc(input.getString("desc"));
		link.setEmail(user.getEmail());
		link.setShared(input.getBoolean("shared"));
		link.setTimeStamp(new Date());
		getLinkDao().save(link);
		if (link.getLink() != null && link.getLink().contains("mp3.zing.vn")) {
			ZingParser parser = new ZingParser(link.getLink());
			List<Song> songList = parser.getSongList();
			for (Song song : songList) {
			}
		}

		return new JsonRepresentation(link);
	}

	@Put
	public JsonRepresentation modifyLink(JsonRepresentation json)
			throws JSONException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, InstantiationException,
			EntityNotFoundException {
		JSONObject input = json.getJsonObject();
		Long linkId = input.getLong("id");
		Link link = getLinkDao().get(linkId);
		link.setName(input.getString("name"));
		link.setLink(input.getString("link"));
		link.setDesc(input.getString("desc"));
		link.setShared(input.getBoolean("shared"));
		getLinkDao().update(link);
		return new JsonRepresentation(input);
	}

	@Get
	public JsonRepresentation getLink() throws JSONException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			InstantiationException, EntityNotFoundException {
		Long id = Long.valueOf(getRequest().getAttributes().get("id")
				.toString());
		Link link = getLinkDao().get(id);
		return new JsonRepresentation(Utils.toJSONObject(link));
	}

	@Delete
	public StringRepresentation delete() {
		Long id = Long.valueOf(getRequest().getAttributes().get("id")
				.toString());
		getLinkDao().delete(id);
		return new StringRepresentation("0");
	}
}
