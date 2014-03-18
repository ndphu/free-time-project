package ndphu.app.sms.smartviewer.model;

import java.util.ArrayList;
import java.util.List;

public class Contact {
	private String mId = null;
	private String mName = null;
	private String mNumber = null;
	private List<SMS> mSmsList = null;

	public Contact(String id, String name, String number) {
		mId = id;
		mName = name;
		mNumber = number;
		setSmsList(new ArrayList<SMS>());
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		this.mNumber = number;
	}

	public List<SMS> getSmsList() {
		return mSmsList;
	}

	public void setSmsList(List<SMS> smses) {
		this.mSmsList = smses;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}
}
