package radiorecorder.threading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.UploadErrorException;

import radiorecorder.dropbox.DropboxManager;
import radiorecorder.logic.FileManager;
import radiorecorder.threading.model.DatedFile;

public class RecorderThread extends Thread {

	private URL url;
	private File filePath;
	private String extension;
	private volatile boolean stop;
	private DropboxManager dropbox;

	public RecorderThread(String urlString, String filePath, DropboxManager dropbox) throws Exception {
		this.url = new URL(urlString);
		Optional<String> optionalExtension = getExtensionByStringHandling(urlString);
		if (!optionalExtension.isPresent()) {
			throw new Exception("File extension is required.");
		}
		this.filePath = new File(filePath);
		this.extension = optionalExtension.get();
		this.dropbox = dropbox;
	}

	@Override
	public void run() {
		FileManager fm = new FileManager(this.filePath, this.extension, 20000);
		while (!this.stop) {
			try {
				this.withDatedFile(fm.next());
			} catch (UnknownHostException | java.lang.IllegalArgumentException e) {
				System.out.println("Bad URL.");
				System.exit(-1);
			} catch (ConnectException e) {
				System.out.println("No Internet.");
				System.exit(-1);
			} catch (FileNotFoundException e) {
				System.out.println(e.getMessage());
				System.exit(-1);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void stopInOrderlyFashion() {
		this.stop = true;
	}

	private static Optional<String> getExtensionByStringHandling(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains("."))
				.map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}
	
	private void withDatedFile(DatedFile datedFile) throws IOException, UploadErrorException, DbxException {
		System.out.println("Recording " + datedFile);
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = this.url.openStream();
			File file = datedFile.getFile();
			outputStream = new FileOutputStream(file);
			byte[] buf = new byte[8192];
			int length;
			while ((!this.stop) && (!datedFile.expired()) && ((length = inputStream.read(buf)) != -1)) {
				outputStream.write(buf, 0, length);
			}
			this.dropbox.upload(file);
			if (this.stop) {
				System.out.println("Stopped.");
			} else if (datedFile.expired()) {
				System.out.println("Creating new file.");
			} else {
				System.out.println("Stream depleted.");
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
