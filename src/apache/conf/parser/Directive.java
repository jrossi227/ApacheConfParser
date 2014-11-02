package apache.conf.parser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>
 * Class used to model an Apache directive. An Apache directive has a type and value(s).
 * </p>
 * <p>
 * Some example directives are as follows:<br/>
 * "Listen 80" - The type of this directive is "Listen" and the value is "80"<br/>
 * "Options Indexes FollowSymLinks" - The type of this directive is "Options" and the values are "Indexes" and "FollowSymLinks"<br/>
 * </p>
 */
public class Directive {
    private String type;
    private ArrayList<String> values;

    public Directive(String type) {
        this.type = type;
        values = new ArrayList<String>();
    }

    public Directive(String type, String values[]) {
        this.type = type;
        this.values = (ArrayList<String>) Arrays.asList(values);
    }

    public String getType() {
        return type;
    }

    public void addValue(String value) {
        values.add(value);
    }

    public String[] getValues() {
        return values.toArray(new String[values.size()]);
    }

    public String toString() {
        StringBuffer rep = new StringBuffer();
        rep.append(this.type);
        for (int i = 0; i < this.values.size(); i++) {
            rep.append(" " + this.values.get(i));
        }
        return rep.toString();
    }
}
