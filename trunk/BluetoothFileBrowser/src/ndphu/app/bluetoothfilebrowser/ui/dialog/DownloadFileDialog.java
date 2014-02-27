package ndphu.app.bluetoothfilebrowser.ui.dialog;

import ndphu.app.bluetoothfilebrowser.MainActivity;
import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.server.IDownloadListener;
import ndphu.app.bluetoothfilebrowser.service.file.BluetoothFileServiceImpl;
import ndphu.app.bluetoothfilebrowser.service.file.IFileService;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ndphu.bluetooth.filebrowser.R;

public class DownloadFileDialog extends DialogFragment implements IDownloadListener {

	private BluetoothDevice mBluetoothDevice;
	private FileObject mFileObject;
	private IFileService mFileService;

	private TextView mFrom;
	private TextView mTo;
	private ProgressBar mProgressBar;
	private TextView mDownloadProgressText;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setTitle("Downloading File");

		View view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_download_file, null);

		mFrom = (TextView) view.findViewById(R.id.dialog_download_file_textview_from);
		mTo = (TextView) view.findViewById(R.id.dialog_download_file_textview_to);
		mProgressBar = (ProgressBar) view.findViewById(R.id.dialog_download_file_progressbar);
		mDownloadProgressText = (TextView) view.findViewById(R.id.dialog_download_file_textview_download_progress);

		mFrom.setText("From: " + mBluetoothDevice.getName() + "::" + mFileObject.getPath());
		mTo.setText("To: " + MainActivity.DEFAULT_DOWNLOAD_DIR + "/" + mFileObject.getName());
		mProgressBar.setMax(100);

		builder.setView(view);

		builder.setPositiveButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mFileService.downloadFile(DownloadFileDialog.this.mFileObject, DownloadFileDialog.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		return builder.create();
	}

	public void setFileObject(FileObject fileObject) {
		this.mFileObject = fileObject;
	}

	public void setBluetoothDevice(BluetoothDevice device) {
		mBluetoothDevice = device;
		mFileService = new BluetoothFileServiceImpl(mBluetoothDevice);
	}

	@Override
	public void onUpdateSize(final int size) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				double percent = size * 100f / mFileObject.getSize();
				mProgressBar.setProgress((int) percent);
				mDownloadProgressText.setText(size + "/" + mFileObject.getSize());
			}
		});
	}

	@Override
	public void onCompleted() {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new AlertDialog.Builder(getActivity()).setTitle("File Downloaded")
						.setMessage("File downloaded at " + MainActivity.DEFAULT_DOWNLOAD_DIR + "/" + mFileObject.getName())
						.setPositiveButton("Close", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								DownloadFileDialog.this.dismiss();
							}
						}).create().show();
			}
		});

	}

	@Override
	public void onError(final Exception ex) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new AlertDialog.Builder(getActivity()).setTitle("Error").setMessage(ex.getMessage())
						.setPositiveButton("Close", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								DownloadFileDialog.this.dismiss();
							}
						}).create().show();
			}
		});

	}

}
