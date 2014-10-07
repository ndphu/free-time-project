package ndphu.app.gae.openthis;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class LoginRequiredServlet extends HttpServlet {

	private static final Map<String, String> openIdProviders;
	static {
		openIdProviders = new HashMap<String, String>();
		openIdProviders.put("Google", "https://www.google.com/accounts/o8/id");
		openIdProviders.put("Yahoo", "yahoo.com");
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser(); // or req.getUserPrincipal()

		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		if (user != null) {
			resp.sendRedirect("/");
		} else {
			out.println("<p>Sign in using: </p>");
			for (String providerName : openIdProviders.keySet()) {
				String providerUrl = openIdProviders.get(providerName);
				String loginUrl = userService.createLoginURL(
						req.getRequestURI(), null, providerUrl, null);
				out.println("<p>[<a href=\"" + loginUrl + "\">" + providerName
						+ "</a>]</p>");
			}
		}
	}
}