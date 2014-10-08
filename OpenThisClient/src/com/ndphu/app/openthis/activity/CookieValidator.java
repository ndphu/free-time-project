package com.ndphu.app.openthis.activity;

import java.io.IOException;

import ndphu.app.openthis.R;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.ndphu.app.openthis.model.User;

public class CookieValidator extends AsyncTask<Void, Void, User> {
	private ProgressDialog pd;
	private Activity activity;
	private String cookie;
	private CookieValidatorListener listener;

	public CookieValidator(CookieValidatorListener listener) {
		this.listener = listener;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public static interface CookieValidatorListener {
		public void onSuccess(String cookie);

		public void onFailed(String cookie);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pd = new ProgressDialog(activity);
		pd.setTitle("Loading");
		pd.setMessage("Validating...");
		pd.setCancelable(false);
		pd.show();
	}

	@Override
	protected User doInBackground(Void... params) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(activity.getString(R.string.APP_URL) + "/rest/current_user");
		httpGet.setHeader("Cookie", this.cookie);
		HttpResponse response = null;
		try {
			response = client.execute(httpGet);
			String data = IOUtils.toString(response.getEntity().getContent());
			JSONObject obj = new JSONObject(data);
			User user = new User();
			user.setEmail(obj.has("email") ? obj.getString("email") : "");
			user.setLogoutUrl(obj.has("logoutUrl") ? obj.getString("logoutUrl") : "");
			return user;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(User result) {
		super.onPostExecute(result);
		pd.cancel();
		if (listener != null) {
			if (result == null) {
				listener.onFailed(cookie);
			} else {
				listener.onSuccess(cookie);
			}
		}
	}
}