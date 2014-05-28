package apache.conf.parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

import apache.conf.global.Const;
import apache.conf.global.Utils;
import apache.conf.modules.SharedModule;
import apache.conf.modules.StaticModule;

/**
 * 
 * This class is used to parse the Apache configuration and obtain enclosures.
 *
 */
public class EnclosureParser extends Parser {
	
	/**
	 * @param rootConfFile the Apache root configuration file.
	 * @param serverRoot the Apache server root
	 * @param staticModules
	 * @param sharedModules
	 * @throws Exception if the rootConfFile or serverRoot do not exist
	 */
	public EnclosureParser(String rootConfFile, String serverRoot, StaticModule staticModules[], SharedModule sharedModules[]) throws Exception {
		super(rootConfFile, serverRoot, staticModules, sharedModules);
	}
	
	/**
	 * <p>
	 * Parses all active configuration files for the enclosure specified by enclosureType.
	 * </p>
	 * <p>
	 * For example if you search for "VirtualHost" this function will return all VirtualHosts in the configuration.
	 * </p>
	 * 
	 * @param enclosureType The enclosure name. This is not case sensitive.
	 * @return gets all the matching Enclosures in an array. 
	 * @throws Exception 
	 */
	public Enclosure[] getEnclosure(String enclosureType) throws Exception
	{
		Define defines[] = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		
		String enclosureTypeRegex="\\b" + enclosureType + "\\b";
		
		ArrayList<Enclosure> enclosures=new ArrayList<Enclosure>();
		
		String includedFiles[]= getActiveConfFileList();
		
		for(int i=0; i<includedFiles.length; i++)
		{	
			if((new File(includedFiles[i]).exists()))
			{
				ParsableLine lines[] = getParsableLines(new File(includedFiles[i]));
				
				String strLine;
				boolean insideEnclosure = false;
				StringBuffer enclosureText=new StringBuffer();
				
				int treeCount=0;
				for(int j=0; j< lines.length; j++) 
				{
					if(lines[j].isInclude()) 
					{	
						strLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(lines[j].getLine()));
						
						if(!isCommentMatch(strLine) && isEnclosureTypeMatch(strLine,enclosureTypeRegex))
						{
							insideEnclosure=true;
							treeCount ++;
						}
						if(insideEnclosure)
						{
							if(!isCommentMatch(strLine) && !strLine.equals("")) {
								enclosureText.append(strLine + Const.newLine);
							}
						
							if(!isCommentMatch(strLine) &&isCloseEnclosureTypeMatch(strLine,enclosureTypeRegex))
							{
								treeCount--;
							
								if(treeCount==0) {
									insideEnclosure=false;
									enclosures.add(parseEnclosure(enclosureText.toString(), defines));
									enclosureText.delete(0, enclosureText.length());
								}	
							}
						}
					}
				}	
				
			}	
		}
		return enclosures.toArray(new Enclosure[enclosures.size()]);
	}
	
	private Enclosure parseEnclosure (String enclosureText, Define defines[]) throws Exception {
				
		//read the text line by line
		//if a new enclosure starts parse all the text and call this function recursively
		String strLine;
		Enclosure enclosure=new Enclosure();
		boolean insideEnclosure=false;
		
		int treeCount=0;
		
		StringBuffer subEnclosureText=new StringBuffer();
		ParsableLine lines[] = getParsableLines(enclosureText);
		
		for(int i=0; i<lines.length; i++) {
			if(lines[i].isInclude()) 
			{	
				strLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(lines[i].getLine()));
								
				if(i==0) {
					String enclosureValues[]=strLine.replaceAll("\"|>|<", "").trim().replaceAll("\\s+|,", "@@").split("@@");
					enclosure.setType(enclosureValues[0]);
					StringBuffer enclosureValue=new StringBuffer();
					for(int j=1;j<enclosureValues.length; j++)
					{	
						enclosureValue.append(enclosureValues[j] + " ");
					}
					enclosure.setValue(enclosureValue.toString().trim());
				} 
				else
				{	
					if(!isCommentMatch(strLine) && isEnclosureMatch(strLine))
					{
						insideEnclosure=true;
						treeCount ++;
					}
					if(insideEnclosure)
					{
						if(!isCommentMatch(strLine)) {
							subEnclosureText.append(strLine + Const.newLine);
						}
				
						if(!isCommentMatch(strLine) && isCloseEnclosureMatch(strLine))
						{
							treeCount--;
						
							if(treeCount==0) {
								insideEnclosure=false;
								enclosure.addEnclosure(parseEnclosure(subEnclosureText.toString(), defines));
								subEnclosureText.delete(0, enclosureText.length());
							}	
						}
					}
					else if(!isCommentMatch(strLine) && !strLine.equals("") && !isCloseEnclosureMatch(strLine))
					{
						String directiveValues[]=strLine.replaceAll("\\s+|,", "@@").replaceAll("\"", "").split("@@");
						Directive directive=new Directive(directiveValues[0]);
						for(int j=1; j<directiveValues.length; j++)
						{
							directive.addValue(directiveValues[j]);
						}
						enclosure.addDirective(directive);
					}
				}
			}	
		}
				
		return enclosure;
	}
	
	/**
	 * Comments out all Enclosures from the active configuration that match the enclosure type and enclosure value regex.
	 * 
	 * @param enclosureType The enclosure name. This is not case sensitive.
	 * @param valueRegex The value to search for. This is not case sensitive and is a Java regex.
	 * @throws Exception
	 */
	public void deleteEnclosure(String enclosureType, String valueRegex) throws Exception
	{
		Define defines[] = Define.getAllDefine(new DirectiveParser(rootConfFile, serverRoot, staticModules, sharedModules));
		
		String enclosureTypeRegex="\\b" + enclosureType + "\\b";
				
		String includedFiles[]= getActiveConfFileList();
				
		StringBuffer fileString=new StringBuffer();
		boolean found=false;
		for(int i=0; i<includedFiles.length; i++)
		{	
			fileString.delete(0, fileString.length());
			found=false;
			
			if((new File(includedFiles[i]).exists()))
			{
				FileInputStream fstream = new FileInputStream(includedFiles[i]);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
				Pattern enclosurePattern=Pattern.compile("<\\s*" + enclosureTypeRegex + " *" + valueRegex + "\\s*>", Pattern.CASE_INSENSITIVE);
				Pattern closeEnclosurePattern=Pattern.compile("</\\s*" + enclosureTypeRegex + ".*>", Pattern.CASE_INSENSITIVE);
			
				String strLine;
				String cmpLine;
				boolean insideEnclosure = false;
				
				int treeCount=0;
				while ((strLine = br.readLine()) != null)   
				{
					cmpLine=Define.replaceDefinesInString(defines, Utils.sanitizeLineSpaces(strLine));
					
					boolean enclosureMatch = enclosurePattern.matcher(cmpLine).find();
					boolean closeEnclosureMatch = closeEnclosurePattern.matcher(cmpLine).find();
					
					if(!isCommentMatch(cmpLine)&&enclosureMatch)
					{
						insideEnclosure=true;
						treeCount ++;
						found=true;
					}
					if(insideEnclosure)
					{
						if(!isCommentMatch(cmpLine)&&closeEnclosureMatch)
						{
							treeCount--;
							
							if(treeCount==0) {
								insideEnclosure=false;
							}	
						}
						
						if(!cmpLine.startsWith("#")) {
							fileString.append("#");
						}
					}	
					fileString.append(strLine + Const.newLine);
				}
				in.close();
				
				if(found) {
					Utils.writeStringBufferToFile(new File(includedFiles[i]), fileString, Charset.defaultCharset());
				}
				
			}	
		}
	}

}
