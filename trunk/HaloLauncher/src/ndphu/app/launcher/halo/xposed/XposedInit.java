package ndphu.app.launcher.halo.xposed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ndphu.app.launcher.halo.activity.MainActivity;
import de.robv.android.xposed.XposedBridge;

public class XposedInit {

	static String DEFAULT_LAUNCHER = "com.android.launcher3";

	static List<String> PACKAGE_NAME_LIST = new ArrayList<String>();

	public static void initPackageNameList() {
		synchronized (XposedInit.PACKAGE_NAME_LIST) {
			if (XposedInit.PACKAGE_NAME_LIST.size() != 0) {
				return;
			}
			File f = new File(MainActivity.SDCARD_DATA_FILE);
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String data = br.readLine();
				XposedBridge.log("Read from data file:" + data);
				String[] packageNameArr = data.split(";");
				for (String str : packageNameArr) {
					XposedInit.PACKAGE_NAME_LIST.add(str);
					XposedBridge.log(str);
				}
			} catch (IOException e) {
				XposedBridge.log(e.getMessage());
			}
		}
	}

}
