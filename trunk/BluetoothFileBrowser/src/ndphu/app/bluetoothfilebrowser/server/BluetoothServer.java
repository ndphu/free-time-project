package ndphu.app.bluetoothfilebrowser.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import ndphu.app.bluetoothfilebrowser.MainActivity;
import ndphu.app.bluetoothfilebrowser.command.impl.DownloadFileCommand;
import ndphu.app.bluetoothfilebrowser.command.impl.ListFileCommand;
import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;
import ndphu.app.bluetoothfilebrowser.service.file.LocalServiceImpl;

import org.apache.commons.io.IOUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

public class BluetoothServer {

	private static final String TAG = BluetoothServer.class.getName();
	private static final String ERROR_EXCEPTION = null;
	private BluetoothServerSocket mServerSocket;
	protected boolean mIsRunning = false;

	private LocalServiceImpl mFileService;
	private BluetoothAdapter mBluetoothAdapter;

	private static final int WRITE_BUFFER = 102400;

	public BluetoothServer(Context context, BluetoothAdapter adapter) {
		mFileService = new LocalServiceImpl();
		mBluetoothAdapter = adapter;
	}

	public void startServer() throws IOException {
		if (mIsRunning) {
			return;
		}

		Log.i(MainActivity.APP_NAME, "Starting Server...");
		mServerSocket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(MainActivity.APP_NAME, MainActivity.APP_UUID);
		mIsRunning = true;
		mServerThread.start();
	}

	public void stopServer() {
		mIsRunning = false;
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			mServerSocket = null;
		}
	}

	private Thread mServerThread = new Thread(new Runnable() {

		@Override
		public void run() {
			Log.i(MainActivity.APP_NAME, "Starting Server Thread...");
			try {
				while (mIsRunning) {
					Log.i(MainActivity.APP_NAME, "Waiting for client...");
					BluetoothSocket clientSocket = mServerSocket.accept();
					Log.i(MainActivity.APP_NAME, "A Client Connected. Name: " + clientSocket.getRemoteDevice().getName());
					handleClientConnection(clientSocket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	});

	protected void handleClientConnection(BluetoothSocket clientSocket) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			Log.i(TAG, "Waiting for client input...");
			String commandString = reader.readLine();
			Log.i(TAG, "Client: " + clientSocket.getRemoteDevice().getName() + "; Command: " + commandString);

			String commandPrefix = commandString.substring(0, commandString.indexOf(":"));
			Log.i(TAG, "Command Prefix:" + commandPrefix);
			String commandData = commandString.substring(commandString.indexOf(":") + 1);

			if (commandPrefix.equals(ListFileCommand.COMMAND_PREFIX)) {
				handleListFileCommand(commandData, clientSocket.getOutputStream());
			} else if (commandPrefix.equals(DownloadFileCommand.COMMAND_PREFIX)) {
				handleDownloadFile(commandData, clientSocket.getOutputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				IOUtils.closeQuietly(clientSocket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				IOUtils.closeQuietly(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			IOUtils.closeQuietly(clientSocket);
		}
	}

	private String handleListFileCommand(String path, OutputStream out) throws Exception {
		Log.i(TAG, "Query directory: " + path);
		List<AbstractFileObject> listFile = mFileService.listFile(path);
		Gson gson = new Gson();
		String result = gson.toJson(listFile);
		PrintWriter writer = new PrintWriter(out);
		writer.println(result);
		writer.flush();
		writer.close();
		return result;
	}

	private void handleDownloadFile(String path, OutputStream out) throws FileNotFoundException, IOException {
		Log.i(TAG, "Download File: " + path);

		FileInputStream in = new FileInputStream(new File(path));
		int total = 0;
		int read = 0;
		byte[] buffer = new byte[WRITE_BUFFER];
		while ((read = in.read(buffer)) > 0) {
			System.out.println("Read: " + read);
			total += read;
			out.write(buffer, 0, read);
			out.flush();
			System.out.println("Total: " + total);
		}
	}

}
