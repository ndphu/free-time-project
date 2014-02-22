package ndphu.app.launcher.halo.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.Calendar;

import ndphu.app.launcher.halo.activity.MainActivity;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedHaloModifier implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XposedBridge.log("Pref: " + lpparam.packageName);
		if (!lpparam.packageName.equals("android"))
			return;
		XposedBridge.log("We are here");
		
		findAndHookMethod("android.app.Activity", lpparam.classLoader, 
				"startActivityForResult", 
				"android.content.Intent", 
				"int", 
				"android.os.Bundle", 
				new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Intent intent = (Intent) param.args[0];
				
				XposedBridge.log("Got intent:" + intent);
				
				if (intent.getPackage().equals("ndphu.app.launcher.halo")) {
					intent.setFlags(intent.getFlags() | 0x00002000);
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			}
		});
	}

}
