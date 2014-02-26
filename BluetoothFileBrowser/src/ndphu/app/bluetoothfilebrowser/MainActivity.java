package ndphu.app.bluetoothfilebrowser;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import ndphu.app.bluetoothfilebrowser.server.BluetoothServer;
import ndphu.app.bluetoothfilebrowser.ui.dialog.FileBrowserDialog;
import ndphu.app.bluetoothfilebrowser.ui.list.adapater.BluetoothDeviceArrayAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bluetoothfilebrowser.R;

public class MainActivity extends Activity {

	private static final int DISCOVERABLE_TIMEOUT = 300;

	public static final int REQUEST_DISCOVERABLE = 1100;
	public static final int REQUEST_ENABLE_BT = 1000;

	public static UUID APP_UUID;
	public static String APP_NAME;
	public static String DEFAULT_DOWNLOAD_DIR;

	private BluetoothAdapter mBluetoothAdapter;
	private int mLastState = -1;

	private IntentFilter mBluetoothStateIntentFilter;

	// private boolean mIsRequesting = false;

	private ListView mPairedDevicesListView;
	private ListView mAvailableDevicesListView;

	private TextView mStatusTextView;

	private BluetoothDeviceArrayAdapter mPairedDeviceArrayAdapter;
	private BluetoothDeviceArrayAdapter mAvailableDeviceArrayAdapter;

	private OnItemClickListener mOnAvailableDeviceClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BluetoothDevice device = mAvailableDeviceArrayAdapter.getItem(position);
			FileBrowserDialog dialog = new FileBrowserDialog();
			dialog.setRemoteDevice(device);
			dialog.show(getFragmentManager(), "remote_device_browser_dialog");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		APP_NAME = getResources().getString(R.string.app_name);
		APP_UUID = UUID.fromString(getResources().getString(R.string.app_uuid));
		DEFAULT_DOWNLOAD_DIR = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS;

		mPairedDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(this, 0);
		mPairedDevicesListView = (ListView) findViewById(R.id.activity_main_listview_paired_bluetooth_devices);
		mPairedDevicesListView.setAdapter(mPairedDeviceArrayAdapter);

		mAvailableDeviceArrayAdapter = new BluetoothDeviceArrayAdapter(this, 0);
		mAvailableDevicesListView = (ListView) findViewById(R.id.activity_main_listview_available_bluetooth_devices);
		mAvailableDevicesListView.setAdapter(mAvailableDeviceArrayAdapter);
		mAvailableDevicesListView.setOnItemClickListener(mOnAvailableDeviceClick);

		mStatusTextView = (TextView) findViewById(R.id.activity_main_textview_status);

		mBluetoothStateIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			new AlertDialog.Builder(this).setTitle(R.string.not_supported).setMessage(R.string.bluetooth_not_supported_message)
					.setPositiveButton(R.string.button_exit, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.this.finish();
						}
					}).show();
		}
		registerReceivers();
		try {
			startServer();
		} catch (IOException e) {
			new AlertDialog.Builder(this).setTitle("Error").setMessage(e.getMessage()).create().show();
		}
	}

	private void registerReceivers() {
		registerReceiver(mBluetoothStateReceiver, mBluetoothStateIntentFilter);
		registerReceiver(mDeviceFoundReceiver, mDeviceFoundIntentFilter);
		registerReceiver(mDiscoveryStartedReceiver, mDiscoveryStartedIntentFilter);
		registerReceiver(mDiscoveryFinishedReceiver, mDiscoveryFinishedIntentFilter);
		registerReceiver(mScanModeReceiver, mScanModeIntentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBluetoothAdapter.isEnabled()) {
			mStatusTextView.setText(R.string.bluetooth_is_ready);
			if (mBluetoothAdapter.isDiscovering()) {
				mStatusTextView.setText(R.string.scanning_in_progress);
			} else {
				mAvailableDeviceArrayAdapter.clear();
				mBluetoothAdapter.startDiscovery();
			}
			loadPairedDeviceList();
		} else {
			requestTurnOnBluetooth();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mBluetoothAdapter.cancelDiscovery();
		unregisterReceiver();
		super.onDestroy();
	}

	private void unregisterReceiver() {
		unregisterReceiver(mBluetoothStateReceiver);
		unregisterReceiver(mDeviceFoundReceiver);
		unregisterReceiver(mDiscoveryStartedReceiver);
		unregisterReceiver(mDiscoveryFinishedReceiver);
		unregisterReceiver(mScanModeReceiver);
	}

	private void requestTurnOnBluetooth() {
		// Bluetooth is disable
		new AlertDialog.Builder(this).setTitle(R.string.turn_on_bluetooth).setMessage(R.string.request_turn_on_bluetooth_message)
				.setPositiveButton(R.string.button_turn_on, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// mIsRequesting = true;
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					}
				}).setNegativeButton(R.string.button_exit, new OnClickListener() {

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
		} else if (requestCode == MainActivity.REQUEST_DISCOVERABLE) {
			if (resultCode == Activity.RESULT_OK) {
			} else if (resultCode == Activity.RESULT_CANCELED) {

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void loadPairedDeviceList() {
		mPairedDeviceArrayAdapter.clear();
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
			// ScanDevicesDialog dialog = new ScanDevicesDialog();
			// dialog.show(getFragmentManager(), "scan_device_dialog");
			if (!mBluetoothAdapter.isDiscovering()) {
				mAvailableDeviceArrayAdapter.clear();
				mBluetoothAdapter.startDiscovery();
			}
			return true;
		}
		case R.id.action_request_discoverable: {
			requestDiscoverable();
			return true;
		}
		case R.id.action_test_server: {
			FileBrowserDialog dialog = new FileBrowserDialog();
			dialog.setRemoteDevice(null);
			dialog.show(getFragmentManager(), "file_browser_dialog");
			return true;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void loadPairedDevice() {
		Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
		for (BluetoothDevice device : bondedDevices) {
			mPairedDeviceArrayAdapter.add(device);
		}
	}

	// TODO: RECEIVER

	IntentFilter mDeviceFoundIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

	BroadcastReceiver mDeviceFoundReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mAvailableDeviceArrayAdapter.add(device);
			}
		}
	};

	IntentFilter mDiscoveryStartedIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	IntentFilter mDiscoveryFinishedIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	BroadcastReceiver mDiscoveryStartedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				mStatusTextView.setText(R.string.scanning_in_progress);
			}
		}
	};

	BroadcastReceiver mDiscoveryFinishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				mStatusTextView.setText(R.string.status_scanning_finished);
			}
		}
	};

	private BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				switch (state) {
				case BluetoothAdapter.STATE_OFF: {
					mStatusTextView.setText(R.string.bluetooth_is_off);
					stopServer();
					if (mLastState == BluetoothAdapter.STATE_TURNING_OFF) {
						requestTurnOnBluetooth();
					}
					break;
				}
				case BluetoothAdapter.STATE_ON: {
					mStatusTextView.setText(R.string.bluetooth_is_ready);
					try {
						startServer();
					} catch (IOException e) {
						new AlertDialog.Builder(MainActivity.this).setTitle("Error").setMessage(e.getMessage()).create().show();
					}
					break;
				}
				case BluetoothAdapter.STATE_TURNING_ON: {
					mStatusTextView.setText(R.string.turning_on_bluetooth_);
					break;
				}
				case BluetoothAdapter.STATE_TURNING_OFF: {
					mStatusTextView.setText(R.string.turning_off_bluetooth_);
					break;
				}
				}
				mLastState = state;
			}
		}

	};

	private void requestDiscoverable() {
		if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			mStatusTextView.setText(R.string.status_device_is_currently_visible);
			return;
		}
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_TIMEOUT);
		startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
	}

	boolean mIsDiscoverable = false;
	int mTimeCountdown = 0;

	private IntentFilter mScanModeIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

	BroadcastReceiver mScanModeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
				int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1);
				switch (scanMode) {
				case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE: {
					if (mIsDiscoverable) {
						break;
					}
					mIsDiscoverable = true;
					mTimeCountdown = DISCOVERABLE_TIMEOUT;
					// Just come to be discoverable
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (mIsDiscoverable) {
								mTimeCountdown--;
								if (mTimeCountdown == 0) {
									mIsDiscoverable = false;
									break;
								}
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										mStatusTextView.setText("Devide is visible in " + mTimeCountdown
												+ (mTimeCountdown > 1 ? " seconds" : " second"));
									}
								});
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// ignore
								}
							}
						}
					}).start();
					break;
				}
				case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
				case BluetoothAdapter.SCAN_MODE_NONE: {
					mIsDiscoverable = false;
					mStatusTextView.setText(R.string.status_device_is_currently_visible);
					break;
				}
				}
			}
		}
	};

	// TODO: Message Sever

	private BluetoothServer mServer;

	private void startServer() throws IOException {
		mServer = new BluetoothServer(this, mBluetoothAdapter);
		mServer.startServer();
	}

	private void stopServer() {
		mServer.stopServer();
	}
}
