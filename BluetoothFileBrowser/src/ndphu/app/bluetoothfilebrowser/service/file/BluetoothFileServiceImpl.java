package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import ndphu.app.bluetoothfilebrowser.MainActivity;
import ndphu.app.bluetoothfilebrowser.command.Command;
import ndphu.app.bluetoothfilebrowser.command.impl.DownloadFileCommand;
import ndphu.app.bluetoothfilebrowser.command.impl.ListFileCommand;
import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothFileServiceImpl implements IFileService {
	private static final String TAG = BluetoothFileServiceImpl.class.getName();

	private static final int READ_BUFFER = 102400;

	private BluetoothDevice mDevice;

	public BluetoothFileServiceImpl(BluetoothDevice device) {
		mDevice = device;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public List<AbstractFileObject> listFile(String path) throws IOException {
		Log.i(TAG, "Browse path: " + path);
		List<AbstractFileObject> result = new ArrayList<AbstractFileObject>();
		BluetoothSocket socket = null;
		PrintStream printer = null;
		BufferedReader reader = null;
		try {
			socket = mDevice.createInsecureRfcommSocketToServiceRecord(MainActivity.APP_UUID);
			Log.i(TAG, "Connecting to server...");
			socket.connect();
			Log.i(TAG, "Connecting to server successfully!");
			printer = new PrintStream(socket.getOutputStream());
			Command command = new ListFileCommand(path);
			String commandString = command.getCommandString();
			printer.println(commandString);
			printer.flush();
			Log.i(TAG, "Sent command string: " + commandString);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			Log.i(TAG, "Waiting for server response: " + commandString);
			String response = reader.readLine();
			System.out.println("Server say: " + response);
			handleResponse(response, result);
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (printer != null) {
				printer.close();
			}

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private void handleResponse(String response, List<AbstractFileObject> result) {
		Gson gson = new Gson();
		JSONArray array;
		try {
			array = new JSONArray(response);
			for (int i = 0; i < array.length(); ++i) {
				JSONObject _object = array.getJSONObject(i);
				AbstractFileObject fileObject = gson.fromJson(_object.toString(), AbstractFileObject.class);
				result.add(fileObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public BluetoothDevice getDevice() {
		return mDevice;
	}

	@Override
	public File downloadFile(AbstractFileObject fileObject, String destDir) throws Exception {
		Log.i(TAG, "Download file: " + fileObject.getPath());
		BluetoothSocket socket = null;
		PrintStream printer = null;
		try {
			socket = mDevice.createInsecureRfcommSocketToServiceRecord(MainActivity.APP_UUID);
			Log.i(TAG, "Connecting to server...");
			socket.connect();
			Log.i(TAG, "Connecting to server successfully!");
			printer = new PrintStream(socket.getOutputStream());
			Command command = new DownloadFileCommand(fileObject);
			String commandString = command.getCommandString();
			printer.println(commandString);
			printer.flush();
			Log.i(TAG, "Sent command string: " + commandString);
			Log.i(TAG, "Waiting for server response: " + commandString);
			return writeStreamToFile(fileObject, socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket.getInputStream());
			IOUtils.closeQuietly(socket.getOutputStream());
			IOUtils.closeQuietly(socket);
		}
		return null;
	}

	private File writeStreamToFile(AbstractFileObject fileObject, InputStream inputStream) throws FileNotFoundException, IOException {
		Log.i(TAG, "Download file " + fileObject.getName() + " to " + MainActivity.DEFAULT_DOWNLOAD_DIR);
		File file = new File(MainActivity.DEFAULT_DOWNLOAD_DIR + "/" + fileObject.getName());
		FileOutputStream out = new FileOutputStream(file);
		file.createNewFile();
		int read = 0;
		int total = 0;
		byte[] buffer = new byte[READ_BUFFER];
		try {
			while ((read = inputStream.read(buffer)) > 0) {
				Log.d(TAG, "Read: " + read);
				total += read;
				out.write(buffer, 0, read);
				out.flush();
				Log.d(TAG, "Total: " + total);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Log.i(TAG, "File downloaded: " + file.getAbsoluteFile() + "; size = " + total);
		IOUtils.closeQuietly(out);
		return file;
	}

}
