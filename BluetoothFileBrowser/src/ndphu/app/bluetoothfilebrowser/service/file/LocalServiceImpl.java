package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ndphu.app.bluetoothfilebrowser.model.FileObject;
import ndphu.app.bluetoothfilebrowser.server.IDownloadListener;

import org.apache.http.MethodNotSupportedException;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalServiceImpl implements IFileService {

	public LocalServiceImpl() {

	}

	@Override
	public String getServerName() {
		return "Local Server";
	}

	@Override
	public List<FileObject> listFile(String path) throws Exception {
		try {
			File dir = new File(path);
			if (!dir.isDirectory()) {
				throw new RuntimeException(dir + " is not a directory");
			}

			File[] files = dir.listFiles();
			if (files == null) {
				return new ArrayList<FileObject>();
			}

			List<FileObject> result = new ArrayList<FileObject>();
			long size = 0;
			for (File file : files) {
				int type = -1;
				if (file.isDirectory()) {
					type = FileObject.TYPE_DIRECTORY;
					size = file.list().length;
				} else {
					type = FileObject.TYPE_FILE;
					size = file.length();
				}
				result.add(new FileObject(file.getName(), file.getAbsolutePath(), type, size));
			}
			// Sorting result
			Collections.sort(result, new Comparator<FileObject>() {

				@Override
				public int compare(FileObject lhs, FileObject rhs) {
					if (lhs.getType() < rhs.getType()) {
						return -1;
					}
					if (lhs.getType() > rhs.getType()) {
						return 1;
					}
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}

			});
			return result;
		} catch (Exception ex) {
			throw ex;
		}
	}

	public JSONObject getFileProperties(String path) throws JSONException {
		JSONObject result = new JSONObject();

		File f = new File(path);
		result.put("name", f.getName());
		result.put("path", f.getAbsolutePath());
		// TODO: Add more info
		return result;
	}

	@Override
	public File downloadFile(FileObject fileObject, IDownloadListener listener) {
		return null;
	}

}
