package ndphu.app.lightning.downloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ndphu.app.lightning.downloader.utils.Utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends Activity {
	private static final String TAG = DownloadActivity.class.getName();
	private String mDataString;
	private Uri mDataUri;
	private TextView mTextViewFrom;
	private TextView mTextViewTo;
	private ProgressBar mProgressBar;
	private TextView mDownloadProgressText;
	private TextView mTextViewFileSize;
	private TextView mTextViewStatus;
	private long mFileSize = -1;
	private String mDestFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		mTextViewFrom = (TextView) findViewById(R.id.activity_download_file_textview_from);
		mTextViewTo = (TextView) findViewById(R.id.activity_download_file_textview_to);
		mTextViewFileSize = (TextView) findViewById(R.id.activity_download_file_textview_file_size);
		mTextViewStatus = (TextView) findViewById(R.id.activity_download_file_textview_status);
		mProgressBar = (ProgressBar) findViewById(R.id.activity_download_file_progressbar);
		mDownloadProgressText = (TextView) findViewById(R.id.activity_download_file_textview_download_progress);

		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			mDataUri = intent.getData();
			mDataString = intent.getDataString();
		} else {
			mDataString = "http://fs1.d-h.st/download/00102/xWF/NStore0.5.apk";
			mDataUri = Uri.parse(mDataString);
		}

		Log.i(TAG, "Downoad file: " + mDataString);

		Commons.DEFAULT_DOWNLOAD_DIR = Environment.getExternalStorageDirectory() + "/LightningDownloader";
		File f = new File(Commons.DEFAULT_DOWNLOAD_DIR);
		if (!f.isDirectory()) {
			f.mkdir();
		}

		mTextViewFrom.setText("From: " + mDataString);
		mDestFilePath = Commons.DEFAULT_DOWNLOAD_DIR + "/" + mDataUri.getLastPathSegment();
		mTextViewTo.setText("To: " + mDestFilePath);
		mProgressBar.setMax(100);

		startDownload();
	}

	private void startDownload() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpHead head = new HttpHead(mDataString);
				HttpClient httpClient = new DefaultHttpClient();
				setStatusText("Getting file size...");
				try {
					HttpResponse headResponse = httpClient.execute(head);
					Header[] allHeaders = headResponse.getAllHeaders();
					for (Header header : allHeaders) {
						Log.i(TAG, header.getName() + ": " + header.getValue());
						if ("Content-Length".equals(header.getName())) {
							mFileSize = Long.valueOf(header.getValue());
						}
					}
					if (mFileSize >= 0) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mTextViewFileSize.setText(Utils.size(mFileSize));
							}
						});
					} else {
						setStatusText("Fail to file size...");
						throw new FileNotFoundException("Cannot get file at: " + mDataString);
					}
					setStatusText("Downloading file...");
					HttpGet httpGet = new HttpGet(mDataString);
					HttpResponse getResponse = httpClient.execute(httpGet);
					InputStream inputStream = getResponse.getEntity().getContent();

					// Create file to store data
					File file = new File(mDestFilePath);
					if (!file.isFile()) {
						file.createNewFile();
					}
					FileOutputStream outputStream = new FileOutputStream(file);
					int read = 0;
					long total = 0;
					byte[] buffer = new byte[Commons.BUFFER_SIZE];
					while ((read = inputStream.read(buffer)) > 0) {
						Log.d(TAG, "Read: " + read);
						total += read;
						Log.d(TAG, "Total: " + total);
						outputStream.write(buffer, 0, read);
						outputStream.flush();
						int percent = (int) (total * 100 / mFileSize);
						updatePercent(percent);
						if (total == mFileSize) {
							break;
						}
					}
					setStatusText("Download finished!");
					outputStream.close();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			private void setStatusText(final String status) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mTextViewStatus.setText(status);
					}
				});
			}

			private void updatePercent(final int percent) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mProgressBar.setProgress(percent);
					}
				});
			}
		}).start();
	}
}
