package ndphu.app.bluetoothfilebrowser.model;

public class AbstractFileObject {

	public static final int TYPE_DIRECTORY = 0;
	public static final int TYPE_FILE = 1;

	private String mName;
	private String mPath;
	private int mType;

	public AbstractFileObject(String name, String path, int type) {
		mName = name;
		mPath = path;
		mType = type;
	}

	public String getName() {
		return mName;
	}

	public String getPath() {
		return mPath;
	}

	public int getType() {
		return mType;
	}

}
