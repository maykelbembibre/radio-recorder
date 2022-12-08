package radiorecorder;

import radiorecorder.dropbox.DropboxManager;
import radiorecorder.threading.RecorderThread;

public class Main {
	
	public static void main(String[] args) throws Exception {
		DropboxManager dropbox = new DropboxManager();
		String url = "http://21423.live.streamtheworld.com/2GBAAC.aac";
		String filePath = "C:\\Users\\Max Power\\Desktop\\Aussie radio";
		RecorderThread recorder = new RecorderThread(url, filePath, dropbox);
		recorder.start();
		System.out.println("Press to stop.");
		System.in.read();
		System.out.println("Stopping...");
		recorder.stopInOrderlyFashion();
	}
}
