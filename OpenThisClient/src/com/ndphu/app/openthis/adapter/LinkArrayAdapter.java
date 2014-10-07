package com.ndphu.app.openthis.adapter;

import ndphu.app.openthis.R;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ndphu.app.openthis.fragment.LinkListFragment;
import com.ndphu.app.openthis.model.Link;

public class LinkArrayAdapter extends ArrayAdapter<Link> {

	private LayoutInflater mInflater;
	private int mType;

	public LinkArrayAdapter(Context context, int type) {
		super(context, 0);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mType = type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_link_details, parent, false);
		}
		ViewHolder holder = null;
		if (convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder)) {
			holder = new ViewHolder();
			holder.mName = (TextView) convertView.findViewById(R.id.listview_item_link_details_name);
			holder.mDecs = (TextView) convertView.findViewById(R.id.listview_item_link_details_desc);
			holder.mLink = (TextView) convertView.findViewById(R.id.listview_item_link_details_link);
			holder.mTime = (TextView) convertView.findViewById(R.id.listview_item_link_details_time);
			holder.mOwner = (TextView) convertView.findViewById(R.id.listview_item_link_details_owner);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Link link = getItem(position);
		holder.mName.setText(link.getName());
		holder.mDecs.setText(link.getDesc());
		holder.mLink.setText(Html.fromHtml("<a href=''>" + link.getLink() + "</a>"));
		holder.mTime.setText(link.getTimeStamp());
		if (mType == LinkListFragment.TYPE_MY_LINKS) {
			holder.mOwner.setVisibility(View.GONE);
		} else {
			holder.mOwner.setVisibility(View.VISIBLE);
			holder.mOwner.setText(link.getEmail());
		}
		return convertView;
	}

	private static class ViewHolder {
		public TextView mName;
		public TextView mDecs;
		public TextView mLink;
		public TextView mTime;
		public TextView mOwner;
	}

}
