package ndphu.app.launcher.halo.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
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
		XposedBridge.log("" + AndroidAppHelper.currentPackageName());
		// Read from file
		XposedBridge.log("Load Package: " + lpparam.packageName);
		XposedBridge.log("" + XposedInit.PACKAGE_NAME_LIST.size());
		if (lpparam.packageName.equals(XposedInit.DEFAULT_LAUNCHER)) {
			XposedBridge.log("Hooking the default launcher: " + XposedInit.DEFAULT_LAUNCHER);
			XposedInit.initPackageNameList();
			hookActiviyClassToApplyHalo(lpparam);
		} else if (XposedInit.PACKAGE_NAME_LIST.contains(lpparam.packageName)) {
			XposedBridge.log("Found package which is configured to run in Halo:" + lpparam.packageName);
			hookActiviyClassToApplyHalo(lpparam);
		}
	}

	private void hookActiviyClassToApplyHalo(LoadPackageParam lpparam) {
		findAndHookMethod("android.app.Activity", lpparam.classLoader, "startActivityForResult", "android.content.Intent", "int",
				"android.os.Bundle", new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						Intent intent = (Intent) param.args[0];

						XposedBridge.log("Got intent:" + intent);
						XposedBridge.log("Package Name:" + intent.getComponent().getPackageName());
						XposedBridge.log("Package List:" + XposedInit.PACKAGE_NAME_LIST.size());

						if (XposedInit.PACKAGE_NAME_LIST.contains(intent.getComponent().getPackageName())) {
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
		prefs = new XSharedPreferences(Common.MY_PACKAGE_NAME, Common.PREFS);
		prefs.makeWorldReadable();
	}

	public static boolean isActive(String packageName) {
		return prefs.getBoolean(packageName + Common.PREF_ACTIVE, false);
	}

	public static boolean isActive(String packageName, String sub) {
		return prefs.getBoolean(packageName + Common.PREF_ACTIVE, false) && prefs.getBoolean(packageName + sub, false);
	}

	@Override
	public void initZygote(de.robv.android.xposed.IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
	}

	@Override
	public void initCmdApp(de.robv.android.xposed.IXposedHookCmdInit.StartupParam startupParam) throws Throwable {

	}

}
