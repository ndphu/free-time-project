package ndphu.app.sms.smartviewer.ui.list;

import ndphu.app.sms.smartviewer.R;
import ndphu.app.sms.smartviewer.model.SMS;
import ndphu.app.sms.smartviewer.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<SMS> {
	LayoutInflater mLayoutInflater;

	public MessageAdapter(Context context, int resource) {
		super(context, resource);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_message, parent, false);
		}

		if (convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder)) {
			ViewHolder holder = new ViewHolder();
			holder.content = (TextView) convertView.findViewById(R.id.list_item_message_content);
			holder.timeStamp = (TextView) convertView.findViewById(R.id.list_item_message_timestamp);
			convertView.setTag(holder);
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		SMS sms = getItem(position);
		holder.content.setText(sms.getBody());
		holder.timeStamp.setText(Utils.getDate(Long.valueOf(sms.getDate()), "dd-MMM-yy hh:mm:ss"));
		return convertView;
	}

	private class ViewHolder {
		TextView content;
		TextView timeStamp;
	}
}
