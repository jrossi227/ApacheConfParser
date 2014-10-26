package apache.conf.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

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
		Pattern directivePattern=Pattern.compile("^\\s*\\b" +directiveType + "\\b +", Pattern.CASE_INSENSITIVE);	
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
		Pattern virtualHostPattern=Pattern.compile("<\\s*\\bVirtualHost\\b.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern virtualHostClosePattern=Pattern.compile("</.*\\bVirtualHost\\b.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern ifModuleOpenNegatePattern=Pattern.compile("<\\s*\\bifmodule\\b.*!.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern ifModuleOpenPattern=Pattern.compile("<\\s*\\bifmodule\\b.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern ifModuleClosePattern=Pattern.compile("</.*\\bifmodule\\b.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern enclosurePattern=Pattern.compile("<\\s*\\b" + enclosureType + "\\b.*>", Pattern.CASE_INSENSITIVE);
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
		Pattern closeEnclosurePattern=Pattern.compile("</\\s*\\b" + enclosureType + "\\b.*>", Pattern.CASE_INSENSITIVE);
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
	
	public static boolean isIncludeMatch(String line) {
        Pattern includePattern=Pattern.compile("\\b(Include|IncludeOptional)\\b", Pattern.CASE_INSENSITIVE);
        return includePattern.matcher(line).find(); 
    }
    
    protected static String getFileFromInclude(String line) {
        return line.replaceAll("(?i)\\b(Include|IncludeOptional)\\b\\s+", "").replaceAll("\"", "");
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
	
	private ConfigurationLine[] getConfigurationLines(String confFile) throws IOException {
        
        ArrayList<ConfigurationLine> configurationLines = new ArrayList<ConfigurationLine>();
        
        getConfigurationString(confFile, configurationLines);
        
        return configurationLines.toArray(new ConfigurationLine[configurationLines.size()]);
    }
    
    private void getConfigurationString(String confFile, ArrayList<ConfigurationLine> configurationLines) throws IOException {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(confFile),"UTF-8"));
        
        String strLine;
        int lineNumInFile = 0;
        while ((strLine = br.readLine()) != null)   
        {
            lineNumInFile ++;
            
            configurationLines.add(new ConfigurationLine(strLine, confFile, lineNumInFile));
            strLine=Utils.sanitizeLineSpaces(strLine);
            
            if(!isCommentMatch(strLine) && isIncludeMatch(strLine)) {
                
                String file;
                file = getFileFromInclude(strLine);
                
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
                        if(!(new File(check.getAbsolutePath(), children[j]).isDirectory())) {
                            getConfigurationString(new File(check.getAbsolutePath(),children[j]).getAbsolutePath(), configurationLines);
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
                                getConfigurationString(refFile.getAbsolutePath(), configurationLines);
                            }
                        }
                    }
                    else 
                    {   
                        getConfigurationString(check.getAbsolutePath(), configurationLines);
                    }
                }
            }
            
        }
        br.close();
        
    }
	
	protected ParsableLine[] getParsableLines(ConfigurationLine[] configurationLines, boolean includeVHosts) throws Exception 
    {   
        
        ArrayList <ParsableLine> lines = new ArrayList<ParsableLine>();
        
        boolean skipIfModuleLine = false;
        int ifModuleTreeCount=0;
        
        boolean skipVirtualHostLine = false;
        
        String cmpLine;
        for(ConfigurationLine configurationLine : configurationLines)   
        {
            cmpLine=Utils.sanitizeLineSpaces(configurationLine.getLine());
            
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
                
                lines.add(new ParsableLine(configurationLine, false));
            } else if(skipVirtualHostLine) {
                if(!isCommentMatch(cmpLine) && isVHostCloseMatch(cmpLine)) {
                    skipVirtualHostLine = false;
                }
                
                lines.add(new ParsableLine(configurationLine, false));
            } else {
                lines.add(new ParsableLine(configurationLine, true));
            }
        }   
        
        return lines.toArray(new ParsableLine[lines.size()]);
    }

	
	public ParsableLine[] getConfigurationParsableLines(boolean includeVHosts) throws IOException, Exception {
		return getParsableLines(getConfigurationLines(rootConfFile), includeVHosts);
	}
	
	public ParsableLine[] getFileParsableLines(String file, boolean includeVHosts) throws IOException, Exception {
	    return getParsableLines(getConfigurationLines(file), includeVHosts);
	}
		
	/**
	 * Gets a list of the configuration files currently included in the apache configuration.
	 * 
	 * @return an array with all included configuration files.
	 * @throws Exception
	 */
	public String[] getActiveConfFileList() throws Exception
	{		
		ParsableLine lines[] = getConfigurationParsableLines(true);
		
		ArrayList<String> files = new ArrayList<String>();
		files.add(rootConfFile);
				
		String strLine, file, addedFile;
		for(ParsableLine line : lines) 
		{
			if(line.isInclude()) 
			{
				strLine=Utils.sanitizeLineSpaces(line.getConfigurationLine().getLine());
								
				if(!isCommentMatch(strLine) && isIncludeMatch(strLine))
				{
					file = getFileFromInclude(strLine);
					
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
				
		Utils.removeDuplicateWithOrder(files);
		
		return files.toArray(new String[files.size()]);
	}
	
}
