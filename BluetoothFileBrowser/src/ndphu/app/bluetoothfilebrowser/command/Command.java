package ndphu.app.bluetoothfilebrowser.command;

public abstract class Command {

	public String getCommandString() {
		return getCommandPrefix() + ":" + getCommandContent();
	}

	public String parseResult(String rawResult) {
		String prefix = rawResult.substring(0, rawResult.indexOf(":"));
		if (prefix.equals(getCommandPrefix())) {
			throw new RuntimeException("Prefix not match: " + prefix);
		}
		String content = rawResult.substring(rawResult.indexOf(":") + 2);
		return content;
	}

	public abstract String getCommandPrefix();

	public abstract String getCommandContent();
}
