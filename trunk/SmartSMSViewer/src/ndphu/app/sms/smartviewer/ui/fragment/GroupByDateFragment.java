package ndphu.app.sms.smartviewer.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ndphu.app.sms.smartviewer.R;
import ndphu.app.sms.smartviewer.model.Contact;
import ndphu.app.sms.smartviewer.model.SMS;
import ndphu.app.sms.smartviewer.utils.Utils;
import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class GroupByDateFragment extends ListFragment implements OnItemClickListener {

	private OnDateSelectedListener mListener;
	private Contact mContact;
	private List<SMS> mSentList = new ArrayList<SMS>();
	private List<SMS> mDisplayList = new ArrayList<SMS>();
	private Map<String, List<SMS>> mGroupByDate = new TreeMap<String, List<SMS>>();
	private boolean mIsLoaded = false;
	private boolean mIsViewCreated = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				prepareList();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mIsLoaded = true;
				populateResult();
			}
		}.execute();
	}

	private void populateResult() {
		if (mIsViewCreated) {
			getListView().setOnItemClickListener(GroupByDateFragment.this);
			setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.list_item_date, mGroupByDate.keySet().toArray(new String[] {})));
			setListShown(true);
		}
	}

	private void prepareList() {
		String threadId = mContact.getSmsList().get(0).getThreadId();
		Cursor sentSMSes = getActivity().getContentResolver().query(Uri.parse("content://sms/sent"), null, " thread_id = ? ",
				new String[] { threadId }, null);
		if (sentSMSes.moveToFirst()) {
			for (int i = 0; i < sentSMSes.getCount(); ++i) {
				SMS sms = SMS.parseFromCursor(sentSMSes);
				mSentList.add(sms);
				sentSMSes.moveToNext();
			}
		}
		mDisplayList.addAll(mSentList);
		mDisplayList.addAll(mContact.getSmsList());

		Collections.sort(mDisplayList, new Comparator<SMS>() {

			@Override
			public int compare(SMS lhs, SMS rhs) {
				return lhs.getDate().compareTo(rhs.getDate());
			}
		});

		for (SMS sms : mDisplayList) {
			// mMessageAdapter.add(sms);
			String date = Utils.getDate(Long.valueOf(sms.getDate()), "yyyy-MM-dd");
			if (mGroupByDate.containsKey(date)) {
				List<SMS> existingList = mGroupByDate.get(date);
				existingList.add(sms);
			} else {
				List<SMS> newList = new ArrayList<SMS>();
				newList.add(sms);
				mGroupByDate.put(date, newList);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mIsViewCreated = true;
		if (mIsLoaded) {
			populateResult();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = (OnDateSelectedListener) activity;
	}

	public void setContact(Contact contact) {
		mContact = contact;
	}

	public interface OnDateSelectedListener {
		void onDateClick(String date, List<SMS> listToDisplay);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		String date = (String) parent.getAdapter().getItem(position);
		mListener.onDateClick(date, mGroupByDate.get(date));
	}
}
