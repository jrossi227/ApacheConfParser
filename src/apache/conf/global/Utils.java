package apache.conf.global;

import java.io.BufferedReader;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;

import apache.conf.parser.File;

public class Utils {

    /**
     * Reads an input file and returns a String.
     * 
     * @param file
     *            the file to read
     * @param charset
     *            the charset to use when reading the file
     * @return a String with the file contents.
     * @throws java.io.IOException
     */
    public static String readFileAsString(File file, Charset charset) throws java.io.IOException {
        return FileUtils.readFileToString(file, charset);
    }

    /**
     * Utility to copy one file to another.
     * 
     * @param source
     *            the file to be copied
     * @param destination
     *            the destination directory
     * @return a boolean indicating if the file was successfully copied.
     */
    public static boolean copyFile(File source, File destination) {
        try {
            FileUtils.copyFile(source, destination);

            setPermissions(destination);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * A utility to move files.
     * 
     * @param source
     *            the file to be moved
     * @param destination
     *            the destination directory
     * @return a boolean indicating if the file was successfully moved.
     * @throws Exception
     *             if the file can't be moved.
     */
    public static boolean moveFile(File source, File destination) {
        try {

            if (destination.exists()) {
                FileUtils.forceDelete(destination);
            }

            FileUtils.moveFile(source, destination);

            setPermissions(destination);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Copies one directory to another
     * 
     * @param srcDir
     *            the source directory.
     * @param destDir
     *            the destination directory.
     * @throws InterruptedException
     * @return a boolean indicating if the directory was successfully copied.
     * @throws IOException
     */
    public static boolean copyDirectory(File srcDir, File destDir) {

        try {
            FileUtils.copyDirectory(srcDir, destDir);
            setPermissions(destDir);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Utility used to delete a directory.
     * 
     * @param path
     *            - A directory to delete.
     * @return a boolean indicating if the deletion was successful.
     */
    public static boolean deleteDirectory(File path) {
        try {
            FileUtils.deleteDirectory(path);
        } catch (IOException e) {
            return false;
        }

        return true;

    }

    /**
     * Utility to move a directory
     * 
     * @param srcDir
     *            the source directory to move.
     * @param destDir
     *            the destination directory.
     * @return a boolean indicating if the directory was successfully moved.
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean moveDirectory(File srcDir, File destDir) {
        try {
            if (destDir.exists()) {
                FileUtils.deleteDirectory(destDir);
            }

            FileUtils.moveDirectory(srcDir, destDir);

            setPermissions(destDir);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Appends a list of all files contained in a directory. This search is recursive and all child directories will be parsed. This method does not return directories.
     * 
     * @param directory
     *            the directory to parse.
     * @return a list of all files contained in the directory and any sub directories.
     * @throws IOException
     */
    public static String[] getFileList(File directory) throws IOException {
        return getFileList(directory, "^(.*)$");
    }

    /**
     * Appends a list of all files contained in a directory. This search is recursive and all child directories will be parsed. This method does not return directories.
     * 
     * @param directory
     *            the directory to parse.
     * @param fileNameRegex
     *            a regex used to match against filenames
     * @return a list of all files contained in the directory and any sub directories.
     * @throws IOException
     */
    public static String[] getFileList(File directory, String fileNameRegex) throws IOException {
        
        //----------- BREADTH FIRST SEARCH------------------//

        Queue<File> queue = new LinkedList<File>();
        queue.add(directory);

        ArrayList<String> fileList = new ArrayList<String>();
        FileFilter fileFilter = new RegexFileFilter(fileNameRegex);
        while (!queue.isEmpty()) {
            File currentDirectory = (File) queue.remove();

            java.io.File children[] = currentDirectory.listFiles();

            File currFile;
            for (java.io.File child : children) {
                currFile = new File(currentDirectory, child.getName());
                if (currFile.isDirectory()) {
                    queue.add(currFile);
                }
                if (currFile.isFile() && fileFilter.accept(currFile)) {
                    fileList.add(currFile.getAbsolutePath());
                }
            }
        }
        return fileList.toArray(new String[fileList.size()]);
    }

    /**
     * Utilitiy used to remove duplicates from a list. This method does not maintain the list order.
     * 
     * @param a
     *            The input list.
     */
    public static void removeDuplicates(ArrayList a) {
        HashSet hs = new HashSet();
        hs.addAll(a);
        a.clear();
        a.addAll(hs);
    }

    /**
     * Utilitiy used to remove duplicates from a list. This method does maintain the list order.
     * 
     * @param arlList
     *            The input list.
     */
    public static void removeDuplicateWithOrder(ArrayList arlList) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = arlList.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (set.add(element))
                newList.add(element);
        }
        arlList.clear();
        arlList.addAll(newList);
    }

    /**
     * Utility used to see if a directory is a sub directory of another directory.
     * 
     * @param base
     *           the sub directory.
     * @param child
     *           the target directory 
     * @return true if base is a sub directory of child
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
     * @param userAgent
     *            the input user agent to match.
     * @param userAgentRegex
     *            a regex to compare with the input user agent.
     * @return a boolean indicating if the user agent matches the regex.
     */
    public static boolean matchUserAgent(String userAgent, String userAgentRegex) {
        if (userAgent == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(userAgentRegex, Pattern.CASE_INSENSITIVE);
        Matcher patternMatcher = pattern.matcher(userAgent);

        return patternMatcher.find();
    }

    /**
     * Executes a system command. No output is needed.
     * 
     * @param command
     *            the command to execute.
     * @throws IOException
     * @throws InterruptedException
     */
    public static void RunProcessWithoutOutput(String[] command) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
    }

    /**
     * Executes a system command and returns the output.
     * 
     * @param command
     *            the command to execute.
     * @return the OS output from the command.
     * @throws IOException
     * @throws InterruptedException
     */
    public static String RunProcessWithOutput(String[] command) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);

        StringBuffer output = new StringBuffer();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            int c;
            while ((c = inputReader.read()) != -1) {
                output.append((char) c);
            }
            inputReader.close();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((c = errorReader.read()) != -1) {
                output.append((char) c);
            }
            errorReader.close();
        } finally {
            p.waitFor();
            p.destroy();
        }
        return output.toString();
    }

    /**
     * Writes a StringBuffer to a file. If the file already exists it will be overwritten.
     * 
     * @param file
     *            the file to write to
     * @param buffer
     *            the content to write to the file
     * @param charset
     *            the charset to use when writing to the file
     * @throws IOException
     */
    public static void writeStringBufferToFile(File file, StringBuffer buffer, Charset charset) throws IOException {
        boolean exists = file.exists();

        FileUtils.writeStringToFile(file, buffer.toString(), charset);

        if (!exists) {
            setPermissions(file);
        }
    }

    /**
     * Set file and directory permissions for reading and writing
     * 
     * @param file
     *            the file to set permissions on
     * @throws IOException
     * @throws InterruptedException
     */
    public static void setPermissions(File file) {
        try {
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);

            if (!isWindows()) {
                String command[] = { "chmod", "777", file.getAbsolutePath() };
                Utils.RunProcessWithoutOutput(command);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
        }
    }

    /**
     * Replaces all tabs and multiple spaces with a single space
     * 
     * @param line
     *            the line to sanitize
     * @return the sanitized line
     */
    public static String sanitizeLineSpaces(String line) {
        return line.replaceAll("\\s+", " ").trim();
    }

    /**
     * 
     * @return true if the OS is Windows, false otherwise
     */
    public static boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    /**
     * 
     * @return the java version
     */
    public static double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }
}
