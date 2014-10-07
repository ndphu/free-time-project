package com.ndphu.app.openthis.fragment;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.openthis.R;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ndphu.app.openthis.activity.AppInfo;
import com.ndphu.app.openthis.adapter.LinkArrayAdapter;
import com.ndphu.app.openthis.model.Link;
import com.ndphu.app.openthis.utils.Utils;

public class LinkListFragment extends Fragment {
	public static final int TYPE_SHARED_LINKS = 1;
	public static final int TYPE_MY_LINKS = 0;
	private ListView mListView = null;

	private int mFragmentType = 0;
	private LinkArrayAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.link_list_fragment, null, false);
		mListView = (ListView) view.findViewById(R.id.link_list_fragment_listview);
		mAdapter = new LinkArrayAdapter(getActivity(), mFragmentType);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Link link = mAdapter.getItem(position);
				String url = link.getLink();
				if (!url.startsWith("http://") && !url.startsWith("https://"))
					url = "http://" + url;
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(browserIntent);
			}
		});
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		refresh();
	}

	public void refresh() {
		mAdapter.clear();
		new AsyncTask<Void, Void, List<Link>>() {

			@Override
			protected List<Link> doInBackground(Void... params) {
				List<Link> result = new ArrayList<Link>();
				HttpGet httpGet = new HttpGet(getString(R.string.APP_URL)
						+ (mFragmentType == TYPE_MY_LINKS ? "/rest/mylink" : "/rest/shared_link"));
				httpGet.setHeader("Cookie", AppInfo.COOKIE);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					response = client.execute(httpGet);
					String data = IOUtils.toString(response.getEntity().getContent());
					JSONArray linkArr = new JSONArray(data);
					for (int i = 0; i < linkArr.length(); ++i) {
						result.add((Link) Utils.parseJSONObject(linkArr.getJSONObject(i), Link.class));
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (java.lang.InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				return result;
			}

			protected void onPostExecute(java.util.List<Link> result) {
				for (Link link : result) {
					mAdapter.add(link);
				}
			};
		}.execute();
	}

	public int getFragmentType() {
		return mFragmentType;
	}

	public void setFragmentType(int mFragmentType) {
		this.mFragmentType = mFragmentType;
	}

}
