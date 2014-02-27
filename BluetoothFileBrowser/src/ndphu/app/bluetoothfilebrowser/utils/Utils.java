package ndphu.app.bluetoothfilebrowser.utils;

import java.text.DecimalFormat;

public class Utils {
	public static String size(long size) {
		String hrSize = "";
		double m = size / 1024.0;
		double m2 = m / 1024.0;
		DecimalFormat dec = new DecimalFormat("0.00");

		if (m2 > 1) {
			hrSize = dec.format(m2).concat(" MB");
		} else if (m > 1) {
			hrSize = dec.format(m).concat(" KB");
		} else {
			hrSize = size + " B";
		}
		return hrSize;
	}
}
