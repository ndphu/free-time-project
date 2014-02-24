package ndphu.app.bluetoothchat.dialog;

import java.lang.reflect.Method;

import ndphu.app.bluetoothchat.R;
import ndphu.app.bluetoothchat.list.adapter.BluetoothDeviceArrayAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class ScanDevicesDialog extends DialogFragment implements OnItemClickListener {

	BluetoothAdapter mBluetoothAdapter;
	ListView mResultListView;
	BluetoothDeviceArrayAdapter mDeviceArrayAdapter;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		getActivity().registerReceiver(mDeviceFoundReceiver, mDeviceFoundIntentFilter);
		getActivity().registerReceiver(mDiscoveryStartedReceiver, mDiscoveryStartedIntentFilter);
		getActivity().registerReceiver(mDiscoveryFinishedReceiver, mDiscoveryFinishedIntentFilter);

		AlertDialog.Builder builder = new Builder(getActivity());

		builder.setTitle("Staring...");
		builder.setPositiveButton("Close", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				stopScanning();
				dismiss();
			}
		});

		View view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_scan_devices, null);
		builder.setView(view);

		mDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(getActivity(), 0);
		mResultListView = (ListView) view.findViewById(R.id.dialog_scan_devices_result_listview);
		mResultListView.setAdapter(mDeviceArrayAdapter);
		mResultListView.setOnItemClickListener(this);

		if (startScanning()) {
			Toast.makeText(getActivity(), "Scanning in progress...", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getActivity(), "Cannot start scanning process", Toast.LENGTH_SHORT).show();
			dismiss();
		}

		return builder.create();
	}

	protected boolean startScanning() {
		mDeviceArrayAdapter.clear();
		return mBluetoothAdapter.startDiscovery();
	}

	protected boolean stopScanning() {
		return mBluetoothAdapter.cancelDiscovery();
	}

	public void onDestroy() {
		getActivity().unregisterReceiver(mDeviceFoundReceiver);
		getActivity().unregisterReceiver(mDiscoveryStartedReceiver);
		getActivity().unregisterReceiver(mDiscoveryFinishedReceiver);
		stopScanning();
		super.onDestroy();
	};

	IntentFilter mDeviceFoundIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

	BroadcastReceiver mDeviceFoundReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDeviceArrayAdapter.add(device);
			}
		}
	};

	IntentFilter mDiscoveryStartedIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	IntentFilter mDiscoveryFinishedIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	BroadcastReceiver mDiscoveryStartedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				getDialog().setTitle("Scanning In Progress...");
			}
		}
	};

	BroadcastReceiver mDiscoveryFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				if (!ScanDevicesDialog.this.isDetached()) {
					if (ScanDevicesDialog.this.getDialog() != null) {
						ScanDevicesDialog.this.getDialog().setTitle("Scanning Finished!");
					}
				}
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BluetoothDevice device = mDeviceArrayAdapter.getItem(position);
		// String ACTION_PAIRING_REQUEST =
		// "android.bluetooth.device.action.PAIRING_REQUEST";
		// Intent intent = new Intent(ACTION_PAIRING_REQUEST);
		// String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
		// intent.putExtra(EXTRA_DEVICE, device);
		// String EXTRA_PAIRING_VARIANT =
		// "android.bluetooth.device.extra.PAIRING_VARIANT";
		// int PAIRING_VARIANT_PIN = 0;
		// intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// getActivity().startActivity(intent);
		try {
			Method m = device.getClass().getMethod("createBond", (Class[]) null);
			m.invoke(device, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
