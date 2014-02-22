package ndphu.app.launcher.halo.list.adapter;

import java.util.Arrays;
import java.util.List;

import ndphu.app.launcher.halo.R;
import ndphu.app.launcher.halo.activity.MainActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationAdapter extends ArrayAdapter<ResolveInfo>{

	protected LayoutInflater mInflater;
	protected PackageManager mPM;
	private SharedPreferences mPref;
	private List<String> selectedAppSet = null;
	private boolean mIsShowCheckBox = true;
	
	public void setShowCheckBox(boolean show) {
		mIsShowCheckBox = show;
	}
	
	public ApplicationAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPM = context.getPackageManager();
		mPref = context.getSharedPreferences(MainActivity.PREF_SELECTED_APPS, Context.MODE_PRIVATE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_item_application, null);
		}
		ViewHolder holder = getViewHolder(convertView);
		
		ResolveInfo info = getItem(position);
		
		holder.name.setText(info.activityInfo.applicationInfo.loadLabel(mPM));
		holder.icon.setImageDrawable(info.activityInfo.applicationInfo.loadIcon(mPM));
		if (mIsShowCheckBox) {
			if (selectedAppSet != null && selectedAppSet.contains(info.activityInfo.packageName)) {
				holder.checked.setChecked(true);
			} else {
				holder.checked.setChecked(false);
			}
		}
		return convertView;
	}
	
	private ViewHolder getViewHolder(View view) {
		if (view.getTag() != null && view.getTag() instanceof ViewHolder) {
			return (ViewHolder) view.getTag();
		} else {
			ViewHolder holder = new ViewHolder();
			holder.name = (TextView) view.findViewById(R.id.listview_item_application_name);
			holder.icon = (ImageView) view.findViewById(R.id.listview_item_application_icon);
			holder.checked = (CheckBox) view.findViewById(R.id.listview_item_application_checked);
			if (mIsShowCheckBox) {
				holder.checked.setVisibility(View.VISIBLE);
			} else {
				holder.checked.setVisibility(View.GONE);
			}
			view.setTag(holder);
			return holder;
		}
	}
	
	private static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public CheckBox checked;
	}

	@Override
	public void notifyDataSetChanged() {
		if (mIsShowCheckBox) {
			String prefStr = mPref.getString(MainActivity.PREF_SELECTED_APPS_KEY, "");
			selectedAppSet = Arrays.asList(prefStr.split(";"));
		}
		super.notifyDataSetChanged();
	}
	
}
