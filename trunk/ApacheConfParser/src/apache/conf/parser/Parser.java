package apache.conf.parser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apache.conf.global.Const;
import apache.conf.global.Utils;
import apache.conf.modules.SharedModule;
import apache.conf.modules.StaticModule;

/**
 * 
 * This class is used to provide generic parser functionality for the Apache configuration. 
 *
 */
public class Parser {
				
	protected String rootConfFile;
	protected String serverRoot;
	protected StaticModule staticModules[];
	protected SharedModule sharedModules[];
	
	/**
	 * @param rootConfFile the Apache root configuration file.
	 * @param serverRoot the Apache server root
	 * @param staticModules
	 * @param sharedModules
	 * @throws Exception if the rootConfFile or serverRoot do not exist
	 */
	public Parser(String rootConfFile, String serverRoot, StaticModule staticModules[], SharedModule sharedModules[]) throws Exception {
		if(!new File(rootConfFile).exists()) {
			throw new Exception("The root configuration file does not exist");
		}
		
		if(!new File(serverRoot).exists()) {
			throw new Exception("The server root does not exist");
		}
		
		this.rootConfFile = rootConfFile;
		this.serverRoot = serverRoot;
		this.staticModules = staticModules;
		this.sharedModules = sharedModules;
	}
	
	protected ParsableLine[] getParsableLines(String parserText, boolean includeVHosts) throws Exception 
	{	
		BufferedReader reader=new BufferedReader(new StringReader(parserText));
		
		ArrayList <ParsableLine> lines = new ArrayList<ParsableLine>();
		
		boolean skipIfModuleLine = false;
		int ifModuleTreeCount=0;
		
		boolean skipVirtualHostLine = false;
		
		String strLine;
		String cmpLine;
		while ((strLine = reader.readLine()) != null)   
		{
			cmpLine=Utils.sanitizeLineSpaces(strLine);
			
			/**
			 * Parse IfModule statements to see if we should add the directives
			 * 
			 * Two types of IfModules
			 * <IfModule mpm_prefork_module>
			 * <IfModule mod_ssl.c>
			 * 
			 */
			if(!isCommentMatch(cmpLine) && isIfModuleOpenNegateMatch(cmpLine)) {
				if(!skipIfModuleLine) {
					
					if(isInNegateStaticModules(cmpLine, staticModules) || isInNegateSharedModules(cmpLine, sharedModules)) {
						skipIfModuleLine=true;
					}
					
					if(skipIfModuleLine) {
						ifModuleTreeCount ++;
					}
				}
				else {
					//we have found a nested iFModule iterate the counter
					ifModuleTreeCount ++;
				}
			}
			else if(!isCommentMatch(cmpLine) && isIfModuleOpenMatch(cmpLine))
			{
				//Check if were already in a module that isn't loaded
				if(!skipIfModuleLine) {
					skipIfModuleLine=true;
					
					if(isInStaticModules(cmpLine, staticModules) || isInSharedModules(cmpLine, sharedModules)) {
						skipIfModuleLine=false;
					}
				
					//if the module isnt loaded we dont include whats in the enclosure
					if(skipIfModuleLine) {
						ifModuleTreeCount ++;
					}
				}
				else {
					//we have found a nested iFModule iterate the counter
					ifModuleTreeCount ++;
				}
			}
			
			/**
			 * Parse VirtualHost statements to see if we should add the directives
			 * 
			 * Example VirtualHost
			 * <VirtualHost *:80>
			 * 
			 */
			if(!includeVHosts && !isCommentMatch(cmpLine) && isVHostMatch(cmpLine)) {
				skipVirtualHostLine = true;
			}
			
			if(skipIfModuleLine)
			{
				if(!isCommentMatch(cmpLine) && isIfModuleCloseMatch(cmpLine))
				{
					ifModuleTreeCount--;
					
					if(ifModuleTreeCount==0) {
						skipIfModuleLine=false;
					}	
				}
				
				lines.add(new ParsableLine(strLine, false));
			} else if(skipVirtualHostLine) {
				if(!isCommentMatch(cmpLine) && isVHostCloseMatch(cmpLine)) {
					skipVirtualHostLine = false;
				}
				
				lines.add(new ParsableLine(strLine, false));
			} else {
				lines.add(new ParsableLine(strLine, true));
			}
		}	
		
		return lines.toArray(new ParsableLine[lines.size()]);
	}
	
	
	/**
	 * Goes through the target file and marks any lines that will be parsed by the Apache configuration.
	 * 
	 * @param file the file to parse
	 * @param includeVHosts flag to indicate if VirtualHosts should be included in the result
	 * @return an array of Parsable Lines in the file.
	 * @throws Exception
	 */
	public ParsableLine[] getParsableLines(File file, boolean includeVHosts) throws Exception 
	{
		return getParsableLines(Utils.readFileAsString(file,Charset.defaultCharset()), includeVHosts);
	}
	
