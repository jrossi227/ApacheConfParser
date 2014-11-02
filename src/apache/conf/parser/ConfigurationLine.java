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
     * @return the line
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
     * @return the processedLine
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
     * @return the file
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
     * @return the lineNumInFile
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
