package ndphu.app.exposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Tutorial implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XposedBridge.log("Load package: " + lpparam.packageName);
		if (!lpparam.packageName.equals("com.android.systemui"))
			return;

		findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// this will be called before the clock was updated by the original method
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// this will be called after the clock was updated by the original method
				TextView tv = (TextView) param.thisObject;
				String text = tv.getText().toString();
				tv.setText(text + " :)");
			}
		});
	}

}
