package com.ndphu.app.openthis.activity;

import ndphu.app.openthis.R;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ndphu.app.openthis.fragment.AccountSettingsFragment;
import com.ndphu.app.openthis.fragment.LinkListFragment;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private String[] menuItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private LinkListFragment currentFragment;
	private boolean isInitialized = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Invoke MainActivity");
		setContentView(R.layout.activity_main);
		mTitle = mDrawerTitle = getTitle();
		menuItems = new String[] { "My Links", "Shared Links", "Account Settings" };
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_2,
				android.R.id.text1, menuItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		initDrawerToggle();
		if (AppInfo.COOKIE == null || AppInfo.CURRENT_USER == null) {
			popupLoginRequireDialog();
		} else {
			initData();
		}
	}

	private void initData() {
		if (!isInitialized) {
			Toast.makeText(MainActivity.this, "Welcome " + AppInfo.CURRENT_USER.getEmail(), Toast.LENGTH_SHORT).show();
			selectItem(0);
			isInitialized = true;
		}
	}

	private void initDrawerToggle() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			if (currentFragment != null) {
				currentFragment.refresh();
			}
			return true;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_refresh).setVisible(
				!drawerOpen && currentFragment != null && currentFragment instanceof LinkListFragment);
		return super.onPrepareOptionsMenu(menu);
	}

	private class DrawerItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		currentFragment = null;
		// Create a new fragment and specify the planet to show based on
		// position
		switch (position) {
		case 0: {
			currentFragment = new LinkListFragment();
			currentFragment.setFragmentType(LinkListFragment.TYPE_MY_LINKS);
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
			break;
		}
		case 1: {
			currentFragment = new LinkListFragment();
			currentFragment.setFragmentType(LinkListFragment.TYPE_SHARED_LINKS);
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, currentFragment).commit();
			break;
		}
		case 2: {
			AccountSettingsFragment fragment = new AccountSettingsFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			break;
		}
		}
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(menuItems[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
		invalidateOptionsMenu();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isInitialized) {
			initData();
		}
	}

	private void popupLoginRequireDialog() {
		openLoginActivity();
		// new
		// AlertDialog.Builder(this).setTitle(R.string.notice).setMessage(R.string.msg_error_user_not_logged_in)
		// .setPositiveButton(R.string.login, new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// openLoginActivity();
		// }
		// }).setNegativeButton("Exit", new OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// MainActivity.this.finish();
		// }
		// }).create().show();
	}

	private void openLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

}
