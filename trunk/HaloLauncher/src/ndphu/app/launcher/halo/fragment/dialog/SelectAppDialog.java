package ndphu.app.launcher.halo.fragment.dialog;

import java.util.ArrayList;
import java.util.List;

import ndphu.app.launcher.halo.Common;
import ndphu.app.launcher.halo.R;
import ndphu.app.launcher.halo.activity.MainActivity;
import ndphu.app.launcher.halo.list.adapter.ApplicationAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SelectAppDialog extends DialogFragment implements OnItemClickListener {

	private ListView mAppList;
	private ApplicationAdapter mAppAdapter;
	private PackageManager mPM;
	private SharedPreferences mPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPM = getActivity().getPackageManager();
		mPref = getActivity().getSharedPreferences(MainActivity.PREF_SELECTED_APPS, Context.MODE_APPEND);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setPositiveButton("Done", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.setTitle("Select Applications");

		mAppList = new ListView(getActivity());
		mAppAdapter = new ApplicationAdapter(getActivity(), 0);
		mAppList.setAdapter(mAppAdapter);
		mAppList.setOnItemClickListener(this);

		loadInstalledApplications();

		builder.setView(mAppList);
		return builder.create();
	}

	private void loadInstalledApplications() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> list = (ArrayList<ResolveInfo>) mPM.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
		for (ResolveInfo info : list) {
			mAppAdapter.add(info);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		getActivity().getMenuInflater().inflate(R.menu.main, menu);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		ResolveInfo info = mAppAdapter.getItem(position);

		String packageName = info.activityInfo.packageName;
		String packageActiveKey = packageName + Common.PREF_ACTIVE;
		boolean isActive = mPref.getBoolean(packageActiveKey, false);
		mPref.edit().putBoolean(packageActiveKey, !isActive).commit();
		mAppAdapter.notifyDataSetChanged();
	}

	public interface OnDialogClosedListener {
		public void onDialogClosed(Object data);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (getActivity() instanceof OnDialogClosedListener) {
			((OnDialogClosedListener) getActivity()).onDialogClosed(null);
		}
	}

}