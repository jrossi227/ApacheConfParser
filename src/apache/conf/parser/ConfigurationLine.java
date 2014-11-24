package apache.conf.parser;

public class ConfigurationLine {

    private String line;
    private String processedLine;
    private String file;
    private int lineNumInFile;

    public ConfigurationLine(String line, String processedLine, String file, int lineNumInFile) {
        this.line = line;
        this.processedLine = processedLine;
        this.file = file;
        this.lineNumInFile = lineNumInFile;
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
     * @return the line number of the configuration line inside of the containing file.
     */
    public int getLineNumInFile() {
        return lineNumInFile;
    }

    /**
     * @param lineNumInFile
     *            the lineNumInFile to set
     */
    public void setLineNumInFile(int lineNumInFile) {
        this.lineNumInFile = lineNumInFile;
    }

}
