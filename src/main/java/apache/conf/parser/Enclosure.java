package apache.conf.parser;

import java.util.ArrayList;

import apache.conf.global.Const;

/**
 * <p>
 * Class used to model an Apache enclosure. An enclosure has a type, a value, included directives and nested enclosures.
 * </p>
 * <p>
 * An Example Enclosure is as follows: <br/>
 * &lt;VirtualHost *:80&gt;<br/>
 * ServerAdmin webmaster@localhost<br/>
 * &nbsp;&nbsp;&nbsp;&lt;Location /status&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;SetHandler server-status<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Order Deny,Allow<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Deny from all<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Allow from .foo.com<br/>
 * &nbsp;&nbsp;&nbsp;&lt;/Location&gt;<br/>
 * &lt;/VirtualHost&gt;<br/>
 * <br/>
 * type is VirtualHost<br/>
 * value is *:80<br/>
 * ServerAdmin is an included directive.<br/>
 * Location is a nested enclosure.<br/>
 * </p>
 */

public class Enclosure {
    private String type;
    private String value;
    private ArrayList<Directive> directives;
    private ArrayList<Enclosure> enclosures;
    private ArrayList<ConfigurationLine> configurationLines;

    public Enclosure() {
        this.type = "";
        this.value = "";
        this.directives = new ArrayList<Directive>();
        this.enclosures = new ArrayList<Enclosure>();
        this.configurationLines = new ArrayList<ConfigurationLine>();
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void addDirective(Directive directive) {
        directives.add(directive);
    }

    public void addEnclosure(Enclosure enclosure) {
        this.enclosures.add(enclosure);
    }
    
    public void addConfigurationLine(ConfigurationLine configurationLine) {
        this.configurationLines.add(configurationLine);
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Enclosure[] getEnclosures() {
        return enclosures.toArray(new Enclosure[enclosures.size()]);
    }

    public Directive[] getDirectives() {
        return directives.toArray(new Directive[directives.size()]);
    }

    public ConfigurationLine[] getConfigurationLines() {
        return configurationLines.toArray(new ConfigurationLine[configurationLines.size()]);
    }

    public int getLineOfStart() {
        if(this.configurationLines.size() == 0) {
            return -1;
        }
        
        return this.configurationLines.get(0).getLineOfStart();
    }
    
    public int getLineOfEnd() {
        if(this.configurationLines.size() == 0) {
            return -1;
        }
        
        return this.configurationLines.get(this.configurationLines.size()-1).getLineOfEnd();
    }
    
    public String getFile() {
        if(this.configurationLines.size() == 0) {
            return null;
        }
        
        return this.configurationLines.get(0).getFile();
    }
    
    public String toString() {
        StringBuffer enclosure = new StringBuffer();
        
        int enclosureCount = 0;
        String line;
        for(int i=0; i<configurationLines.size(); i++) {
            line = configurationLines.get(i).getProcessedLine();
            
            if(Parser.isEnclosureMatch(line) && !Parser.isVHostMatch(line) && i > 0) {
                enclosure.append(enclosures.get(enclosureCount).toString());
                enclosureCount ++;
            } else {
                enclosure.append(line + Const.newLine);
            }
        }
        
        return enclosure.toString();
    }
}
