package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.File;
import java.util.List;

import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;

public interface IFileService {

	public String getServerName();

	public List<AbstractFileObject> listFile(String path) throws Exception;
	
	public File downloadFile(AbstractFileObject fileObject, String destDir) throws Exception;
	
}
