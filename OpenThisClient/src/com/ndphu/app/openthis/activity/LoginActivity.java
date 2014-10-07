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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ndphu.app.openthis.model.User;

public class LoginActivity extends Activity {
	private static final String TAG = "AccountListActivity";
	protected boolean isFetchingUser = false;
	private WebViewClient webViewClientImpl = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			mWebView.loadUrl(url);
			return true;
		}

		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			if (url.matches(getString(R.string.APP_URL) + "(/#)")) {
				String cookies = CookieManager.getInstance().getCookie(url);
				AppInfo.COOKIE = cookies;
				if (!isFetchingUser) {
					isFetchingUser = true;
					new FetchUser().execute();
				}
			}
			super.onPageStarted(view, url, favicon);
		};
	};
	private WebView mWebView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Invoke Login Activity");
		mWebView = new WebView(this);
		mWebView.getSettings().setJavaScriptEnabled(true);
		setContentView(mWebView);
		mWebView.setWebViewClient(webViewClientImpl);
		mWebView.loadUrl(getString(R.string.APP_URL) + "/_ah/login_redir?claimid=https://www.google.com/accounts/o8/id&continue="
				+ getString(R.string.APP_URL));
	}
	
	private class FetchUser extends AsyncTask<Void, Void, User> {
		private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(LoginActivity.this);
			pd.setTitle("Loading");
			pd.setMessage("Fetching user info...");
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected User doInBackground(Void... params) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(getString(R.string.APP_URL) + "/rest/current_user");
			httpGet.setHeader("Cookie", AppInfo.COOKIE);
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
			if (result == null) {
				// Login failed
				LoginActivity.this.finish();
			} else {
				AppInfo.CURRENT_USER = result;
			}
			LoginActivity.this.finish();
		}
	}
}
