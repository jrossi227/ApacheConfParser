package apache.conf.global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import apache.conf.parser.File;

public class Utils 
{
	
	/**
	 * Reads an input file and returns a String.
	 * 
	 * @param file - the file to read.
	 * @return a String with the file contents.
	 * @throws java.io.IOException
	 */
	public static String readFileAsString(File file, Charset charset) throws java.io.IOException
	{
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),charset));
		char[] buf = new char[1024];
		int numRead=0;
		while((numRead=reader.read(buf)) != -1){
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}
	
	/**
	 * Utility to copy one file to another.
	 * 
	 * @param source - the absolute path to the source file.
	 * @param destination - the absolute path to the destination file.
	 * @return a boolean indicating if the file was successfully copied.
	 */
	public static boolean copyFile(String source, String destination)
	{
		try
		{
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(destination);
			byte[] buf = new byte[1024];
		 	int len;
		 	while ((len = in.read(buf)) > 0) {
		 	   out.write(buf, 0, len);
		 	}
		 	in.close();
		 	out.close();
		 	
		 	Utils.setPermissions(destination);
		 	
		 	return true;
		}
		catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}
		return false;
	}
	 
	/**
	 * A utility to move files.
	 * 
	 * @param source - The absolute path to the source file.
	 * @param destination - The absolute path to the destination file.
	 * @throws Exception if the file can't be moved.
	 */
	public static void moveFile(String source, String destination) throws Exception
	{
		boolean copied=Utils.copyFile(source, destination);
		if(!copied)
			throw new Exception("Unable to copy file");
			
		boolean delete=(new File(source)).delete();
		if(!delete)
			throw new Exception("Unable to delete file");
	}
	 
	/**
	 * Copies one directory to another
	 * 
	 * @param srcDir - The source directory.
	 * @param dstDir - The destination directory.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void copyDirectory(File srcDir, File dstDir) throws IOException, InterruptedException 
	{
		if (srcDir.isDirectory()) 
		{
			if (!dstDir.exists()) 
		    {
				dstDir.mkdir();
				
				Utils.setPermissions(dstDir.getAbsolutePath());
		    }

		    String[] children = srcDir.list();
		    for (int i=0; i<children.length; i++) 
		    {
		       copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
		    }
		} 
		else 
		{
		    // This method is implemented in Copying a File
			copyFile(srcDir.getAbsolutePath(), dstDir.getAbsolutePath());
		} 
	}
	
	/**
	 * Utility used to delete a directory.
	 * 
	 * @param path - A directory to delete.
	 * @return a boolean indicating if the deletion was successful.
	 */
	public static boolean deleteDirectory(File path) 
	{
		if( path.exists() ) 
		{
		   java.io.File[] files = path.listFiles();
		   for(int i=0; i<files.length; i++) 
		   {
		      if(files[i].isDirectory()) 
		      {
		         deleteDirectory(new File(files[i]));
		      }
		      else 
		      {
		        files[i].delete();
		      }
		   }
		}
		return( path.delete() );
	}
	 
	/**
	 * Utility to move a directory
	 * 
	 * @param srcDir - The source directory to move.
	 * @param dstDir - The destination directory.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void moveDirectory(File srcDir, File dstDir) throws IOException, InterruptedException
	{
		copyDirectory(srcDir,dstDir);
		deleteDirectory(srcDir);
	}
	 
	/**
	 * Appends a list of all files contained in a directory. 
	 * This search is recursive and all child direcotries will be parsed.
	 * This method does not return directories.
	 * 
	 * @param directory - The directory to parse. 
	 * @param recursiveFiles - A list of all files contained in the direcory will be appended to this parameter.
	 * @throws IOException
	 */
	public static void getFileList(String directory, ArrayList<String> recursiveFiles) throws IOException
	{
		File currentDirectory = new File(directory);
		String children[]=currentDirectory.list();
		File currFile;
		for(int i=0; i<children.length; i++)
		{
			currFile=new File(directory, children[i]);
			if(currFile.isDirectory())
			{
				getFileList(currFile.getAbsolutePath(), recursiveFiles);
			}
			else
			{
				if(recursiveFiles.indexOf(currFile.getAbsolutePath())==-1)
					recursiveFiles.add(currFile.getAbsolutePath());
			}
		}
	}
	  
	/**
	   * Utilitiy used to remove duplicates from a list.
	   * 
	   * @param a - The input list.
	   */
	  public static void removeDuplicates(ArrayList a)
	  {
		  HashSet hs = new HashSet();
		  hs.addAll(a);
		  a.clear();
		  a.addAll(hs);
	  }
	  
	  /**
	   * Utility used to see if a directory is a sub directory of another directory.
	   * 
	   * @param base - The sub directory.
	   * @param child 
	   * @return
	   * @throws IOException
	   */
	  public static boolean isSubDirectory(File base, File child) throws IOException {
		  base = new File(base.getCanonicalFile());
      	  child = new File(child.getCanonicalFile());

      	  File parentFile = child;
      	  while (parentFile != null) {
      		  if (base.equals(parentFile)) {
      			  return true;
      		  }
      		  parentFile = parentFile.getParentFile() != null ? new File(parentFile.getParentFile()) : null;
      	  }
      	  return false;
	  }
	  
	  /**
	   * Matches an input user agent with a regex. This is not case sensitive.
	   * 
	   * @param userAgent - The input user agent to match. 
	   * @param userAgentRegex - A regex to compare with the input user agent.
	   * @return a boolean indicating if the user agent matches the regex.
	   */
	  public static boolean matchUserAgent(String userAgent, String userAgentRegex) {
		  if(userAgent==null) {
			  return false;
		  }
		  
		  Pattern pattern=Pattern.compile(userAgentRegex, Pattern.CASE_INSENSITIVE);
		  Matcher patternMatcher = pattern.matcher(userAgent); 
		  
		  return patternMatcher.find();
	  }
  
	/**
	 * Executes a system command. No output is needed.
	 * 
	 * @param command - The command to execute.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void RunProcessWithoutOutput(String[] command) throws IOException, InterruptedException
	{
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
	}
	
	/**
	 * Executes a system command and receive the output.
	 * 
	 * @param command - The command to execute.
	 * @return the OS output from the command.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	 public static String RunProcessWithOutput(String[] command) throws IOException, InterruptedException
	 {
		Process p = Runtime.getRuntime().exec(command);
		  
		 StringBuffer output= new StringBuffer();
		 try
		 { 
			 BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			 int c;
			 while ((c=inputReader.read()) != -1) 
			 {
				 output.append((char)c);
			 }
			 inputReader.close();
			  
			 BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			 while ((c=errorReader.read()) != -1) 
			 {
				 output.append((char)c);
			 }
			 errorReader.close();
		 }
		 finally 
		 {
			 p.waitFor();
			 p.destroy();
		 }
		 return output.toString();
	 }

	/**
	 * Writes a StringBuffer to a file. If the file already exists it will be overwritten.
	 * 
	 * @param file
	 * @param buffer
  	* @throws IOException
  	*/
	public static void writeStringBufferToFile(String file, StringBuffer buffer, Charset charset) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),charset)); 
        out.write(buffer.toString());
        //Close the output stream
        out.close();
	}
  
	/**
	 * Set file and directory permissions for reading and writing
	 * 
	 * @param file the absolute path to the file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void setPermissions(String file)
	{
		File fileMod=new File(file);
	  
		try
		{
			if(fileMod.isDirectory()) 
			{
				String command[]={"chmod", "777", fileMod.getAbsolutePath()};
				Utils.RunProcessWithoutOutput(command);
			}
			else
			{
				String command[]={"chmod", "777", fileMod.getAbsolutePath()};
				Utils.RunProcessWithoutOutput(command);
			}
		}
		catch(Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
		}
	}
	
	public static String sanitizeLineSpaces(String line)
	{
		return line.replaceAll("\\s+"," ").trim();
	}
	  
	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}
}
