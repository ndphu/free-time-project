package ndphu.app.launcher.halo.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import ndphu.app.launcher.halo.Common;
import ndphu.app.launcher.halo.activity.MainActivity;
import android.app.AndroidAppHelper;
import android.content.Intent;
import de.robv.android.xposed.IXposedHookCmdInit;
import de.robv.android.xposed.IXposedHookCmdInit.StartupParam;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedHaloModifier implements IXposedHookLoadPackage, IXposedHookCmdInit, IXposedHookZygoteInit {
	public static XSharedPreferences prefs;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		prefs.reload();
		XposedBridge.log("Load Package: " + lpparam.packageName);
		
		if (lpparam.packageName.equals(Common.DEFAULT_LAUNCHER)) {
			XposedBridge.log("Hooking the default launcher: " + Common.DEFAULT_LAUNCHER);
			hookActiviyClassToApplyHalo(lpparam);
		} else if (isActive(lpparam.packageName)) {
			XposedBridge.log("Found package which is configured to run in Halo:" + lpparam.packageName);
			hookActiviyClassToApplyHalo(lpparam);
		}
	}

	private void hookActiviyClassToApplyHalo(LoadPackageParam lpparam) {
		findAndHookMethod("android.app.Activity", lpparam.classLoader, "startActivityForResult", "android.content.Intent", "int",
				"android.os.Bundle", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						loadPrefs();
						Intent intent = (Intent) param.args[0];

						XposedBridge.log("Got intent:" + intent);
						XposedBridge.log("Package Name:" + intent.getComponent().getPackageName());
						
						String packageName = intent.getComponent().getPackageName();
						if (isActive(packageName)) {
							XposedBridge.log("Launch in HALO mode: " + intent.getPackage());
							intent.setFlags(intent.getFlags() | 0x00002000);
						}
					}

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					}
				});
	}

	public static void loadPrefs() {
		prefs = new XSharedPreferences(MainActivity.prefsFile);
	}

	public static boolean isActive(String packageName) {
		return prefs.getBoolean(packageName + Common.PREF_ACTIVE, false);
	}

	public static boolean isActive(String packageName, String sub) {
		return prefs.getBoolean(packageName + Common.PREF_ACTIVE, false) && prefs.getBoolean(packageName + sub, false);
	}

	@Override
	public void initZygote(de.robv.android.xposed.IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
		loadPrefs();
	}

	@Override
	public void initCmdApp(de.robv.android.xposed.IXposedHookCmdInit.StartupParam startupParam) throws Throwable {
		prefs.reload();
	}

}
