package radiorecorder.threading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import radiorecorder.logic.FileManager;
import radiorecorder.threading.model.DatedFile;

public class RecorderThread extends Thread {

	private URL url;
	private File filePath;
	private String extension;
	private volatile boolean stop;

	public RecorderThread(String urlString, String filePath) throws Exception {
		this.url = new URL(urlString);
		Optional<String> optionalExtension = getExtensionByStringHandling(urlString);
		if (!optionalExtension.isPresent()) {
			throw new Exception("File extension is required.");
		}
		this.filePath = new File(filePath);
		this.extension = optionalExtension.get();
	}

	@Override
	public void run() {
		FileManager fm = new FileManager(this.filePath, this.extension, 1800000);
		while (!this.stop) {
			try {
				this.withDatedFile(fm.next());
			} catch (IOException e) {
				throw new RuntimeException(e);
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
	
	private void withDatedFile(DatedFile datedFile) throws IOException {
		System.out.println("Recording " + datedFile);
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = this.url.openStream();
			outputStream = new FileOutputStream(datedFile.getFile());
			byte[] buf = new byte[8192];
			int length;
			while ((!this.stop) && (!datedFile.expired()) && ((length = inputStream.read(buf)) != -1)) {
				outputStream.write(buf, 0, length);
			}
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
