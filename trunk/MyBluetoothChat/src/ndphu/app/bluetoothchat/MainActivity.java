package ndphu.app.bluetoothchat;

import java.util.Set;

import ndphu.app.bluetoothchat.dialog.ScanDevicesDialog;
import ndphu.app.bluetoothchat.list.adapter.BluetoothDeviceArrayAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int REQUEST_ENABLE_BT = 1000;
	private BluetoothAdapter mBluetoothAdapter;
	private int mLastState = -1;

	private IntentFilter bluetootIntentFilter;

	private ProgressDialog mProgressDialog;

	// private boolean mIsRequesting = false;

	private ListView mBluetoothDevicesListView;
	private BluetoothDeviceArrayAdapter mBluetoothDeviceArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBluetoothDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(this, 0);
		mBluetoothDevicesListView = (ListView) findViewById(R.id.activity_main_listview_bluetooth_devices);
		mBluetoothDevicesListView.setAdapter(mBluetoothDeviceArrayAdapter);

		bluetootIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			new AlertDialog.Builder(this).setTitle("Not Supported").setMessage("Bluetooth is not suported on this device.")
					.setPositiveButton("Exit", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.finish();
						}
					}).show();
		}
	}

	protected void showProcessDialog() {
		mProgressDialog = ProgressDialog.show(this, "Please wait", "Bluetooth state is changing...");
	}

	protected void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mBluetoothReceiver, bluetootIntentFilter);
		if (mBluetoothAdapter.isEnabled()) {
			loadDeviceList();
		} else {
			requestTurnOnBluetooth();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mBluetoothReceiver);
	}

	private void requestTurnOnBluetooth() {
		// Bluetooth is disable
		new AlertDialog.Builder(this).setTitle("Turn On Bluetooth").setMessage("Please turn on Bluetooth to continue.")
				.setPositiveButton("Turn On", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// mIsRequesting = true;
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					}
				}).setNegativeButton("Exit", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// mIsRequesting = false;
		if (requestCode == MainActivity.REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
			} else if (resultCode == Activity.RESULT_CANCELED) {
				requestTurnOnBluetooth();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void loadDeviceList() {
		mBluetoothDeviceArrayAdapter.clear();
		Toast.makeText(this, "Loading devices...", Toast.LENGTH_SHORT).show();
		loadPairedDevice();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_scan_devices: {
			ScanDevicesDialog dialog = new ScanDevicesDialog();
			dialog.show(getFragmentManager(), "scan_device_dialog");
			return true;
		}
		case R.id.action_request_discoverable: {
			requestDiscoverable();
			return true;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void loadPairedDevice() {
		Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : bondedDevices) {
			mBluetoothDeviceArrayAdapter.add(device);
		}
	}

	private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				switch (state) {
				case BluetoothAdapter.STATE_OFF: {
					dismissProgressDialog();
					if (mLastState == BluetoothAdapter.STATE_TURNING_OFF) {
						requestTurnOnBluetooth();
					}
					break;
				}
				case BluetoothAdapter.STATE_ON: {
					dismissProgressDialog();
					break;
				}
				case BluetoothAdapter.STATE_TURNING_ON:
				case BluetoothAdapter.STATE_TURNING_OFF: {
					showProcessDialog();
				}
				}
				mLastState = state;
			}
		}
	};

	private void requestDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		// discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
		// 30);
		startActivity(discoverableIntent);
	}

}
