package apache.conf.parser;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;

import apache.conf.global.Const;
import apache.conf.global.Utils;
import apache.conf.modules.SharedModule;
import apache.conf.modules.StaticModule;

/**
 * 
 * This class is used to parse the Apache configuration and obtain directives.
 *
 */
public class DirectiveParser extends Parser {

    /**
     * @param rootConfFile
     *            the Apache root configuration file.
     * @param serverRoot
     *            the Apache server root
     * @param staticModules
     * @param sharedModules
     * @throws Exception
     *             if the rootConfFile or serverRoot do not exist
     */
    public DirectiveParser(String rootConfFile, String serverRoot, StaticModule staticModules[], SharedModule sharedModules[]) throws Exception {
        super(rootConfFile, serverRoot, staticModules, sharedModules);
    }

    /**
     * <p>
     * Parses all active configuration files for the directive values specified by directiveType.
     * </p>
     * <p>
     * For example if you search for the directive "Listen" and the apache configuration contains the lines:<br/>
     * Listen 80<br/>
     * Listen 443 https<br/>
     * The function will return an array with values "80" and "443 https".
     * </p>
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param includeVHosts
     *            flag to indicate whether to include directives in VirtualHosts
     * @return gets all of the values of a directive in an array. If one instance of a directive has multiple values then they will be separated by spaces.
     * @throws Exception
     */
    public String[] getDirectiveValue(String directiveType, boolean includeVHosts) throws Exception {

        Define defines[] = getAllDefines(directiveType);

        ArrayList<String> directives = new ArrayList<String>();

        ParsableLine lines[] = getConfigurationParsableLines(includeVHosts);
        String strLine = "";
        for (ParsableLine line : lines) {
            if (line.isInclude()) {

                strLine = processConfigurationLine(defines, line.getConfigurationLine().getLine());

                String directiveValues[];
                String addDirective = "";

                if (!isCommentMatch(strLine) && isDirectiveMatch(strLine, directiveType)) {
                    directiveValues = strLine.replaceAll("(\\s+)|(\\s*,\\s*)", "@@").replaceAll("\"", "").split("@@");
                    for (String directiveValue : directiveValues) {
                        addDirective += " " + directiveValue;
                    }
                    directives.add(addDirective.trim());
                }
            }
        }

        return directives.toArray(new String[directives.size()]);
    }

    /**
     * <p>
     * Parses all active configuration files for the directive specified by directiveType.
     * </p>
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param includeVHosts
     *            flag to indicate whether to include directives in VirtualHosts
     * @return all instances of the directive.
     * @throws Exception
     */
    public Directive[] getDirective(String directiveType, boolean includeVHosts) throws Exception {
        ArrayList<Directive> directives = new ArrayList<Directive>();
        String values[] = getDirectiveValue(directiveType, includeVHosts);

        String directiveValues[];
        Directive addDirective;

        for (String value : values) {

            directiveValues = value.split(" ");
            addDirective = new Directive(directiveType);
            for (String directiveValue : directiveValues) {
                addDirective.addValue(directiveValue);
            }
            directives.add(addDirective);
        }

        return directives.toArray(new Directive[directives.size()]);
    }

    /**
     * <p>
     * Inserts a directive string before or after the first found matching directive type.
     * </p>
     * <p>
     * For Example: <br/>
     * If you specify a directive type of "Listen" and a directive String of "Listen 127.0.0.1:80" then this directive String would be inserted after the first "Listen" directive in the configuration.
     * </p>
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param directiveString
     *            The directive string to insert.
     * @param before
     *            a boolean indicating whether the directiveString should be inserted before the first found directive. true for before,false for after.
     * @param includeVHosts
     *            flag to indicate whether to include directives in VirtualHosts
     * @return a boolean indicating if the directive was found.
     * @throws Exception
     */
    public boolean insertDirectiveBeforeOrAfterFirstFound(String directiveType, String directiveString, boolean before, boolean includeVHosts) throws Exception {
        return insertDirectiveBeforeOrAfterFirstFound(directiveType, directiveString, Pattern.compile(".*"), before, includeVHosts);
    }

    /**
     * <p>
     * Inserts a directive string before or after the first found matching directive type and matches the matchesRegex.
     * </p>
     * <p>
     * For Example: <br/>
     * If you specify a directive type of "Listen", matches "[^0-9]70([^0-9]|)" and a directive String of "Listen 127.0.0.1:80" then this directive String would be inserted after the first "Listen"
     * directive with a value containing the number "70" in the configuration eg. "Listen 70"
     * </p>
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param directiveString
     *            The directive string to insert.
     * @param matches
     *            A filter that is used to check whether or not the directive matches a certain Regex. This is not case sensitive
     * @param before
     *            a boolean indicating whether the directiveString should be inserted before the first found directive. true for before,false for after.
     * @return a boolean indicating if the directive was found.
     * @param includeVHosts
     *            flag to indicate whether to include directives in VirtualHosts
     * @throws Exception
     */
    public boolean insertDirectiveBeforeOrAfterFirstFound(String directiveType, String directiveString, Pattern matchesPattern, boolean before, boolean includeVHosts) throws Exception {
        Define defines[] = getAllDefines(directiveType);

        boolean directiveFound = false;

        String file = getDirectiveFile(directiveType, matchesPattern, includeVHosts);

        if (file != null) {

            directiveFound = true;

            StringBuffer fileText = new StringBuffer();

            ParsableLine lines[] = getFileParsableLines(file, includeVHosts);

            String strLine = "", cmpLine = "";

            boolean found = false;
            for (ParsableLine line : lines) {
                strLine = line.getConfigurationLine().getLine();
                cmpLine = processConfigurationLine(defines, strLine);

                if (found) {
                    fileText.append(strLine + Const.newLine);
                    continue;
                }

                if (!before) {
                    fileText.append(strLine + Const.newLine);
                }

                if (line.isInclude()) {
                    if (!isCommentMatch(cmpLine) && isDirectiveMatch(cmpLine, directiveType)) {
                        if (matchesPattern.matcher(cmpLine).find()) {
                            fileText.append(directiveString + Const.newLine);
                            found = true;
                        }
                    }
                }

                if (before) {
                    fileText.append(strLine + Const.newLine);
                }
            }

            if (found) {
                Utils.writeStringBufferToFile(new File(file), fileText, Charset.forName("UTF-8"));
            }
        }

        return directiveFound;
    }

