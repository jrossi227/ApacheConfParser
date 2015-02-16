package apache.conf.parser;

/** 
 * This class is used to model a configuration line from the Apache Configuration <br/>
 * <br/>
 * A configuration line consists of the following:<br/>
 * line - The unmodified line(s) from the Apache Configuration. A configuration line may span multiple lines as long as the end of the line ends with a backslash<br/> 
 * processed line - The processed configuration line. The following is done to process a configuration line:<br/>
 * <br/>
 * 1. Trim leading and trailing whitespace from the line.<br/>
 * 2. Remove multiple spaces and tabs and replace them with one space.<br/>
 * 3. If the configuration line spans multiple lines make it a single line.<br/>
 * <br/>
 * file - The file that contains the line<br/>
 * isComment - indicates whether the configuration line is a comment<br/>
 * lineOfStart - The line number inside of the file where the configuration line starts<br/>
 * lineOfEnd - The line number insode of the file where the configuration line ends.<br/>
 * 
 */

public class ConfigurationLine {

    private String line;
    private String processedLine;
    private String file;
    private int lineOfStart;
    private int lineOfEnd;
    private boolean isComment;

    public ConfigurationLine(String line, String processedLine, String file, boolean isComment, int lineOfStart, int lineOfEnd) {
        this.line = line;
        this.processedLine = processedLine;
        this.file = file;
        this.isComment = isComment;
        this.lineOfStart = lineOfStart;
        this.lineOfEnd = lineOfEnd;
    }

    /**
     * @return the unmodified line from the apache configuration
     */
    public String getLine() {
        return line;
    }

    /**
     * @param line
     *            the line to set
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     * @return the processedLine from the configuration. 
     */
    public String getProcessedLine() {
        return processedLine;
    }

    /**
     * @param processedLine
     *            the processedLine to set
     */
    public void setProcessedLine(String processedLine) {
        this.processedLine = processedLine;
    }

    /**
     * @return the file that contains the configuration line.
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the isComment
     */
    public boolean isComment() {
        return isComment;
    }

    /**
     * @param isComment the isComment to set
     */
    public void setComment(boolean isComment) {
        this.isComment = isComment;
    }

    /**
     * 
     * @return
     */
    public int getLineOfStart() {
        return lineOfStart;
    }

    /**
     * 
     * @param lineOfStart
     */
    public void setLineOfStart(int lineOfStart) {
        this.lineOfStart = lineOfStart;
    }

    /**
     * 
     * @return lineOfEnd
     */
    public int getLineOfEnd() {
        return lineOfEnd;
    }

    /**
     * 
     * @param lineOfEnd
     */
    public void setLineOfEnd(int lineOfEnd) {
        this.lineOfEnd = lineOfEnd;
    }

    public boolean isMultiLine() {
        return (lineOfStart != lineOfEnd);
    }
    
}
