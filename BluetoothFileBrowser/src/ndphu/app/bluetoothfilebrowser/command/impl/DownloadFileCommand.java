package ndphu.app.bluetoothfilebrowser.command.impl;

import ndphu.app.bluetoothfilebrowser.command.Command;
import ndphu.app.bluetoothfilebrowser.model.AbstractFileObject;

public class DownloadFileCommand extends Command {

	public static final String COMMAND_PREFIX = "DOWNLOAD_FILE";
	
	private String mPath = null;
	
	public DownloadFileCommand(AbstractFileObject object) {
		mPath = object.getPath();
	}

	@Override
	public String getCommandPrefix() {
		
		return COMMAND_PREFIX;
	}

	@Override
	public String getCommandContent() {
		return mPath;
	}

}
