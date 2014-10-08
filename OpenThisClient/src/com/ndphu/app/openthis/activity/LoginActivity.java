package com.ndphu.app.openthis.activity;

import ndphu.app.openthis.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ndphu.app.openthis.activity.CookieValidator.CookieValidatorListener;

public class LoginActivity extends Activity {
	private SharedPreferences mPrefAppInfo;
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
				if (!isFetchingUser) {
					isFetchingUser = true;
					CookieValidator validator = new CookieValidator(new CookieValidatorListener() {

						@Override
						public void onSuccess(String cookie) {
							AppInfo.COOKIE = cookie;
							AppInfo.COOKIE_VALIDATED = true;
							mPrefAppInfo = getSharedPreferences(AppInfo.PREF_APP_INFO, MODE_PRIVATE);
							mPrefAppInfo.edit().putString(AppInfo.PREF_APP_INFO_KEY_COOKIE, cookie).commit();
							LoginActivity.this.finish();
						}

						@Override
						public void onFailed(String cookie) {
							AppInfo.COOKIE = null;
							AppInfo.COOKIE_VALIDATED = false;
						}
					});
					validator.setActivity(LoginActivity.this);
					validator.setCookie(cookies);
					validator.execute();
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
		mWebView.loadUrl(getString(R.string.APP_URL)
				+ "/_ah/login_redir?claimid=https://www.google.com/accounts/o8/id&continue="
				+ getString(R.string.APP_URL));
	}
}
