package radiorecorder.threading.model;

import java.io.File;
import java.util.Date;

public class DatedFile {

	private final File file;
	private final Date validUntil;

	public DatedFile(File file, Date validUntil) {
		super();
		this.file = file;
		this.validUntil = validUntil;
	}

	public File getFile() {
		return file;
	}

	public Date getValidUntil() {
		return validUntil;
	}
	
	@Override
	public String toString() {
		return this.file.getName();
	}
	
	public boolean expired() {
		return new Date().getTime() > this.validUntil.getTime();
	}
}
