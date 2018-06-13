package io.zip;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class CompressionTests {
	
	private final String fileZip = "data_tests/file.zip";
	private final String folderZip = "data_tests/folder.zip";


	@Test
	public void FileCompressionTest() {
		File fileZip = new File(this.fileZip);
		if (fileZip.exists())
			fileZip.delete();
		
		ZipManager oz = new ZipManager(this.fileZip);
		oz.addFile("data_tests/zip_data/root.txt", "root.txt");
		oz.writeZip();
		
		Assert.assertTrue(new File(this.fileZip).exists());
	}
	
	@Test
	public void FolderCompressionTest() {
		File folderZip = new File(this.folderZip);
		if (folderZip.exists())
			folderZip.delete();
		
		ZipManager oz = new ZipManager(this.folderZip);
		oz.addDirectory("data_tests/zip_data/", "");
		oz.writeZip();
		
		Assert.assertTrue(new File(this.folderZip).exists());
	}

}
