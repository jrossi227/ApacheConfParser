package apache.conf.modules;

import apache.conf.parser.File;

public class ModuleParser {

	protected File binFile;
	
	public ModuleParser(File binFile) throws Exception {
		if(!binFile.exists()) {
			throw new Exception("The specified binary file does not exist.");
		}
		
		this.binFile = binFile;
	}
	
}
