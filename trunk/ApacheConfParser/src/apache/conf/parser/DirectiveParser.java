package apache.conf.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import apache.conf.global.Const;
import apache.conf.global.Utils;
import apache.conf.modules.SharedModule;
import apache.conf.modules.StaticModule;

/**
 * 
 * This class is used to parse the Apache configuration and obtain directives.
 *
 */
public class DirectiveParser extends Parser {
		
	/**
	 * @param rootConfFile the Apache root configuration file.
	 * @param serverRoot the Apache server root
	 * @param staticModules
	 * @param sharedModules
	 * @throws Exception if the rootConfFile or serverRoot do not exist
	 */
	public DirectiveParser(String rootConfFile, String serverRoot, StaticModule staticModules[], SharedModule sharedModules[]) throws Exception {
		super(rootConfFile, serverRoot, staticModules, sharedModules);
	}
	
	/**
	 * <p>
	 * Parses all active configuration files for the directive values specified by directiveType.
	 * </p>
	 * <p>
	 * For example if you search for the directive "Listen" and the apache configuration contains the lines:<br/>
	 * Listen 80<br/>
	 * Listen 443 https<br/>
	 * The function will return an array with values "80" and "443 https".
	 * </p>
	 * @param directiveType The directive name. This is not case sensitive.
	 * @return gets all of the values of a directive in an array. If one instance of a directive has multiple values then they will be seperated by spaces.
	 * @throws Exception
	 */
	public String[] getDirectiveValue(String directiveType) throws Exception
	{
		Define defines[]; 
		if(!directiveType.equals(Const.defineDirective))	{	
			defines = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		} else {
			defines = new Define[0];
		}
		
		directiveType="\\b" + directiveType + "\\b";
		
		ArrayList<String> directives=new ArrayList<String>();
		
		String includedFiles[]= getActiveConfFileList();
		
		for(int i=0; i<includedFiles.length; i++)
		{	
			if((new File(includedFiles[i]).exists()))
			{					
				ParsableLine lines[] = getParsableLines(new File(includedFiles[i]));
				String strLine = "";
				for(int j=0; j< lines.length; j++) 
				{
					if(lines[j].isInclude()) 
					{	
						strLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(lines[j].getLine()));
						
						String directiveValues[];
						String addDirective="";
						
						if(!isCommentMatch(strLine) && isDirectiveMatch(strLine,directiveType))
						{
							directiveValues=strLine.replaceAll("( +)|( *, *)", "@@").replaceAll("\"", "").split("@@");
							for(int k=1; k<directiveValues.length; k++)
							{
								addDirective = addDirective + " " + directiveValues[k];
							}
							addDirective=addDirective.trim();
							directives.add(addDirective);
						}
					}
				}
			}
		}
		 
