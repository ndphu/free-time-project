package ndphu.app.bluetoothfilebrowser.command.impl;

import ndphu.app.bluetoothfilebrowser.command.Command;

public class ListFileCommand extends Command {
	public static final String COMMAND_PREFIX = "LIST_FILE";

	private String mPath = null;

	public ListFileCommand(String path) {
		mPath = path;
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
