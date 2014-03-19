package ndphu.app.sms.smartviewer.service;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.sms.smartviewer.model.Contact;
import ndphu.app.sms.smartviewer.model.SMS;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class SMSService {
	private Context mContext;
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

	private List<Contact> mContactList = new ArrayList<Contact>();
	private List<Contact> mAllContact = new ArrayList<Contact>();

	public SMSService() {

	}

	public void init() {
		initContactListCache();

		mInboxCursor = mContext.getContentResolver().query(Uri.parse(Inbox), null, null, null, null);

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
		Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
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

	public void setContext(Context context) {
		mContext = context;
	}
	
	public List<Contact> getContactList() {
		return mContactList;
	}
}
