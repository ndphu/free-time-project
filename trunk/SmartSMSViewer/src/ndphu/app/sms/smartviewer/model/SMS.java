package ndphu.app.sms.smartviewer.model;

import android.database.Cursor;

public class SMS {
	private String mAddress;
	private String mPerson;
	private String mDate;
	private String mProtocol;
	private String mRead;
	private String mStatus;
	private String mType;
	private String mSubject;
	private String mBody;
	private String mThreadId;

	private SMS() {

	}

	public static SMS parseFromCursor(Cursor cursor) {
		SMS sms = new SMS();
		sms.mAddress = cursor.getString(cursor.getColumnIndex("address"));
		sms.mPerson = cursor.getString(cursor.getColumnIndex("person"));
		sms.mDate = cursor.getString(cursor.getColumnIndex("date"));
		sms.mProtocol = cursor.getString(cursor.getColumnIndex("protocol"));
		sms.mRead = cursor.getString(cursor.getColumnIndex("read"));
		sms.mStatus = cursor.getString(cursor.getColumnIndex("status"));
		sms.mType = cursor.getString(cursor.getColumnIndex("type"));
		sms.mSubject = cursor.getString(cursor.getColumnIndex("subject"));
		sms.mBody = cursor.getString(cursor.getColumnIndex("body"));
		sms.mThreadId = cursor.getString(cursor.getColumnIndex("thread_id"));

		System.out.println("address: " + sms.mAddress + "; person: " + sms.mPerson + "; date: " + sms.mDate + "; protocol: " + sms.mProtocol
				+ "; read: " + sms.mRead + "; status: " + sms.mStatus + "; type: " + sms.mType + "; subject: " + sms.mSubject + "; body: "
				+ sms.mBody);

		return sms;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		this.mAddress = address;
	}

	public String getPerson() {
		return mPerson;
	}

	public void setPerson(String person) {
		this.mPerson = person;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		this.mDate = date;
	}

	public String getProtocol() {
		return mProtocol;
	}

	public void setProtocol(String protocol) {
		this.mProtocol = protocol;
	}

	public String getRead() {
		return mRead;
	}

	public void setRead(String read) {
		this.mRead = read;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		this.mStatus = status;
	}

	public String getType() {
		return mType;
	}

	public void setType(String type) {
		this.mType = type;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(String subject) {
		this.mSubject = subject;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String body) {
		this.mBody = body;
	}

	public String getThreadId() {
		return mThreadId;
	}

	public void setThreadId(String threadId) {
		this.mThreadId = threadId;
	}
}
