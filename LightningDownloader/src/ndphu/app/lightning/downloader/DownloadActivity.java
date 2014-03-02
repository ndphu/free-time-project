package ndphu.app.lightning.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadActivity extends Activity {
	private static final String PART_FILE_NAME_FORMAT = "%s.part.%s";
	private static final String TAG = DownloadActivity.class.getName();
	private String mDataString;
	private Uri mDataUri;
	private HttpClient mHttpClient;

	private TextView mTextViewFrom;
	private TextView mTextViewTo;
	// private ProgressBar mProgressBar;
	private TextView mTextViewFileSize;
	private TextView mTextViewStatus;
	private long mFileSize = -1;
	private String mDestFilePath;

	private Map<Integer, Integer> mProgressMap = new HashMap<Integer, Integer>();
	private LinearLayout mProgressBarContainer;

	private List<Integer> mFinishedPartCounter = new ArrayList<Integer>();

	long mStartTime;
	long mEndTime;
	long mMaxPartDownloadDuration = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);

		Commons.DEFAULT_DOWNLOAD_DIR = Environment.getExternalStorageDirectory() + "/LightningDownloader";
		File f = new File(Commons.DEFAULT_DOWNLOAD_DIR);
		if (!f.isDirectory()) {
			f.mkdir();
		}

		mTextViewFrom = (TextView) findViewById(R.id.activity_download_file_textview_from);
		mTextViewTo = (TextView) findViewById(R.id.activity_download_file_textview_to);
		mTextViewFileSize = (TextView) findViewById(R.id.activity_download_file_textview_file_size);
		mTextViewStatus = (TextView) findViewById(R.id.activity_download_file_textview_status);
		mProgressBarContainer = (LinearLayout) findViewById(R.id.activity_download_container_progress_bars);
		initProgressBars();

		// mProgressBar = (ProgressBar) findViewById(R.id.activity_download_file_progressbar);

		Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			mDataUri = intent.getData();
			mDataString = intent.getDataString();
		} else {
			mDataString = "http://fs1.d-h.st/download/00103/TxS/4.4%20Blue%20Quick%20Settings%20Toggle%20Icons.zip";
			mDataUri = Uri.parse(mDataString);
		}

		Log.i(TAG, "Downoad file: " + mDataString);
		mTextViewFrom.setText("From: " + mDataString);
		mDestFilePath = Commons.DEFAULT_DOWNLOAD_DIR + "/" + mDataUri.getLastPathSegment();
		mTextViewTo.setText("To: " + mDestFilePath);
		// mProgressBar.setMax(100);

		startDownload();
	}

	private void initProgressBars() {
		for (int i = 0; i < Commons.THREAD_COUNT; ++i) {
			ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
			progressBar.setMax(100);
			mProgressBarContainer.addView(progressBar);
		}
	}

	private void startDownload() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpHead head = new HttpHead(mDataString);
				mHttpClient = new DefaultHttpClient();
				setStatusText("Getting file size...");
				try {
					HttpResponse headResponse = mHttpClient.execute(head);
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
					int partSize = (int) (mFileSize / Commons.THREAD_COUNT);
					for (int i = 0; i < Commons.THREAD_COUNT; ++i) {
						int start = partSize * i;
						int end = partSize * (i + 1) - 1;
						if (i < Commons.THREAD_COUNT - 1) {
							downloadPart(i, start, end);
						} else {
							// Last part
							downloadPart(i, start, mFileSize - 1);
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void setStatusText(final String status) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mTextViewStatus.setText(status);
			}
		});
	}

	private void updateProgressBar() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (mProgressMap) {
					for (Entry<Integer, Integer> entry : mProgressMap.entrySet()) {
						int threadIndex = entry.getKey();
						((ProgressBar) mProgressBarContainer.getChildAt(threadIndex)).setProgress(entry.getValue());
					}
				}
			}
		});
	}

	private void downloadPart(final int partIndex, final long start, final long end) {
		Log.i(TAG, "download part: " + partIndex + "; start = " + start + "; end = " + end);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpGet httpGet = new HttpGet(mDataString);
					httpGet.addHeader("Range", "bytes=" + start + "- " + end);
					HttpResponse getResponse = new DefaultHttpClient().execute(httpGet);
					InputStream inputStream = getResponse.getEntity().getContent();

					// Create file to store data
					File file = new File(String.format(PART_FILE_NAME_FORMAT, mDestFilePath, partIndex));
					if (!file.isFile()) {
						file.createNewFile();
					}
					FileOutputStream outputStream = new FileOutputStream(file);
					int read = 0;
					long total = 0;
					long sizeToDownload = end - start + 1;
					byte[] buffer = new byte[Commons.BUFFER_SIZE];
					long startTime = System.currentTimeMillis();
					while ((read = inputStream.read(buffer)) > 0) {
						Log.d(TAG, "Part" + partIndex + "::Read: " + read);
						total += read;
						Log.d(TAG, "Part" + partIndex + "::Total: " + total);
						outputStream.write(buffer, 0, read);
						outputStream.flush();
						int percent = (int) (total * 100 / sizeToDownload);
						synchronized (mProgressMap) {
							mProgressMap.put(partIndex, percent);
						}
						updateProgressBar();
						if (total == sizeToDownload) {
							notifyPartFinished(partIndex, startTime, System.currentTimeMillis());
							break;
						}
					}
					outputStream.close();
					inputStream.close();
				} catch (Exception ex) {

				}
			}
		}).start();
	}

	protected void notifyPartFinished(int partIndex, long startTime, long endTime) {
		synchronized (mFinishedPartCounter) {
			mFinishedPartCounter.add(partIndex);
			long duration = endTime - startTime;
			Log.i(TAG, "Part + " + partIndex + " finished in " + duration + " ms");
			if (duration > mMaxPartDownloadDuration) {
				mMaxPartDownloadDuration = duration;
			}
			if (mFinishedPartCounter.size() == Commons.THREAD_COUNT) {
				// Completed
				setStatusText("All part are downloaded in " + mMaxPartDownloadDuration + " ms. Start to join file.");
				joinParts();
			}
		}
	}

	private void joinParts() {
		File resultFile = new File(mDestFilePath);
		FileOutputStream out = null;
		try {
			if (!resultFile.isFile()) {
				resultFile.createNewFile();
			}
			out = new FileOutputStream(resultFile);
			long resultSize = 0;
			// join to final file
			for (int i = 0; i < Commons.THREAD_COUNT; ++i) {
				String partFileName = String.format(DownloadActivity.PART_FILE_NAME_FORMAT, mDestFilePath, i);
				File partFile = new File(partFileName);
				long partSize = partFile.length();
				Log.i(TAG, "Joining file " + partFileName);
				InputStream partInputStream = new FileInputStream(partFileName);
				int read = 0;
				int total = 0;
				byte[] joinBuffer = new byte[Commons.JOIN_FILE_BUFFER_SIZE];
				while ((read = partInputStream.read(joinBuffer)) > 0) {
					out.write(joinBuffer, 0, read);
					out.flush();
					total += read;
					if (total == partSize) {
						break;
					}
				}
				if (total != partSize) {
					throw new IOException("Invalid part file size : " + partFileName);
				}
				resultSize += total;
				partInputStream.close();
				partFile.delete();
			}

			if (resultSize == mFileSize) {
				setStatusText("Download completed");
			} else {
				throw new IOException("Invalid result file size");
			}
		} catch (IOException e) {
			e.printStackTrace();
			setStatusText(e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ignore) {
					// ignore
				}
			}
		}

	}
}