    /**
     * Parses the Apache active file list looking for the first file with the directive and value combination.
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param valueContained
     *            The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
     * @param includeVHosts
     *            flag to indicate whether to include directives in VirtualHosts
     * @return the first file that matches the directive value combination or null if no file is found.
     * @throws Exception
     */
    public String getDirectiveFile(String directiveType, Pattern matchesPattern, boolean includeVHosts) throws Exception {
        Define defines[] = getAllDefines(directiveType);

        ParsableLine lines[] = getConfigurationParsableLines(includeVHosts);

        String strLine = "";
        for (ParsableLine line : lines) {
            if (line.isInclude()) {
                strLine = processConfigurationLine(defines, line.getConfigurationLine().getLine());

                if (!isCommentMatch(strLine) && isDirectiveMatch(strLine, directiveType)) {
                    if (matchesPattern.matcher(strLine).find()) {
                        return line.getConfigurationLine().getFile();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Goes through the target file and removes any lines that match the directive type and value.
     * 
     * @param directiveType
     *            The directive name. This is not case sensitive.
     * @param file
     *            The target file.
     * @param removeValue
     *            The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
     * @param commentOut
     *            a boolean indicating if the directive should be commented out rather than completely removed from the file.
     * @return a boolean indicating if the directive was found.
     *
     * @throws Exception
     */
    public boolean removeDirectiveFromFile(String directiveType, String file, Pattern matchesPattern, boolean commentOut, boolean includeVHosts) throws Exception {
        Define defines[] = getAllDefines(directiveType);

        StringBuffer fileText = new StringBuffer();

        boolean changed = false;

        ParsableLine lines[] = getFileParsableLines(file, includeVHosts);

        String strLine = "", cmpLine = "";
        for (ParsableLine line : lines) {
            strLine = line.getConfigurationLine().getLine();
            cmpLine = processConfigurationLine(defines, strLine);

            if (!isCommentMatch(cmpLine) && isDirectiveMatch(cmpLine, directiveType) && line.isInclude()) {

                if (matchesPattern.matcher(cmpLine).find()) {

                    changed = true;

                    if (commentOut) {
                        fileText.append("#" + strLine + Const.newLine);
                    }
                } else {
                    fileText.append(strLine + Const.newLine);
                }
            } else {
                fileText.append(strLine + Const.newLine);
            }
        }

        if (changed) {
            Utils.writeStringBufferToFile(new File(file), fileText, Charset.forName("UTF-8"));
        }

        return changed;
    }

    /**
     * Goes through the target file and replaces the value of the passed in directive type with the insertValue.
     * 
     * @param directiveType
     *            The directive type. This is not case sensitive.
     * @param file
     *            The target file.
     * @param insertValue
     *            The value to insert.
     * @param valueContained
     *            The value to search for. The value is not case sensitive. Set this to empty if you dont wish to search for a specific value. The value is compared on a "contains" basis.
     * @param add
     *            - Specifies whether we should add the directive to the file if it doesent exist.
     * @throws Exception
     */
    public void setDirectiveInFile(String directiveType, String file, String insertValue, Pattern matchesPattern, boolean add, boolean includeVHosts) throws Exception {

        Define defines[] = getAllDefines(directiveType);

        StringBuffer fileText = new StringBuffer();

        boolean changed = false;

        ParsableLine lines[] = getFileParsableLines(file, includeVHosts);

        String strLine = "", cmpLine = "";
        for (ParsableLine line : lines) {
            strLine = line.getConfigurationLine().getLine();
            cmpLine = processConfigurationLine(defines, strLine);

            if (!isCommentMatch(cmpLine) && isDirectiveMatch(cmpLine, directiveType) && line.isInclude()) {

                if (matchesPattern.matcher(cmpLine).find()) {

                    changed = true;

                    fileText.append(directiveType + " " + insertValue);
                    fileText.append(Const.newLine);
                } else {
                    fileText.append(strLine + Const.newLine);
                }
            } else {
                fileText.append(strLine + Const.newLine);
            }
        }

        if (!changed && add) {

            changed = true;

            fileText.append(Const.newLine);
            fileText.append(directiveType + " " + insertValue);
            fileText.append(Const.newLine);
        }

        if (changed) {
            Utils.writeStringBufferToFile(new File(file), fileText, Charset.forName("UTF-8"));
        }

    }
}
