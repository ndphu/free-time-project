package ndphu.app.bluetoothchat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ndphu.app.bluetoothchat.dialog.MessengerDialog;
import ndphu.app.bluetoothchat.list.adapter.BluetoothDeviceArrayAdapter;
import ndphu.app.bluetoothchat.model.Client;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int DISCOVERABLE_TIMEOUT = 60;

	public static final int REQUEST_DISCOVERABLE = 1100;
	public static final int REQUEST_ENABLE_BT = 1000;

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
			try {
				BluetoothSocket serverSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(getResources().getString(R.string.app_uuid)));
				Client client = new Client(serverSocket.getRemoteDevice().getName(), serverSocket);
				MessengerDialog dialog = new MessengerDialog();
				dialog.setClient(client);
				dialog.show(getFragmentManager(), "messenger_dialog");
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
	}

	private void registerReceivers() {
		registerReceiver(mBluetoothStateReceiver, mBluetoothStateIntentFilter);
		registerReceiver(mDeviceFoundReceiver, mDeviceFoundIntentFilter);
		registerReceiver(mDiscoveryStartedReceiver, mDiscoveryStartedIntentFilter);
		registerReceiver(mDiscoveryFinishedReceiver, mDiscoveryFinishedIntentFilter);
		registerReceiver(mScanModeReceiver, mScanModeIntentFilter);
	}
	
	protected void startClientThread(final Client mClient) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String line = null;
					while ((line = mClient.getReader().readLine()) != null) {
						System.out.println("Client " + mClient.getClientName() + " sent: " + line);
						mClient.getReceivedMessages().add(line);
					}
				} catch (Exception ex) {

				}
			}
		}).start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBluetoothAdapter.isEnabled()) {
			if (mServerSocket == null) {
				startServer(false);
			}
			mStatusTextView.setText(R.string.bluetooth_is_ready);
			if (mBluetoothAdapter.isDiscovering()) {
				mStatusTextView.setText(R.string.scanning_in_progress);
			} else {
				mStatusTextView.setText(R.string.bluetooth_is_ready);
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
					startServer(true);
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

	// TODO: CHAT SERVER

	private BluetoothServerSocket mServerSocket;
	private List<Client> mClientList = new ArrayList<Client>();

	private void startServer(boolean cleanUpRequired) {
		System.out.println("Starting server...");
		if (cleanUpRequired) {
			cleanUpServer();
		}

		try {
			String appName = getResources().getString(R.string.app_name);
			System.out.println("App name = " + appName);
			UUID uuid = UUID.fromString(getResources().getString(R.string.app_uuid));
			System.out.println(uuid.toString());
			System.out.println();
			System.out.println("Getting server socket using name = " + appName + " and UUID = " + uuid.toString());
			mServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, uuid);
			System.out.println("Obtained");
			startServerThread();
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server started");
	}

	private void startServerThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						BluetoothSocket newClientSocket = mServerSocket.accept();
						Client client = new Client(newClientSocket.getRemoteDevice().getName(), newClientSocket);
						System.out.println("New Client Connected. Name = " + newClientSocket.getRemoteDevice().getName() + "; MAC = "
								+ newClientSocket.getRemoteDevice().getAddress());
						mClientList.add(client);
						startClientThread(client);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void stopServer() {
		cleanUpServer();
	}

	private void cleanUpServer() {
		System.out.println("Clean up server");
		for (Client client : mClientList) {
			cleanUpClient(client);
		}
		mClientList.clear();
		if (mServerSocket != null) {
			try {
				System.out.println("Close server socket");
				mServerSocket.close();
				System.out.println("Closed server socket");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mServerSocket = null;
			}
		}
	}

	private void cleanUpClient(Client client) {
		try {
			client.getBluetoothSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
