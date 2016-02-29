package cs.vt.analysis.datamanager.worker;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileResourceManager {

	private File datasetDirectory;

	public void setDatasetDirectory(String path) throws Exception {
		File f = new File(path);
		if(f.isDirectory()){
			datasetDirectory = new File(path);
			
		}else{
			throw new Exception("The givien path is not a directory");
		}
		
	}

	public File getDatasetDirectory() {
		return datasetDirectory;
	}
	
	public File[] getFiles(){
		return datasetDirectory.listFiles();
	}

	public void write(String fileName, String string) throws IOException {
		File path = new File(datasetDirectory, fileName);
		FileUtils.writeStringToFile(path, string);
	}
	
	public void cleanDirectory() throws IOException{
		FileUtils.cleanDirectory(datasetDirectory);
	}
	

}
