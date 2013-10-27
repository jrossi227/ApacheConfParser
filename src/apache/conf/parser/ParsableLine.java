package apache.conf.parser;

/**
 *  <p>
 *  Class used to give information on whether a line in the configuration is parsable.<br/>
 *  If a line is parsable then the include value will be set to true. <br/>
 *  </p>
 */
public class ParsableLine {
	private String line;
	private boolean include;
	
	public ParsableLine(String line, boolean include) {
		this.line=line;
		this.include=include;
	}
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public boolean isInclude() {
		return include;
	}
	public void setInclude(boolean include) {
		this.include = include;
	}
}
