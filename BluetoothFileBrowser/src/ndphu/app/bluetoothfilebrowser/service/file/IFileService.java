package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.File;
import java.util.List;

import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.server.IDownloadListener;

public interface IFileService {

	public String getServerName();

	public List<FileObject> listFile(String path) throws Exception;
	
	public File downloadFile(FileObject fileObject, IDownloadListener downloadListener) throws Exception;
	
}
