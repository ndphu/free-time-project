package ndphu.app.sms.smartviewer.ui.list;

import ndphu.app.sms.smartviewer.R;
import ndphu.app.sms.smartviewer.model.Contact;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Contact> {
	
	LayoutInflater mLayoutInflater;

	public ContactAdapter(Context context, int resource) {
		super(context, resource);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_item_contact, parent, false);
		}
		
		if (convertView.getTag() == null || ! (convertView.getTag() instanceof ViewHolder)) {
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.list_item_contact_name);
			holder.picture = (ImageView) convertView.findViewById(R.id.list_item_contact_profile_picture);
			convertView.setTag(holder);
		}
		
		ViewHolder holder = (ViewHolder) convertView.getTag();
		
		Contact contact = getItem(position);
		holder.name.setText(contact.getName());
		holder.picture.setImageResource(R.drawable.ic_launcher);
		
		return convertView;
	}
	
	private class ViewHolder {
		ImageView picture;
		TextView name;
	}

}
