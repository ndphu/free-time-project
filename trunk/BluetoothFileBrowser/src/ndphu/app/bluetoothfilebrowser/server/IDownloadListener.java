package ndphu.app.bluetoothfilebrowser.server;

public interface IDownloadListener {
	
	void onUpdateSize(int size);
	
	void onCompleted();
	
	void onError(Exception ex);
	
}
