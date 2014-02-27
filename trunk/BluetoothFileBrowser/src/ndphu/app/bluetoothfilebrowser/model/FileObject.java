package ndphu.app.bluetoothfilebrowser.model;

public class FileObject {

	public static final int TYPE_DIRECTORY = 0;
	public static final int TYPE_FILE = 1;

	private String mName;
	private String mPath;
	private int mType;
	private long mSize;

	public FileObject(String name, String path, int type, long size) {
		mName = name;
		mPath = path;
		mType = type;
		mSize = size;
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

	public long getSize() {
		return mSize;
	}

}
