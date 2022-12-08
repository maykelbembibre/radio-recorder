package radiorecorder.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxManager {

	private DbxClientV2 client;
	
	public DropboxManager() throws IOException, DbxException {
		this.client = authDropbox();
	}
	
	public void list() throws IOException, DbxException {
		// Get files and folder metadata from Dropbox root directory
		ListFolderResult result = client.files().listFolder("");
		while (true) {
		    for (Metadata metadata : result.getEntries()) {
		        System.out.println(metadata.getPathLower());
		    }

		    if (!result.getHasMore()) {
		        break;
		    }

		    result = client.files().listFolderContinue(result.getCursor());
		}
	}
	
	public void upload(File file) throws FileNotFoundException, IOException, UploadErrorException, DbxException {
		// Upload "test.txt" to Dropbox
		System.out.println("Uploading to dropbox: " + file.getName());
		try (InputStream in = new FileInputStream(file)) {
		    client.files().uploadBuilder("/" + file.getName())
		        .uploadAndFinish(in);
		}
		System.out.println("Uploaded to dropbox: " + file.getName());
	}
	
	private static DbxClientV2 authDropbox()
			throws IOException, DbxException {
		// Create Dropbox client
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        
        // Get current account info
        FullAccount account = client.users().getCurrentAccount();
        System.out.println("Connected to Dropbox account: " + account.getName().getDisplayName());
        
        return client;
	}
}
