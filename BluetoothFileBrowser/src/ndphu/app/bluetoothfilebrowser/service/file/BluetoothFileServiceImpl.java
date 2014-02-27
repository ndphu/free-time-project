package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.server.IDownloadListener;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothFileServiceImpl implements IFileService {
	private static final String TAG = BluetoothFileServiceImpl.class.getName();

	private static final int WRITE_BUFFER_SIZE = 102400;

	private BluetoothDevice mDevice;

	public BluetoothFileServiceImpl(BluetoothDevice device) {
		mDevice = device;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public List<FileObject> listFile(String path) throws IOException {
		Log.i(TAG, "Browse path: " + path);
		List<FileObject> result = new ArrayList<FileObject>();
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

	private void handleResponse(String response, List<FileObject> result) {
		Gson gson = new Gson();
		JSONArray array;
		try {
			array = new JSONArray(response);
			for (int i = 0; i < array.length(); ++i) {
				JSONObject _object = array.getJSONObject(i);
				FileObject fileObject = gson.fromJson(_object.toString(), FileObject.class);
				result.add(fileObject);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public BluetoothDevice getDevice() {
		return mDevice;
	}

	public File downloadFile(FileObject fileObject, IDownloadListener listener) throws Exception {
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
			return writeStreamToFile(fileObject, socket.getInputStream(), listener);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(socket.getInputStream());
			IOUtils.closeQuietly(socket.getOutputStream());
			IOUtils.closeQuietly(socket);
		}
		return null;
	}

	private File writeStreamToFile(FileObject fileObject, InputStream inputStream, IDownloadListener listener) throws FileNotFoundException,
			IOException {
		Log.i(TAG, "Download file " + fileObject.getName() + " to " + MainActivity.DEFAULT_DOWNLOAD_DIR);
		File file = new File(MainActivity.DEFAULT_DOWNLOAD_DIR + "/" + fileObject.getName());
		file.createNewFile();
		BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
		BufferedInputStream reader = new BufferedInputStream(inputStream);
		int read = 0;
		int total = 0;
		byte[] buffer = new byte[WRITE_BUFFER_SIZE];
		try {
			while ((read = reader.read(buffer)) > 0) {
				Log.d(TAG, "Read: " + read);
				total += read;
				writer.write(buffer, 0, read);
				writer.flush();
				Log.d(TAG, "Total: " + total);
				if (listener != null) {
					listener.onUpdateSize(total);
				}
				if (total == fileObject.getSize()) {
					if (listener != null) {
						listener.onCompleted();
					}
					Log.i(TAG, "File downloaded: " + file.getAbsoluteFile() + "; size = " + total);
					return file;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (listener != null && total < fileObject.getSize()) {
				listener.onError(ex);
			}
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
		}
		return null;
	}
}
