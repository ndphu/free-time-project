package ndphu.app.bluetoothfilebrowser.ui.dialog;

import java.util.List;

import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.service.file.BluetoothFileServiceImpl;
import ndphu.app.bluetoothfilebrowser.service.file.IFileService;
import ndphu.app.bluetoothfilebrowser.service.file.LocalServiceImpl;
import ndphu.app.bluetoothfilebrowser.ui.list.adapater.FileArrayAdapter;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ndphu.bluetooth.filebrowser.R;

public class FileBrowserDialog extends DialogFragment implements OnClickListener, OnItemClickListener {
	private ListView mListView;
	private FileArrayAdapter mAdapter;
	private BluetoothDevice mRemoteDevice;
	private IFileService mFileService;
	private String mCurrentPath;
	private String mInitPath = "/sdcard";
	private Button mBackButton;
	private Button mCloseButton;

	private String[] mFileActionList = new String[] { "Download", "Properties" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_file_browser, container, false);
		mListView = (ListView) view.findViewById(R.id.dialog_file_browser_listview);
		mListView.setOnItemClickListener(this);
		mAdapter = new FileArrayAdapter(getActivity(), 0);
		mListView.setAdapter(mAdapter);
		mBackButton = (Button) view.findViewById(R.id.dialog_file_browser_button_back);
		mBackButton.setOnClickListener(this);
		mCloseButton = (Button) view.findViewById(R.id.dialog_file_browser_button_close);
		mCloseButton.setOnClickListener(this);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		browserPath(mInitPath);
	}

	private void browserPath(String path) {
		mCurrentPath = path;
		refresh();
	}

	private void refresh() {
		new AsyncTask<Void, Void, List<FileObject>>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				getDialog().setTitle(mCurrentPath);
				mAdapter.clear();
				mBackButton.setText("Loading...");
				mBackButton.setEnabled(false);
				mCloseButton.setEnabled(false);
			}

			@Override
			protected List<FileObject> doInBackground(Void... params) {
				List<FileObject> listFile = null;
				try {
					listFile = mFileService.listFile(mCurrentPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return listFile;
			}

			@Override
			protected void onPostExecute(List<FileObject> result) {
				super.onPostExecute(result);
				mBackButton.setText("Back");
				mBackButton.setEnabled(true);
				mCloseButton.setEnabled(true);
				if (result == null) {
					new AlertDialog.Builder(getActivity()).setTitle("Error").setMessage("Required service is not running on the remote server.")
							.setPositiveButton("Close", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									getDialog().dismiss();
								}
							}).create().show();
				} else {
					mAdapter.addAll(result);
				}
			}
		}.execute();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_file_browser_button_back:
			doBack();
			break;
		case R.id.dialog_file_browser_button_close:
			doClose();
			break;
		}
	}

	private void doClose() {
		this.dismiss();
	}

	private void doBack() {
		String parentDir = mCurrentPath.substring(0, mCurrentPath.lastIndexOf("/"));
		if (parentDir.length() == 0) {
			parentDir = mInitPath;
		}
		browserPath(parentDir);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		FileObject fileObject = mAdapter.getItem(position);
		if (fileObject.getType() == FileObject.TYPE_DIRECTORY) {
			browserPath(fileObject.getPath());
		} else {
			openContextMenu(fileObject);
		}
	}

	private void openContextMenu(final FileObject fileObject) {
		// TODO: Add more action
		new AlertDialog.Builder(getActivity()).setItems(mFileActionList, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					startDownloadThread(fileObject);
					break;
				case 1:
					Toast.makeText(getActivity(), "Implement later", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		}).create().show();
	}

	protected void startDownloadThread(final FileObject fileObject) {
		DownloadFileDialog dialog = new DownloadFileDialog();
		dialog.setBluetoothDevice(mRemoteDevice);
		dialog.setFileObject(fileObject);
		dialog.show(getFragmentManager(), "download_file");
	}

	public void setRemoteDevice(BluetoothDevice remoteDevice) {
		mRemoteDevice = remoteDevice;
		if (mRemoteDevice == null) {
			// Test for local
			mFileService = new LocalServiceImpl();
		} else {
			mFileService = new BluetoothFileServiceImpl(mRemoteDevice);
		}
	}

}
