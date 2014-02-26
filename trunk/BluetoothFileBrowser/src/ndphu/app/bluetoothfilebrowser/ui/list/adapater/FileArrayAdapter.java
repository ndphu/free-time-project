package ndphu.app.bluetoothfilebrowser.ui.list.adapater;

import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bluetoothfilebrowser.R;

public class FileArrayAdapter extends ArrayAdapter<AbstractFileObject> {
	
	private LayoutInflater mInflater;

	public FileArrayAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_file_object, null);
		}
		ViewHolder holder = getViewHolder(convertView);

		AbstractFileObject object = getItem(position);

		holder.name.setText(object.getName());
		switch (object.getType()) {
		case AbstractFileObject.TYPE_DIRECTORY:
			holder.icon.setImageResource(R.drawable.ic_directory);
			break;
		case AbstractFileObject.TYPE_FILE:
			holder.icon.setImageResource(R.drawable.ic_file);	
			break;
		default:
			break;
		}
		holder.details.setText(object.getPath());

		return convertView;
	}

	private ViewHolder getViewHolder(View view) {
		if (view.getTag() != null && view.getTag() instanceof ViewHolder) {
			return (ViewHolder) view.getTag();
		} else {
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.listview_item_file_object_name);
			holder.icon = (ImageView) view.findViewById(R.id.listview_item_file_object_icon);
			holder.details = (TextView) view.findViewById(R.id.listview_item_file_object_details);
			view.setTag(holder);
			return holder;
		}
	}

	private static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView details;
	}

}
