package ndphu.app.gae.zd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import ndphu.app.gae.zd.model.Song;

public class ZingParser {
	private static final Logger log = Logger.getLogger(ZingParser.class.getName());
	private String url = null;
	private String dataUrl = null;
	private List<Song> songList = null;

	public ZingParser(String url) throws IOException {
		this.setUrl(url);
		parse();
	}

	public void parse() throws IOException {
		processLineByLine(getUrl(), new LineHandler() {

			@Override
			public void processLine(String line) {
				if (getDataUrl() != null) {
					return;
				}
				line = line.trim();
				if (line.contains("flashvars")) {
					setDataUrl(line.substring(line.indexOf("http://"),
							line.indexOf("&amp;textad")));
					log.warning(getDataUrl());
				}
			}
		});

		setSongList(new ArrayList<Song>());

		processLineByLine(getDataUrl(), new LineHandler() {
			Song song = null;

			@Override
			public void processLine(String line) {
				if (song == null) {
					song = new Song();
				}
				line = line.trim();
				if (line.contains("<title>")) {
					song.setTitle(line.substring(17, line.length() - 12));
				} else if (line.contains("<performer>")) {
					song.setPerformer(line.substring(20, line.length() - 15));
				} else if (line.contains("<source>")) {
					song.setSource(line.substring(17, line.length() - 12));
					getSongList().add(song);
					song = new Song();
				}
			}
		});

		for (Song s : getSongList()) {
			log.warning(s.getSource());
		}
	}

	public static InputStream getFromURL(String url)
			throws IllegalStateException, IOException {
		URL _url = new URL(url);
		URLConnection openConnection = _url.openConnection();
		InputStream content = openConnection.getInputStream();
		if (openConnection.getHeaderField("content-encoding") != null
				&& openConnection.getHeaderField("content-encoding").equals("gzip")) {
			return new GZIPInputStream(content);
		} else {
			return content;
		}
	}

	public static void processLineByLine(String url, LineHandler handler)
			throws IOException {
		InputStream is = getFromURL(url);
		processLineByLine(is, handler);
	}

	public static void processLineByLine(InputStream is, LineHandler handler)
			throws IOException {
		if (handler == null) {
			throw new RuntimeException("Line handler is null");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = br.readLine()) != null) {
				handler.processLine(line);
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	public List<Song> getSongList() {
		return songList;
	}

	public void setSongList(List<Song> songList) {
		this.songList = songList;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public static interface LineHandler {
		void processLine(String line);
	}

}