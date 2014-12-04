package apache.conf.parser;

public class ConfigurationLine {

    private String line;
    private String processedLine;
    private String file;
    private int lineOfStart;
    private int lineOfEnd;

    public ConfigurationLine(String line, String processedLine, String file, int lineOfStart, int lineOfEnd) {
        this.line = line;
        this.processedLine = processedLine;
        this.file = file;
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
     * @return the processedLine from the configuration. Leading and trailing whitespace will be removed and multiple spaces and tabs will be replaced with a single space in the processed line.
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

    public int getLineOfEnd() {
        return lineOfEnd;
    }

    public void setLineOfEnd(int lineOfEnd) {
        this.lineOfEnd = lineOfEnd;
    }

}
