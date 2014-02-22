package ndphu.app.exposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.Calendar;

import android.widget.TextView;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HaloModifier implements IXposedHookLoadPackage{

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XposedBridge.log("Load package: " + lpparam.packageName);
		if (!lpparam.packageName.equals("com.android.systemui"))
			return;

		findAndHookMethod("com.android.systemui.statusbar.policy.DateView", lpparam.classLoader, "updateClock", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// this will be called before the clock was updated by the
				// original method
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// this will be called after the clock was updated by the
				// original method
				TextView tv = (TextView) param.thisObject;
				String text = tv.getText().toString();
				String[] splitted = text.split("-");
				Calendar today = Calendar.getInstance();
				int day = today.get(Calendar.DAY_OF_MONTH);
				int month = today.get(Calendar.MONTH) + 1;
				int year = today.get(Calendar.YEAR);
				int jd = VietCalendar.jdFromDate(day, month, year);
				int[] s = VietCalendar.jdToDate(jd);
				int[] l = VietCalendar.convertSolar2Lunar(s[0], s[1], s[2], 7.0);
				String _day = String.valueOf(l[0]);
				String _month = String.valueOf(l[1]);
				tv.setText(splitted[0].trim() + " - " + _day + "/" + _month);
			}
		});
	}

}
