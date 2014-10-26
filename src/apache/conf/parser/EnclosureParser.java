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
	 * @param includeVHosts flag to indicate whether to include enclosures in VirtualHosts
	 * @return gets all the matching Enclosures in an array. 
	 * @throws Exception 
	 */
	public Enclosure[] getEnclosure(String enclosureType, boolean includeVHosts) throws Exception
	{
		Define defines[] = getAllDefines();	
				
		ArrayList<Enclosure> enclosures=new ArrayList<Enclosure>();
		
		ParsableLine lines[] = getConfigurationParsableLines(includeVHosts);
		
		String strLine;
		boolean insideEnclosure = false;
		ArrayList<ParsableLine> parsableLines = new ArrayList<ParsableLine>();
		
		int treeCount=0;
		for(ParsableLine line : lines) 
		{
			if(line.isInclude()) 
			{	
				strLine = processConfigurationLine(defines, line.getConfigurationLine().getLine());
				
				if(!isCommentMatch(strLine) && isEnclosureTypeMatch(strLine,enclosureType))
				{
					insideEnclosure=true;
					treeCount ++;
				}
				if(insideEnclosure)
				{
					if(!isCommentMatch(strLine) && !strLine.equals("")) {
						parsableLines.add(line);
					}
				
					if(!isCommentMatch(strLine) &&isCloseEnclosureTypeMatch(strLine,enclosureType))
					{
						treeCount--;
					
						if(treeCount==0) {
							insideEnclosure=false;
							enclosures.add(parseEnclosure(parsableLines.toArray(new ParsableLine[parsableLines.size()]), defines, includeVHosts));
							parsableLines.clear();
						}	
					}
				}
			}
		}	
				
		return enclosures.toArray(new Enclosure[enclosures.size()]);
	}
	
	private Enclosure parseEnclosure (ParsableLine[] parsableLines, Define defines[], boolean includeVHosts) throws Exception {
				
		String strLine;
		Enclosure enclosure=new Enclosure();
		boolean insideEnclosure=false;
		
		int treeCount=0;
		
		ArrayList<ParsableLine> subParsableLines = new ArrayList<ParsableLine>();
		
		int iter = 0;
		for(ParsableLine parsableLine : parsableLines) {
			iter ++;				
			
			strLine = processConfigurationLine(defines, parsableLine.getConfigurationLine().getLine());
			
			if(iter == 1) {
				String enclosureValues[]=strLine.replaceAll("\"|>|<", "").trim().replaceAll("\\s+|,", "@@").split("@@");
				enclosure.setType(enclosureValues[0]);
				StringBuffer enclosureValue=new StringBuffer();
				for(int j=1;j<enclosureValues.length; j++)
				{	
					enclosureValue.append(enclosureValues[j] + " ");
				}
				enclosure.setValue(enclosureValue.toString().trim());
				enclosure.setLineOfStart(parsableLine.getConfigurationLine().getLineNumInFile());
				enclosure.setLineOfEnd(parsableLines[parsableLines.length -1].getConfigurationLine().getLineNumInFile());
				enclosure.setFile(new File(parsableLine.getConfigurationLine().getFile()));
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
						subParsableLines.add(parsableLine);
					}
			
					if(!isCommentMatch(strLine) && isCloseEnclosureMatch(strLine))
					{
						treeCount--;
					
						if(treeCount==0) {
							insideEnclosure=false;
							enclosure.addEnclosure(parseEnclosure(subParsableLines.toArray(new ParsableLine[subParsableLines.size()]), defines, includeVHosts));
							subParsableLines.clear();							
						}	
					}
				}
				else if(!isCommentMatch(strLine) && !isCloseEnclosureMatch(strLine))
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
		Define defines[] = getAllDefines();							
		
		String includedFiles[]= getActiveConfFileList();
				
		StringBuffer fileString=new StringBuffer();
		boolean found=false;
		for(String includedFile : includedFiles)
		{	
			fileString.delete(0, fileString.length());
			found=false;
			
			if((new File(includedFile).exists()))
			{
				FileInputStream fstream = new FileInputStream(includedFile);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
				Pattern enclosurePattern=Pattern.compile("<\\s*\\b" + enclosureType + "\\b *" + valueRegex + "\\s*>", Pattern.CASE_INSENSITIVE);
			
				String strLine;
				String cmpLine;
				boolean insideEnclosure = false;
				
				int treeCount=0;
				while ((strLine = br.readLine()) != null)   
				{
					cmpLine = processConfigurationLine(defines, strLine);
					
					boolean enclosureMatch = enclosurePattern.matcher(cmpLine).find();
					boolean closeEnclosureMatch = isCloseEnclosureTypeMatch(cmpLine,enclosureType);
					
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
					Utils.writeStringBufferToFile(new File(includedFile), fileString, Charset.defaultCharset());
				}
				
			}	
		}
	}

}
