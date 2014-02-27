package ndphu.app.bluetoothfilebrowser.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import ndphu.app.bluetoothfilebrowser.model.FileObject;
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

	private static final int READ_BUFFER_SIZE = 102400;

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
			if (mServerSocket != null) {
				mServerSocket.close();
			}
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
		List<FileObject> listFile = mFileService.listFile(path);
		Gson gson = new Gson();
		String result = gson.toJson(listFile);
		PrintWriter writer = new PrintWriter(out);
		writer.println(result);
		writer.flush();
		writer.close();
		return result;
	}

	private void handleDownloadFile(String path, OutputStream out) throws FileNotFoundException, IOException {
		Log.i(TAG, "Download File Request: " + path);
		File fileToDownload = new File(path);
		FileInputStream in = new FileInputStream(fileToDownload);
		BufferedInputStream reader = new BufferedInputStream(in);
		BufferedOutputStream writer = new BufferedOutputStream(out);
		int read = 0;
		long total = 0;
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		while ((read = reader.read(buffer)) > 0) {
			total += read;
			writer.write(buffer, 0, read);
			writer.flush();
			Log.d(TAG, "Sent: " + total + "B; Fize size: " + fileToDownload.length());
			if (fileToDownload.length() == total) {
				Log.i(TAG, "File sent. Size = " + total);
				break;
			}
		}
		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);
	}

	public boolean isRunning() {
		return mIsRunning;
	}
}
