package radiorecorder.logic;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import radiorecorder.threading.model.DatedFile;

public class FileManager implements java.util.Iterator<DatedFile> {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH.mm.ss");
	
	private File basePath;
	private String extension;
	private int interval;
	private DatedFile current;
	
	public FileManager(File basePath, String extension, int interval) {
		this.basePath = basePath;
		this.extension = extension;
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public DatedFile next() {
		if ((this.current == null) || (this.current.expired())) {
			this.current = newDatedFile();
		}
		return this.current;
	}
	
	private static Date atMidnight() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        return now.getTime();
	}
	
	private DatedFile newDatedFile() {
		Date startOfDay = atMidnight();
		Date previous = startOfDay;
		Date now = new Date();
		Date target = startOfDay;
		while (target.getTime() <= now.getTime()) {
			previous = target;
			target = new Date(target.getTime() + this.interval);
		}
		String name = DATE_FORMAT.format(previous) + "-" + TIME_FORMAT.format(target);
		File file = new File(this.basePath, name + "." + extension);
		return new DatedFile(file, target);
	}
}