		return directives.toArray(new String[directives.size()]);
	}
	
	/**
	 * <p>
	 * Parses all active configuration files for the directive specified by directiveType.
	 * </p>
	 * @param directiveType The directive name. This is not case sensitive.
	 * @return all instances of the directive.
	 * @throws Exception
	 */
	public Directive[] getDirective(String directiveType) throws Exception
	{
		ArrayList<Directive> directives=new ArrayList<Directive>();
		String values[] = getDirectiveValue(directiveType);
		
		String directiveValues[];
		Directive addDirective;
		
		String strLine="";
		for(int i=0; i<values.length; i++) {
			strLine=values[i];
			
			directiveValues=strLine.split(" ");
			addDirective = new Directive(directiveType);
			for(int j=0; j<directiveValues.length; j++)
			{
				addDirective.addValue(directiveValues[j]);
			}
			directives.add(addDirective);
		}
		
		return directives.toArray(new Directive[directives.size()]);
	}
	
	/**
	 * <p>
	 * Inserts a directive string before or after the first found matching directive type.
	 * </p>
	 * <p>
	 * For Example: <br/>
	 * If you specify a directive type of "Listen" and a directive String of "Listen 127.0.0.1:80" then this directive String would be inserted after the first "Listen" directive in the configuration.
	 * </p>
	 * 
	 * @param directiveType The directive name. This is not case sensitive.
	 * @param directiveString The directive string to insert.
	 * @param before a boolean indicating whether the directiveString should be inserted before the first found directive. true for before,false for after.
	 * @return a boolean indicating if the directive was found.
	 * @throws Exception
	 */
	public boolean insertDirectiveBeforeOrAfterFirstFound(String directiveType, String directiveString, boolean before) throws Exception
	{
		directiveType="\\b" + directiveType + "\\b";
		
		String includedFiles[]= getActiveConfFileList();
		
		boolean found=false;
		String foundFile="";
		StringBuffer fileText=new StringBuffer();
		for(int i=0; i<includedFiles.length && !found; i++)
		{	
			if((new File(includedFiles[i]).exists()))
			{
				fileText.delete(0, fileText.length());
				
				ParsableLine lines[] = getParsableLines(new File(includedFiles[i]));
				
				String strLine = "";
				String cmpLine = "";
				for(int j=0; j< lines.length; j++) 
				{
					strLine=lines[j].getLine();
					cmpLine = Utils.sanitizeLineSpaces(strLine);
					
					if(found) {
						fileText.append(strLine + Const.newLine);
						continue;
					}
					
					if(!before) {
						fileText.append(strLine + Const.newLine);
					}
					
					if(lines[j].isInclude()) {	
						if(!isCommentMatch(cmpLine) && isDirectiveMatch(cmpLine,directiveType))
						{
							fileText.append(directiveString + Const.newLine);
							foundFile=includedFiles[i];
							found=true;
						}
					}
					
					if(before) {
						fileText.append(strLine + Const.newLine);
					}
				}	
			}	
		}
		
		if(found) {
			Utils.writeStringBufferToFile(foundFile, fileText, Charset.defaultCharset());
		}
		
		return found;
	}
	
	/**
	 * Parses the Apache active file list looking for the first file with the directive and value combination.
	 * 
	 * @param directiveType The directive name. This is not case sensitive.
	 * @param valueContained The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
	 * @return the first file that matches the directive value combination or null if no file is found.
	 * @throws Exception 
	 */
	public String getDirectiveFile(String directiveType, String valueContained) throws Exception
	{
		Define defines[]; 
		if(!directiveType.equals(Const.defineDirective))	{	
			defines = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		} else {
			defines = new Define[0];
		}
		
		directiveType="\\b" + directiveType + "\\b";
			
		String includedFiles[]= getActiveConfFileList();
		
		for(int i=0; i<includedFiles.length; i++)
		{	
			if((new File(includedFiles[i]).exists()))
			{				
				ParsableLine lines[] = getParsableLines(new File(includedFiles[i]));
				
				String strLine = "";
				for(int j=0; j< lines.length; j++) 
				{
					if(lines[j].isInclude()) {	
						strLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(lines[j].getLine()));
					
						if(!isCommentMatch(strLine) && isDirectiveMatch(strLine,directiveType))
						{						
							if(strLine.toLowerCase().contains(valueContained.toLowerCase()))
							{	
								return includedFiles[i];
							}	
						}
					}
				}
			}	
		}
		return null;
	}

	/**
	 * Goes through the target file and removes any lines that match the directive type and value.
	 * 
	 * @param directiveType The directive name. This is not case sensitive.
	 * @param file The target file.
	 * @param removeValue The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
	 * @param commentOut a boolean indicating if the directive should be commented out rather than completely removed from the file.
	 * @throws Exception
	 */
	public void removeDirectiveFromFile(String directiveType, String file, String removeValue, boolean commentOut) throws Exception
	{
		Define defines[]; 
		if(!directiveType.equals(Const.defineDirective))	{	
			defines = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		} else {
			defines = new Define[0];
		}
		
		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		File FileHandle = new File(file + ".tmp"); 
		BufferedWriter writer = new BufferedWriter(new FileWriter(FileHandle));
				
		String strLine;
		String cmpLine;
		while ((strLine = br.readLine()) != null)   
		{
			cmpLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(strLine));
		
			if(!isCommentMatch(cmpLine)&&isDirectiveMatch(cmpLine,directiveType))
			{
				
				if(cmpLine.toLowerCase().contains(removeValue.toLowerCase()))
				{	
					
					if(commentOut) {
						writer.write("#" + strLine);
				        writer.newLine();
					}
				}
				else
				{
					writer.write(strLine);
			        writer.newLine();
				}
			}
			else
			{
				writer.write(strLine);
		        writer.newLine();
			}
			
		}
		in.close();
		writer.flush();
		writer.close();
		
		File fileNew = new File(file + ".tmp");
	    
		File fileOld = new File(file);
		
		Utils.moveFile(fileNew.getAbsolutePath(), fileOld.getAbsolutePath()); 
	}
	
	/**
	 * Goes through the target file and replaces the value of the passed in directive type with the insertValue. 
	 * 
	 * @param directiveType The directive type. This is not case sensitive.
	 * @param file The target file.
	 * @param insertValue The value to insert.
	 * @param valueContained The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
	 * @param add - Specifies whether we should add the directive to the file if it doesent exist.
	 * @throws Exception
	 */
	public void setDirectiveInFile(String directiveType, String file, String insertValue, String valueContained, boolean add) throws Exception
	{
		Define defines[]; 
		if(!directiveType.equals(Const.defineDirective))	{	
			defines = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		} else {
			defines = new Define[0];
		}
		
		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		File FileHandle = new File(file + ".tmp"); 
		BufferedWriter writer = new BufferedWriter(new FileWriter(FileHandle));
				
		String strLine;
		String cmpLine;
		boolean found = false;
		while ((strLine = br.readLine()) != null)   
		{
			cmpLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(strLine));
		
			if(!isCommentMatch(cmpLine)&&isDirectiveMatch(cmpLine,directiveType))
			{
				if(cmpLine.toLowerCase().contains(valueContained.toLowerCase())) 
				{
					writer.write(directiveType + " " + insertValue);
				    writer.newLine();
				    found=true;
				}
				else
				{
					writer.write(strLine);
			        writer.newLine();
				}
			}
			else
			{
				writer.write(strLine);
		        writer.newLine();
			}
			
		}
		
		if(!found && add) {
			writer.newLine();
			writer.write(directiveType + " " + insertValue);
		    writer.newLine();
		}
		
		in.close();
		writer.flush();
		writer.close();
		
		File fileNew = new File(file + ".tmp");
	    
		File fileOld = new File(file);
		
		Utils.moveFile(fileNew.getAbsolutePath(), fileOld.getAbsolutePath()); 
	}
}
