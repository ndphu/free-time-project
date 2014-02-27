package ndphu.app.bluetoothfilebrowser.command.impl;

import ndphu.app.bluetoothfilebrowser.command.Command;

public class GetPropertiesCommand extends Command {

	public static final String COMMAND_PREFIX = "GET_PROPERTIES";
	private String mPath = null;

	public GetPropertiesCommand(String path) {
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
