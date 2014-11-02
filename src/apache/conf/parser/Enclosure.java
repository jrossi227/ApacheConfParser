package apache.conf.parser;

import java.util.ArrayList;
import java.util.Arrays;

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
    private File file;
    private int lineOfStart;
    private int lineOfEnd;
    private String type;
    private String value;
    private ArrayList<Directive> directives;
    private ArrayList<Enclosure> enclosures;

    public Enclosure() {
        this.file = null;
        this.lineOfStart = -1;
        this.lineOfEnd = -1;
        this.type = "";
        this.value = "";
        directives = new ArrayList<Directive>();
        this.enclosures = new ArrayList<Enclosure>();
    }

    public Enclosure(File file, int lineOfStart, int lineOfEnd, String type) {
        this.file = file;
        this.lineOfStart = lineOfStart;
        this.lineOfEnd = lineOfEnd;
        this.type = type;
        this.value = "";
        directives = new ArrayList<Directive>();
        this.enclosures = new ArrayList<Enclosure>();
    }

    public Enclosure(File file, int lineOfStart, int lineOfEnd, String type, String value) {
        this.file = file;
        this.lineOfStart = lineOfStart;
        this.lineOfEnd = lineOfEnd;
        this.type = type;
        this.value = value;
        directives = new ArrayList<Directive>();
        this.enclosures = new ArrayList<Enclosure>();
    }

    public Enclosure(File file, int lineOfStart, int lineOfEnd, String type, String value, Directive directives[]) {
        this.file = file;
        this.lineOfStart = lineOfStart;
        this.lineOfEnd = lineOfEnd;
        this.type = type;
        this.value = value;
        this.directives = (ArrayList<Directive>) Arrays.asList(directives);
        this.enclosures = new ArrayList<Enclosure>();
    }

    public Enclosure(File file, int lineOfStart, int lineOfEnd, String type, String value, Directive directives[], Enclosure enclosures[]) {
        this.file = file;
        this.lineOfStart = lineOfStart;
        this.lineOfEnd = lineOfEnd;
        this.type = type;
        this.value = value;
        this.directives = (ArrayList<Directive>) Arrays.asList(directives);
        this.enclosures = (ArrayList<Enclosure>) Arrays.asList(enclosures);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setLineOfStart(int lineOfStart) {
        this.lineOfStart = lineOfStart;
    }

    public void setLineOfEnd(int lineOfEnd) {
        this.lineOfEnd = lineOfEnd;
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

    public File getFile() {
        return file;
    }

    public int getLineOfStart() {
        return lineOfStart;
    }

    public int getLineOfEnd() {
        return lineOfEnd;
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

    public String toString() {
        StringBuffer enclosure = new StringBuffer();
        enclosure.append("<" + type + " " + value + ">" + Const.newLine);

        for (int i = 0; i < directives.size(); i++) {
            enclosure.append(directives.get(i).toString() + Const.newLine);
        }

        for (int i = 0; i < enclosures.size(); i++) {
            enclosures.get(i).toString(enclosure);
        }

        enclosure.append("</" + type + ">" + Const.newLine);
        return enclosure.toString();
    }

    private String toString(StringBuffer enclosure) {
        enclosure.append("<" + type + " " + value + ">" + Const.newLine);

        for (int i = 0; i < directives.size(); i++) {
            enclosure.append(directives.get(i).toString() + Const.newLine);
        }

        for (int i = 0; i < enclosures.size(); i++) {
            enclosures.get(i).toString(enclosure);
        }

        enclosure.append("</" + type + ">" + Const.newLine);
        return enclosure.toString();
    }

}
