package apache.conf.modules;

import java.io.BufferedReader;

import apache.conf.parser.File;

import java.io.StringReader;
import java.util.ArrayList;

import apache.conf.global.Const;
import apache.conf.global.Utils;

/**
 * 
 *	<p>
 *	This class is used to parse and grab Shared Modules from the Apache configuration.
 *	</p>
 *	<p>
 *	Shared Modules are modules that are included in the Apache configuration using the "LoadModule" Directive.
 *	</p>
 *	<p>
 *	Example Shared Module Load Include:  "LoadModule authn_core_module modules/mod_authn_core.so"
 *	</p>
 */
public class SharedModuleParser extends ModuleParser{

	/**
	 * <p>
	 * SharedModuleParser constructor. A valid Apache binary file must be specified. Modules are obtained by running the binary file with the -M option.
	 * </p>
	 * <p>
	 * Example: "/usr/local/apache/bin/apachectl -M"<br/>
	 * /usr/local/apache/bin/apachectl would be the binary file in this case.
	 * </p>
	 * 
	 * @param binFile An Apache binary file. 
	 * @throws Exception if the specified binFile does not exist
	 */
	public SharedModuleParser(File binFile) throws Exception {
		super(binFile);
	}

	/**
	 * Function used to get a list of all Shared Modules.
	 * 
	 * @return an array of all Shared Modules.
	 * @throws Exception
	 * 
	 */
	public SharedModule[] getSharedModules() throws Exception
	{
		String output = runModuleCommand();
		
		ArrayList <SharedModule>modules=new ArrayList<SharedModule>();
		
		BufferedReader reader = new BufferedReader(new StringReader(output));
		
		String line;
		while ((line = reader.readLine()) != null) 
		{
			if(line.matches(".*(?i:syntax error).*")) {
				throw new Exception("There is an error when obtaining modules");
			}	
			
			if(line.matches(Const.sharedModulesSearchString)) {	
				modules.add(new SharedModule(line.replaceAll(Const.sharedModulesReplaceString, "").trim()));
			}	
		}
		reader.close();
		
		Utils.removeDuplicates(modules);
		
		return modules.toArray(new SharedModule[modules.size()]);
	}
}
