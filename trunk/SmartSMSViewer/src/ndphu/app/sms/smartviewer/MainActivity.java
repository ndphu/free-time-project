package ndphu.app.sms.smartviewer;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.sms.smartviewer.model.Contact;
import ndphu.app.sms.smartviewer.model.SMS;
import ndphu.app.sms.smartviewer.ui.fragment.SMSListDialogFragment;
import ndphu.app.sms.smartviewer.ui.list.ContactAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {

	String Inbox = "content://sms/inbox";
	String Failed = "content://sms/failed";
	String Queued = "content://sms/queued";
	String Sent = "content://sms/sent";
	String Draft = "content://sms/draft";
	String Outbox = "content://sms/outbox";
	String Undelivered = "content://sms/undelivered";
	String All = "content://sms/all";
	String Conversations = "content://sms/conversations";
	private Cursor mInboxCursor;

	List<Contact> mContactList = new ArrayList<Contact>();
	List<Contact> mAllContact = new ArrayList<Contact>();

	private ListView mListView;
	private ContactAdapter mContactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mListView = (ListView) findViewById(R.id.main_activity_listview_contact);
		mListView.setOnItemClickListener(this);
		mContactAdapter = new ContactAdapter(this, 0);
		mListView.setAdapter(mContactAdapter);

		initContactListCache();

		mInboxCursor = getContentResolver().query(Uri.parse(Inbox), null, null, null, null);

		if (mInboxCursor.moveToFirst()) {
			for (int i = 0; i < mInboxCursor.getColumnCount(); i++) {
				Log.i("SMSTAG", mInboxCursor.getColumnName(i) + ": " + mInboxCursor.getString(i));
			}
			for (int i = 0; i < mInboxCursor.getCount(); ++i) {
				SMS sms = SMS.parseFromCursor(mInboxCursor);
				Contact contact = findContactByNumber(sms.getAddress());
				contact.getSmsList().add(sms);
				mInboxCursor.moveToNext();
			}
		}
	}

	private void initContactListCache() {
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
		while (phones.moveToNext()) {
			String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
			String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			phoneNumber = phoneNumber.replace("-", "").replace(" ", "");
			if (phoneNumber.startsWith("0")) {
				phoneNumber = phoneNumber.substring(1);
			}
			if (!phoneNumber.startsWith("+84")) {
				phoneNumber = "+84" + phoneNumber;
			}
			Contact contact = new Contact(id, name, phoneNumber);
			mAllContact.add(contact);
			Log.i("CONTACTSTAG", contact.getName() + ";" + contact.getNumber());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mContactAdapter.clear();
		for (Contact contact : mContactList) {
			mContactAdapter.add(contact);
		}
	}

	private Contact findContactByNumber(String number) {
		for (Contact contact : mContactList) {
			if (contact.getNumber().equals(number)) {
				return contact;
			}
		}
		// search in all contact list
		for (Contact contact : mAllContact) {
			if (contact.getNumber().equals(number)) {
				mContactList.add(contact);
				return contact;
			}
		}
		Contact unmanagedContact = new Contact("0", number, number);
		mContactList.add(unmanagedContact);
		return unmanagedContact;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Contact contact = mContactAdapter.getItem(position);
		SMSListDialogFragment dialogFragment = new SMSListDialogFragment();
		dialogFragment.setContact(contact);
		dialogFragment.show(getFragmentManager(), "SMS_LIST_FRAGMENT");
	}

}
