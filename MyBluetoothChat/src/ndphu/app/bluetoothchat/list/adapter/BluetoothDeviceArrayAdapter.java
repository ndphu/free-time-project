package ndphu.app.bluetoothchat.list.adapter;

import ndphu.app.bluetoothchat.R;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {

	private LayoutInflater mInflater;

	public BluetoothDeviceArrayAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_bluetooth_device, null);
		}
		ViewHolder holder = getViewHolder(convertView);

		BluetoothDevice device = getItem(position);

		holder.name.setText(device.getName());
		holder.icon.setImageResource(R.drawable.ic_launcher);
		holder.macAddress.setText(device.getAddress());

		return convertView;
	}

	private ViewHolder getViewHolder(View view) {
		if (view.getTag() != null && view.getTag() instanceof ViewHolder) {
			return (ViewHolder) view.getTag();
		} else {
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.listview_item_bluetooth_device_name);
			holder.icon = (ImageView) view.findViewById(R.id.listview_item_bluetooth_device_icon);
			holder.macAddress = (TextView) view.findViewById(R.id.listview_item_bluetooth_device_mac_address);
			view.setTag(holder);
			return holder;
		}
	}

	private static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView macAddress;
	}

}
