package radiorecorder;

import radiorecorder.threading.RecorderThread;

public class Main {
	
	public static void main(String[] args) throws Exception {
		String url = "http://21423.live.streamtheworld.com/2GBAAC.aac";
		String filePath = "C:/Users/mikel.rodriguez/Desktop";
		RecorderThread recorder = new RecorderThread(url, filePath);
		recorder.start();
		System.out.println("Press to stop.");
		System.in.read();
		System.out.println("Stopping...");
		recorder.stopInOrderlyFashion();
	}
}
