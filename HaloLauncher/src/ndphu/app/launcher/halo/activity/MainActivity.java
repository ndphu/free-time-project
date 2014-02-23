package ndphu.app.launcher.halo.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ndphu.app.launcher.halo.R;
import ndphu.app.launcher.halo.fragment.dialog.SelectAppDialog;
import ndphu.app.launcher.halo.fragment.dialog.SelectAppDialog.OnDialogClosedListener;
import ndphu.app.launcher.halo.list.adapter.ApplicationAdapter;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnDialogClosedListener, OnItemClickListener{

	private FragmentManager mFM;
	private PackageManager mPM;
	private SharedPreferences mPref;
	
	public static final String PREF_SELECTED_APPS = "pref_selected_apps";
	public static final String PREF_SELECTED_APPS_KEY = "pref_selected_apps_key";
	
	public static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/halo_launcher";
	public static final String SDCARD_DATA_FILE = MainActivity.SDCARD_DIR + "/data";
	
	ListView mListView;
	ApplicationAdapter mAppAdapter;
	private List<ResolveInfo> mInstalledAppList;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFM = getFragmentManager();
		mPM = getPackageManager();
		mPref = getSharedPreferences(PREF_SELECTED_APPS, MODE_PRIVATE);
		
		mListView = (ListView) findViewById(R.id.activity_main_selected_app_listview);
		mAppAdapter = new ApplicationAdapter(this, 0);
		mAppAdapter.setShowCheckBox(false);
		mListView.setAdapter(mAppAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadInstalledAppList();
		loadSelectedAppListView();
	}

	private void loadInstalledAppList() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		mInstalledAppList = (ArrayList<ResolveInfo>) mPM.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
	}

	private void loadSelectedAppListView() {
		mAppAdapter.clear();
		String prefStr = mPref.getString(MainActivity.PREF_SELECTED_APPS_KEY, "");
		List<String> selectedApps = Arrays.asList(prefStr.split(";"));
		for (ResolveInfo info : mInstalledAppList) {
			if (selectedApps.contains(info.activityInfo.packageName)) {
				mAppAdapter.add(info);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			SelectAppDialog dialog = new SelectAppDialog();
			dialog.show(mFM, "action_add");
			return true;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onDialogClosed(Object data) {
		loadSelectedAppListView();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		ResolveInfo info = mAppAdapter.getItem(position);
		Intent intentForPackage = mPM.getLaunchIntentForPackage(info.activityInfo.packageName);
		// Set Flag to start in Halo/Muti-window
//		intentForPackage.setFlags(0x00002000);
		startActivity(intentForPackage);
	}

}
