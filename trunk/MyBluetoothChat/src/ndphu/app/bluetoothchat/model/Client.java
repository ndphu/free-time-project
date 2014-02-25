package ndphu.app.bluetoothchat.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class Client {

	private String mClientName;
	private List<String> mReceivedMessages;
	private BluetoothSocket mBluetoothSocket;
	private BufferedReader mReader;
	private PrintWriter mWriter;

	public Client(String clientName, BluetoothSocket clientBluetoothSocket) throws IOException {
		mClientName = clientName;
		mBluetoothSocket = clientBluetoothSocket;
		mReader = new BufferedReader(new InputStreamReader(clientBluetoothSocket.getInputStream()));
		mWriter = new PrintWriter(new OutputStreamWriter(clientBluetoothSocket.getOutputStream()));
	}

	public List<String> getReceivedMessages() {
		return mReceivedMessages;
	}

	public void setReceivedMessages(List<String> mReceivedMessages) {
		this.mReceivedMessages = mReceivedMessages;
	}

	public BluetoothSocket getBluetoothSocket() {
		return mBluetoothSocket;
	}

	public void setBluetoothSocket(BluetoothSocket mBluetoothSocket) {
		this.mBluetoothSocket = mBluetoothSocket;
	}

	public String getClientName() {
		return mClientName;
	}

	public void setClientName(String mClientName) {
		this.mClientName = mClientName;
	}

	public BufferedReader getReader() {
		return mReader;
	}

	public PrintWriter getWriter() {
		return mWriter;
	}

	public String getMACAddress() {
		return mBluetoothSocket.getRemoteDevice().getAddress();
	}

	public BluetoothDevice getBluetoothDevice() {
		return mBluetoothSocket.getRemoteDevice();
	}

}