	/**
	 * Utility to check if a line matches an Apache comment.
	 * 
	 * @param line the line to check for a comment.
	 * @return a boolean indicating if the line is a comment.
	 */
	public static boolean isCommentMatch(String line) {
		Pattern commentPattern=Pattern.compile("^\\s*#");
		return commentPattern.matcher(line).find(); 
	}
	
	/**
	 * Utility to check if a line matches a directive type.
	 * 
	 * @param line the line to check for the directive type.
	 * @param directiveType the name of the directive to match against. This is not case sensitive.
	 * @return
	 */
	protected static boolean isDirectiveMatch(String line, String directiveType) {
		Pattern directivePattern=Pattern.compile("^\\s*" +directiveType + " +", Pattern.CASE_INSENSITIVE);	
		return directivePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches an VirtualHost <br/>
	 * <br/>
	 * Example :<br/>
	 * &lt;/VirtualHost&gt;
	 * 
	 * @param line the line to match against
	 * @return a boolean indicating if the line matches a VirtualHost
	 */
	protected static boolean isVHostMatch(String line) {
		Pattern virtualHostPattern=Pattern.compile("<\\s*VirtualHost.*>", Pattern.CASE_INSENSITIVE);
		return virtualHostPattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check for a VirtualHost Close declaration<br/>
	 * <br/>
	 * Example :<br/>
	 * &lt;/VirtualHost&gt;
	 * 
	 * @param line the line to match against
	 * @return a boolean indicating if the line matches an IfModule Close declaration.
	 */
	protected static boolean isVHostCloseMatch(String line) {
		Pattern virtualHostClosePattern=Pattern.compile("</.*VirtualHost.*>", Pattern.CASE_INSENSITIVE);
		return virtualHostClosePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches an IfModule Open Negation <br/>
	 * <br/>
	 * Example :<br/>
	 * &lt;IfModule !mpm_netware_module&gt;
	 * 
	 * @param line the line to match against
	 * @return a boolean indicating if the line matches an IfModule Open Negation
	 */
	protected static boolean isIfModuleOpenNegateMatch(String line) {
		Pattern ifModuleOpenNegatePattern=Pattern.compile("<\\s*ifmodule.*!.*>", Pattern.CASE_INSENSITIVE);
		return ifModuleOpenNegatePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches an IfModule Open Declaration<br/>
	 * <br/>
	 * Example :<br/>
	 * &lt;IfModule status_module&gt;
	 * 
	 * @param line the line to match against
	 * @return a boolean indicating if the line matches an IfModule Open Declaration
	 */
	protected static boolean isIfModuleOpenMatch(String line) {
		Pattern ifModuleOpenPattern=Pattern.compile("<\\s*ifmodule.*>", Pattern.CASE_INSENSITIVE);
		return ifModuleOpenPattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check for an IfModule Close declaration<br/>
	 * <br/>
	 * Example :<br/>
	 * &lt;/ifmodule&gt;
	 * 
	 * @param line the line to match against
	 * @return a boolean indicating if the line matches an IfModule Close declaration.
	 */
	protected static boolean isIfModuleCloseMatch(String line) {
		Pattern ifModuleClosePattern=Pattern.compile("</.*ifmodule.*>", Pattern.CASE_INSENSITIVE);
		return ifModuleClosePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches an enclosure.
	 * 
	 * @param line the line to match against the enclosure.
	 * @param enclosureType the name of the directive to match against. This is not case sensitive.
	 * @return a boolean indicating if the line matches the enclosure.
	 */
	protected static boolean isEnclosureTypeMatch(String line, String enclosureType) {
		Pattern enclosurePattern=Pattern.compile("<\\s*" + enclosureType + ".*>", Pattern.CASE_INSENSITIVE);
		return enclosurePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches a closing enclosure.
	 * 
	 * @param line the line to match against the closing enclosure.
	 * @param enclosureType the name of the enclosure to match against. This is not case sensitive.
	 * @return a boolean indicating if the line matches the closing enclosure type.
	 */
	protected static boolean isCloseEnclosureTypeMatch(String line, String enclosureType) {
		Pattern closeEnclosurePattern=Pattern.compile("</\\s*" + enclosureType + ".*>", Pattern.CASE_INSENSITIVE);
		return closeEnclosurePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches an enclosure format.
	 * 
	 * @param line the line to match against the enclosure.
	 * @return a boolean indicating if the line matches the enclosure format.
	 */
	protected static boolean isEnclosureMatch(String line) {
		Pattern enclosurePattern=Pattern.compile("<\\s*[^/].*>", Pattern.CASE_INSENSITIVE);
		return enclosurePattern.matcher(line).find(); 
	}
	
	/**
	 * Utility used to check if a line matches a closing enclosure.
	 * 
	 * @param line the line to match against the closing enclosure.
	 */
	protected static boolean isCloseEnclosureMatch(String line) {
		Pattern closeEnclosurePattern=Pattern.compile("</.*>", Pattern.CASE_INSENSITIVE);
		return closeEnclosurePattern.matcher(line).find(); 
	}
	
	/**
	 * Checks for the negate ifmodule
	 * 
	 * eg. <IfModule !mpm_winnt_module>
	 * @param line
	 * @param staticModules
	 * @return
	 */
	protected static boolean isInNegateStaticModules(String line, StaticModule staticModules[]) {
		for(int j=0; j<staticModules.length; j++) {
			if(staticModules[j].getName().replaceAll("_module", "").equals(line.replaceAll("(?i)<.*ifmodule.*mod_", "").replaceAll("\\.c.*>", "")
					.replaceAll("(?i)<.*ifmodule *!", "").replaceAll("_module *>", ""))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks for the negate ifmodule
	 * 
	 * eg. <IfModule !mpm_winnt_module>
	 * @param line
	 * @param staticModules
	 * @return
	 */
	protected static boolean isInNegateSharedModules(String line, SharedModule sharedModules[]) {
		for(int j=0; j<sharedModules.length; j++) {
			if(sharedModules[j].getName().replaceAll("_module", "").equals(line.replaceAll("(?i)<.*ifmodule.*mod_", "").replaceAll("\\.c.*>", "")
					.replaceAll("(?i)<.*ifmodule *!", "").replaceAll("_module *>", ""))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks for theifmodule
	 * 
	 * eg. <IfModule mpm_winnt_module>
	 * @param line
	 * @param staticModules
	 * @return
	 */
	protected static boolean isInStaticModules(String line, StaticModule staticModules[]) {
		for(int j=0; j<staticModules.length; j++) {
			if(staticModules[j].getName().replaceAll("_module", "").equals(line.replaceAll("(?i)<.*ifmodule.*mod_", "").replaceAll("\\.c.*>", "")
					.replaceAll("(?i)<.*ifmodule *", "").replaceAll("_module *>", ""))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks for the ifmodule
	 * 
	 * eg. <IfModule mpm_winnt_module>
	 * @param line
	 * @param staticModules
	 * @return
	 */
	protected static boolean isInSharedModules(String line, SharedModule sharedModules[]) {
		for(int j=0; j<sharedModules.length; j++) {
			if(sharedModules[j].getName().replaceAll("_module", "").equals(line.replaceAll("(?i)<.*ifmodule.*mod_", "").replaceAll("\\.c.*>", "")
					.replaceAll("(?i)<.*ifmodule *", "").replaceAll("_module *>", ""))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gets a list of the configuration files currently included in the apache configuration.
	 * 
	 * @return an array with all included configuration files.
	 * @throws Exception
	 */
	public String[] getActiveConfFileList() throws Exception
	{		
		ArrayList<String> recursiveFiles = new ArrayList<String>();
		getActiveConfFileList(rootConfFile, recursiveFiles);
		
		Utils.removeDuplicateWithOrder(recursiveFiles);
		
		return recursiveFiles.toArray(new String[recursiveFiles.size()]);
	}
	
	private void getActiveConfFileList(String confFile, ArrayList<String> recursiveFiles) throws Exception
	{	
		
		ArrayList<String> files = new ArrayList<String>();
		
		Pattern includePattern=Pattern.compile(Const.includeDirective, Pattern.CASE_INSENSITIVE);
		
		ParsableLine lines[] = getParsableLines(new File(confFile), true);
		
		String strLine="";
		for(int i=0; i<lines.length; i++) 
		{
			if(lines[i].isInclude()) 
			{
				strLine=Utils.sanitizeLineSpaces(lines[i].getLine());
			
				Matcher includeMatcher = includePattern.matcher(strLine); 
				
				String file;
				String addedFile;
				
				if(!isCommentMatch(strLine) && includeMatcher.find())
				{
					file=strLine.replaceAll(Const.includeDirective + "\\s+", "").replaceAll("\"", "");
					
					//if the filename starts with it is an absolute path, otherwise its a relative path
					File check;
					if(file.startsWith("/") || (file.contains(":")) ) {
						check= new File(file);
					}	
					else {
						check= new File(serverRoot,file);
					}
					
					//check if its a directory, if it is we must include all files in the directory
					if(check.isDirectory())
					{
						String children[]=check.list();
						
						Arrays.sort(children);
						
						for(int j=0; j<children.length; j++)
						{
							if(!(new File(check.getAbsolutePath(), children[j]).isDirectory()))
							{
								addedFile=(new File(check.getAbsolutePath(),children[j]).getAbsolutePath());
								files.add(addedFile);
							}
						}
					}
					else
					{
						//check if its wild card here
						if(file.contains("*"))
						{
							File parent = new File(check.getParentFile().getAbsolutePath());
							String children[]=parent.list();
							
							Arrays.sort(children);
							
							File refFile;
							for(int j=0; j<children.length; j++)
							{	
								refFile=new File(parent.getAbsolutePath(), children[j]);
								if(!refFile.isDirectory() && refFile.getName().matches(check.getName().replaceAll("\\.", "\\.").replaceAll("\\*", ".*")))
								{
									addedFile=refFile.getAbsolutePath();
									files.add(addedFile);
								}
							}
						}
						else 
						{	
							addedFile=check.getAbsolutePath();
							files.add(addedFile);
						}
					}
				}
			}
		}	
		
		String tempList[] = files.toArray(new String[files.size()]);
		for (int i=0; i<tempList.length; i++)
		{
			if((new File(tempList[i]).exists())) {
				getActiveConfFileList(tempList[i],recursiveFiles);
			}
		}
		
		recursiveFiles.add(confFile);		
	}
}
