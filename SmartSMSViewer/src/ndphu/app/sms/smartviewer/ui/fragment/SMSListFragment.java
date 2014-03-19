package ndphu.app.sms.smartviewer.ui.fragment;

import java.util.List;

import ndphu.app.sms.smartviewer.R;
import ndphu.app.sms.smartviewer.model.SMS;
import ndphu.app.sms.smartviewer.ui.list.MessageAdapter;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class SMSListFragment extends ListFragment {
	private List<SMS> mListToDisplay;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setBackgroundColor(Color.WHITE);
		MessageAdapter adapter = new MessageAdapter(getActivity(), 0);
		setListAdapter(adapter);
		adapter.addAll(mListToDisplay);
		setListShown(true);
		getListView().setDivider(this.getResources().getDrawable(R.drawable.transperent_color));
	}

	public void setListToDisplay(List<SMS> listToDisplay) {
		mListToDisplay = listToDisplay;
	}
}
