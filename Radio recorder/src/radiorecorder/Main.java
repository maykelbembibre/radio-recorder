package radiorecorder;

import radiorecorder.dropbox.DropboxManager;
import radiorecorder.threading.RecorderThread;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Access token needed as an argument to the program.");
		} else {
			DropboxManager dropbox;
			try {
				dropbox = new DropboxManager(args[0]);
			} catch (com.dropbox.core.InvalidAccessTokenException e) {
				dropbox = null;
				System.out.println("Your dropbox access token is no longer valid.");
			}
			if (dropbox == null) {
				System.out.println("There's no dropbox connection.");
			} else {
				String url = "http://21423.live.streamtheworld.com/2GBAAC.aac";
				String filePath = "C:\\Users\\Max Power\\Desktop\\Aussie radio";
				RecorderThread recorder = new RecorderThread(url, filePath, dropbox, 10);
				recorder.start();
				System.out.println("Press to stop.");
				System.in.read();
				System.out.println("Stopping...");
				recorder.stopInOrderlyFashion();
			}
		}
	}
}
