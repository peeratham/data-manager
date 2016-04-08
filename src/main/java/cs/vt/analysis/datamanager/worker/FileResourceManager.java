package cs.vt.analysis.datamanager.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class FileResourceManager {

	private File baseDatasetDirectory;
	private File analysisResultDir;
	public static final int MAX_FILES_PER_DIRECTORY = 10;
	public static int currentPart = 0; 
	public File currentDatasetDirectory;

	
	
	public File getAnalysisResultDir() {
		return analysisResultDir;
	}

	public void setDatasetDirectory(String path) throws Exception {
		File f = new File(path);
		if(f.isDirectory()){
			baseDatasetDirectory = new File(path);
			loadParams();
			currentDatasetDirectory = new File(baseDatasetDirectory,"part-"+currentPart);
		}else{
			throw new Exception("The givien path "+currentDatasetDirectory+" is not a directory");
		}
		
	}

	public File getBaseDatasetDirectory() {
		return baseDatasetDirectory;
	}
	

	public void write(String fileName, String string) throws IOException {
		checkAndUpdateDirectory();	//check and update first
		File path = new File(currentDatasetDirectory, fileName);
		FileUtils.writeStringToFile(path, string);
		
	}
	
	private void checkAndUpdateDirectory() {
		if(!currentDatasetDirectory.exists()){
			currentDatasetDirectory.mkdirs();
			saveParamChanges();
		}
		if(currentDatasetDirectory.listFiles().length>=MAX_FILES_PER_DIRECTORY){
			currentPart++;
			currentDatasetDirectory = new File(baseDatasetDirectory,"part-"+currentPart);
			currentDatasetDirectory.mkdirs();
			saveParamChanges();
		}
		
	}

	public void cleanDatasetDirectory() throws IOException{
		FileUtils.cleanDirectory(baseDatasetDirectory);
	}

	public void cleanAnalysisResultDir() throws IOException {
		FileUtils.cleanDirectory(analysisResultDir);
		
	}

	public void setAnalysisResultDir(String analysisOutputDir) throws Exception {
		File f = new File(analysisOutputDir);
		if(f.isDirectory()){
			analysisResultDir = new File(analysisOutputDir);
			
		}else{
			throw new Exception("The givien path is not a directory");
		}
		
	}
	
	public void saveParamChanges() {
	    try {
	        Properties props = new Properties();
	        props.setProperty("lastSubDir", ""+currentPart);
	        
	        File f = new File(baseDatasetDirectory,"saved_progress.properties");
	        OutputStream out = new FileOutputStream( f );
	        props.store(out, "Progress");
	        out.close();
	    }
	    catch (Exception e ) {
	        e.printStackTrace();
	    }
	}
	
	public void loadParams() {
	    Properties props = new Properties();
	    InputStream is = null;
	
	    try {
	        File f = new File(baseDatasetDirectory,"saved_progress.properties");
	        is = new FileInputStream( f );
	        props.load( is );
	        currentPart = new Integer(props.getProperty("lastSubDir","0"));
	        is.close();
	    }
	    catch ( Exception e ) { is = null; }
	 
	    
	    
	}
	
	

}
