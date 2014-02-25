package ndphu.app.bluetoothchat.dialog;

import ndphu.app.bluetoothchat.R;
import ndphu.app.bluetoothchat.model.Client;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class MessengerDialog extends DialogFragment {

	private Client mClient;
	private ListView mMessageListView;
	private EditText mMessageContent;
	private ImageButton mSendButton;
	private OnClickListener mOnSendButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... params) {
					mClient.getWriter().println(mMessageContent.getText().toString());
					return null;
				}

			}.execute();
		}
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle(mClient.getClientName());

		View view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_messenger, null);
		mSendButton = (ImageButton) view.findViewById(R.id.dialog_messenger_button_send);
		mSendButton.setOnClickListener(mOnSendButtonClick);
		mMessageListView = (ListView) view.findViewById(R.id.dialog_messenger_listview_messages);
		mMessageContent = (EditText) view.findViewById(R.id.dialog_messenger_textview_message_content);

		startClientThread();

		builder.setView(view);

		return builder.create();
	}

	protected void startClientThread() {
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

	public void setClient(Client client) {
		mClient = client;
	}
}
