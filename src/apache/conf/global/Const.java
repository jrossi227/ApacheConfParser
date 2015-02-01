package apache.conf.global;

public class Const {

    public final static String newLine = System.getProperty("line.separator");

    // Constants used for Module Parsing
    public final static String staticModulesSearchString = ".*\\(static\\).*";
    public final static String sharedModulesSearchString = ".*\\(shared\\).*";
    public final static String staticModulesReplaceString = "\\(static\\)";
    public final static String sharedModulesReplaceString = "\\(shared\\)";
    public final static String staticModulesType = "static";
    public final static String sharedModulesType = "shared";
    
    //Selects spaces before or after a comma
    public final static String replaceCommaSpacesRegex = "(\\s*,\\s*)";
    //Selects any space that is not between a quotation
    public final static String replaceSpacesInValuesRegex = "\\s+(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)";
    // Constants to search for apache directives
    public final static String defineDirective = "Define";

}
