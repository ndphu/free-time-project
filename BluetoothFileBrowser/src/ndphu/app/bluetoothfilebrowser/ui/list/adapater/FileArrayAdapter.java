package ndphu.app.bluetoothfilebrowser.ui.list.adapater;

import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ndphu.bluetooth.filebrowser.R;

public class FileArrayAdapter extends ArrayAdapter<FileObject> {

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

		FileObject fileObject = getItem(position);

		holder.name.setText(fileObject.getName());
		switch (fileObject.getType()) {
		case FileObject.TYPE_DIRECTORY:
			holder.icon.setImageResource(R.drawable.ic_directory);
			if (fileObject.getSize() == 0) {
				holder.size.setText("Empty");
			} else if (fileObject.getSize() == 1) {
				holder.size.setText("1 item");
			} else {
				holder.size.setText(fileObject.getSize() + " items");
			}
			break;
		case FileObject.TYPE_FILE:
			holder.icon.setImageResource(R.drawable.ic_file);
			holder.size.setText(Utils.size(fileObject.getSize()));
			break;
		default:
			break;
		}
		holder.details.setText(fileObject.getPath());

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
			holder.size = (TextView) view.findViewById(R.id.listview_item_file_object_size);
			view.setTag(holder);
			return holder;
		}
	}

	private static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView details;
		public TextView size;
	}

}
