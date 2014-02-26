package ndphu.app.bluetoothfilebrowser.service.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;

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
	public List<AbstractFileObject> listFile(String path) throws Exception {
		try {
			File dir = new File(path);
			if (!dir.isDirectory()) {
				throw new RuntimeException(dir + " is not a directory");
			}

			File[] files = dir.listFiles();
			if (files == null) {
				return new ArrayList<AbstractFileObject>();
			}

			List<AbstractFileObject> result = new ArrayList<AbstractFileObject>();

			for (File file : files) {
				int type = -1;
				if (file.isDirectory()) {
					type = AbstractFileObject.TYPE_DIRECTORY;
				} else {
					type = AbstractFileObject.TYPE_FILE;
				}
				result.add(new AbstractFileObject(file.getName(), file.getAbsolutePath(), type));
			}
			// Sorting result
			Collections.sort(result, new Comparator<AbstractFileObject>() {

				@Override
				public int compare(AbstractFileObject lhs, AbstractFileObject rhs) {
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
	public File downloadFile(AbstractFileObject fileObject, String destDir) {
		return null;
	}

}
