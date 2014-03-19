package ndphu.app.sms.smartviewer.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ndphu.app.sms.smartviewer.R;
import ndphu.app.sms.smartviewer.model.Contact;
import ndphu.app.sms.smartviewer.model.SMS;
import ndphu.app.sms.smartviewer.ui.list.MessageAdapter;
import android.app.DialogFragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SMSListDialogFragment extends DialogFragment {

	private Contact mContact;
	private ListView mSmsList;
	private MessageAdapter mMessageAdapter;
	
	private static final long MILISEC_OF_DAY = 86400000;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sms_list, container, false);
		mSmsList = (ListView) view.findViewById(R.id.fragment_sms_list_listview_sms);
		mMessageAdapter = new MessageAdapter(getActivity(), 0);
		mSmsList.setAdapter(mMessageAdapter);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		String threadId = mContact.getSmsList().get(0).getThreadId();
		Cursor sentSMSes = getActivity().getContentResolver().query(Uri.parse("content://sms/sent"), null, " thread_id = ? ", new String[]{threadId}, null);
		if (sentSMSes.moveToFirst()) {
			for (int i = 0; i < sentSMSes.getCount(); ++i) {
				SMS sms = SMS.parseFromCursor(sentSMSes);
				mContact.getSmsList().add(sms);
				sentSMSes.moveToNext();
			}
		}
		Collections.sort(mContact.getSmsList(), new Comparator<SMS>() {

			@Override
			public int compare(SMS lhs, SMS rhs) {
				return lhs.getDate().compareTo(rhs.getDate());
			}
		});
		
		SMS firstMessage = mContact.getSmsList().get(0);
		long firstDateLong = Long.valueOf(firstMessage.getDate());
		long firstAbsDate = firstDateLong / MILISEC_OF_DAY;
		
		SMS lastMessage = mContact.getSmsList().get(mContact.getSmsList().size() - 1);
		long lastDateLong = Long.valueOf(lastMessage.getDate());
		long lastAbsDate = lastDateLong / MILISEC_OF_DAY;
		
		
		for (SMS sms : mContact.getSmsList()) {
			mMessageAdapter.add(sms);
		}
		
	}

	public void setContact(Contact contact) {
		mContact = contact;
	}
}
