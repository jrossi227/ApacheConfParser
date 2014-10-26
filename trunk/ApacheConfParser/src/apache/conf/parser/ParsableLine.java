package apache.conf.parser;

/**
 *  <p>
 *  Class used to give information on whether a line in the configuration is parsable.<br/>
 *  If a line is parsable then the include value will be set to true. <br/>
 *  </p>
 */
public class ParsableLine {
	private ConfigurationLine configurationLine;
	private boolean include;
	
	public ParsableLine(ConfigurationLine configurationLine, boolean include) {
		this.configurationLine = configurationLine;
		this.include=include;
	}
	
	public ConfigurationLine getConfigurationLine() {
		return configurationLine;
	}
	public void setConfigurationLine(ConfigurationLine configurationLine) {
		this.configurationLine = configurationLine;
	}
	
	public boolean isInclude() {
		return include;
	}
	public void setInclude(boolean include) {
		this.include = include;
	}
}
