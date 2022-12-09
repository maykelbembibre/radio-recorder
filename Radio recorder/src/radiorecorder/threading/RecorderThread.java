package radiorecorder.threading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	private int intervalSeconds;

	public RecorderThread(String urlString, String filePath, DropboxManager dropbox, int intervalSeconds) throws Exception {
		this.url = new URL(urlString);
		Optional<String> optionalExtension = getExtensionByStringHandling(urlString);
		if (!optionalExtension.isPresent()) {
			throw new Exception("File extension is required.");
		}
		this.filePath = new File(filePath);
		this.extension = optionalExtension.get();
		this.dropbox = dropbox;
		if (intervalSeconds < 10) {
			this.intervalSeconds = 10;
		} else if (intervalSeconds > 3600) {
			this.intervalSeconds = 3600;
		} else {
			this.intervalSeconds = intervalSeconds;
		}
		System.out.println("A new recording will be created every " + this.intervalSeconds + " seconds.");
	}

	@Override
	public void run() {
		FileManager fm = new FileManager(this.filePath, this.extension, this.intervalSeconds * 1000);
		InputStream inputStream = null;
		byte[] buf = new byte[8192];
		try {
			inputStream = this.url.openStream();
			while (!this.stop) {
				try {
					this.withDatedFile(buf, inputStream, fm.next());
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
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
	
	private void withDatedFile(byte[] buf, InputStream inputStream, DatedFile datedFile) throws IOException, UploadErrorException, DbxException {
		System.out.println("Recording " + datedFile);
		OutputStream outputStream = null;
		int bytes = 0;
		try {
			outputStream = datedFile.getOutputStream();
			int length;
			while ((!this.stop) && (!datedFile.expired()) && ((length = inputStream.read(buf)) != -1)) {
				outputStream.write(buf, 0, length);
				bytes++;
			}
			System.out.println("Got " + bytes + " bytes.");
			if (this.stop) {
				System.out.println("Stopped.");
			} else if (datedFile.expired()) {
				System.out.println("Creating new file.");
			} else {
				System.out.println("Stream depleted.");
			}
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
					if (bytes == 0) {
						System.out.println("No time to record anything. Deleting file.");
						datedFile.getFile().delete();
					} else {
						this.dropbox.upload(datedFile.getFile());
					}
				} catch (UploadErrorException e) {
					System.out.println("Can't upload the file because it was uploaded before.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
