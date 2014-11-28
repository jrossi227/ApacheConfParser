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
 * This class is used to parse the Apache configuration and obtain enclosures.
 *
 */
public class EnclosureParser extends Parser {

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
    public EnclosureParser(String rootConfFile, String serverRoot, StaticModule staticModules[], SharedModule sharedModules[]) throws Exception {
        super(rootConfFile, serverRoot, staticModules, sharedModules);
    }

    /**
     * <p>
     * Parses all active configuration files for the enclosure specified by enclosureType.
     * </p>
     * <p>
     * For example if you search for "VirtualHost" this function will return all VirtualHosts in the configuration.
     * </p>
     * 
     * @param enclosureType
     *            The enclosure name. This is not case sensitive.
     * @param includeVHosts
     *            flag to indicate whether to include enclosures in VirtualHosts
     * @return An array with all matching enclosures.
     * @throws Exception
     */
    public Enclosure[] getEnclosure(String enclosureType, boolean includeVHosts) throws Exception {

        ArrayList<Enclosure> enclosures = new ArrayList<Enclosure>();

        ParsableLine lines[] = getConfigurationParsableLines(includeVHosts);

        String strLine;
        boolean insideEnclosure = false;
        ArrayList<ParsableLine> parsableLines = new ArrayList<ParsableLine>();

        int treeCount = 0;
        for (ParsableLine line : lines) {
            if (line.isInclude()) {
                strLine = line.getConfigurationLine().getProcessedLine();

                if (!isCommentMatch(strLine) && isEnclosureTypeMatch(strLine, enclosureType)) {
                    insideEnclosure = true;
                    treeCount++;
                }
                if (insideEnclosure) {
                    if (!isCommentMatch(strLine) && !strLine.equals("")) {
                        parsableLines.add(line);
                    }

                    if (!isCommentMatch(strLine) && isCloseEnclosureTypeMatch(strLine, enclosureType)) {
                        treeCount--;

                        if (treeCount == 0) {
                            insideEnclosure = false;
                            enclosures.add(parseEnclosure(parsableLines.toArray(new ParsableLine[parsableLines.size()]), includeVHosts));
                            parsableLines.clear();
                        }
                    }
                }
            }
        }

        return enclosures.toArray(new Enclosure[enclosures.size()]);
    }

    private Enclosure parseEnclosure(ParsableLine[] parsableLines, boolean includeVHosts) throws Exception {

        String strLine;
        Enclosure enclosure = new Enclosure();
        boolean insideEnclosure = false;

        int treeCount = 0;

        ArrayList<ParsableLine> subParsableLines = new ArrayList<ParsableLine>();

        int iter = 0;
        for (ParsableLine parsableLine : parsableLines) {
            iter++;

            strLine = parsableLine.getConfigurationLine().getProcessedLine();

            if (iter == 1) {
                strLine = strLine.replaceAll("\"|>|<", "");
                strLine = strLine.replaceAll("(\\s*,\\s*)", ",");
                strLine = strLine.replaceAll("\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)", "@@");
                
                String enclosureValues[] = strLine.split("@@");
                enclosure.setType(enclosureValues[0]);
                StringBuffer enclosureValue = new StringBuffer();
                for (int j = 1; j < enclosureValues.length; j++) {
                    enclosureValue.append(enclosureValues[j] + " ");
                }
                enclosure.setValue(enclosureValue.toString().trim());
                enclosure.setLineOfStart(parsableLine.getConfigurationLine().getLineNumInFile());
                enclosure.setLineOfEnd(parsableLines[parsableLines.length - 1].getConfigurationLine().getLineNumInFile());
                enclosure.setFile(new File(parsableLine.getConfigurationLine().getFile()));
            } else {
                if (!isCommentMatch(strLine) && isEnclosureMatch(strLine)) {
                    insideEnclosure = true;
                    treeCount++;
                }
                if (insideEnclosure) {
                    if (!isCommentMatch(strLine)) {
                        subParsableLines.add(parsableLine);
                    }

                    if (!isCommentMatch(strLine) && isCloseEnclosureMatch(strLine)) {
                        treeCount--;

                        if (treeCount == 0) {
                            insideEnclosure = false;
                            enclosure.addEnclosure(parseEnclosure(subParsableLines.toArray(new ParsableLine[subParsableLines.size()]), includeVHosts));
                            subParsableLines.clear();
                        }
                    }
                } else if (!isCommentMatch(strLine) && !isCloseEnclosureMatch(strLine)) {
                    
                    strLine = strLine.replaceAll("(\\s*,\\s*)", ",");
                    strLine = strLine.replaceAll("\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)", "@@");
                    
                    String directiveValues[] = strLine.split("@@");
                    
                    Directive directive = new Directive(directiveValues[0]);
                    for (int j = 1; j < directiveValues.length; j++) {
                        directive.addValue(directiveValues[j]);
                    }
                    enclosure.addDirective(directive);
                }
            }
        }

        return enclosure;
    }

    /**
     * Removes all Enclosures from the active configuration that match the enclosure type and enclosure value pattern.
     * 
     * @param enclosureType
     *            The enclosure name. This is not case sensitive.
     * @param matchesValuePattern
     *            The pattern to match the enclosure value against
     * @param commentOut
     *            true to comment out the matching enclosure, false to remove the enclosure
     * @param includeVHosts
     *            boolean indicating whether to search for enclosures inside virtual hosts
     * @throws Exception
     */
    public void deleteEnclosure(String enclosureType, Pattern matchesValuePattern, boolean commentOut, boolean includeVHosts) throws Exception {

        String includedFiles[] = getActiveConfFileList();

        boolean changed = false, insideEnclosure = false;
        StringBuffer fileText = new StringBuffer();

        ParsableLine lines[];

        for (String file : includedFiles) {

            fileText.delete(0, fileText.length());

            changed = insideEnclosure = false;

            lines = getFileParsableLines(file, includeVHosts);

            int treeCount = 0;
            String strLine = "", cmpLine = "";
            for (ParsableLine line : lines) {
                strLine = line.getConfigurationLine().getLine();
                cmpLine = line.getConfigurationLine().getProcessedLine();

                if (!isCommentMatch(cmpLine) && isEnclosureTypeMatch(cmpLine, enclosureType) && line.isInclude()) {

                    if (matchesValuePattern.matcher(cmpLine).find()) {

                        treeCount++;

                        insideEnclosure = true;

                        changed = true;
                    }
                }

                if (insideEnclosure) {
                    if (!isCommentMatch(cmpLine) && isCloseEnclosureTypeMatch(cmpLine, enclosureType)) {
                        treeCount--;

                        if (treeCount == 0) {
                            insideEnclosure = false;
                        }
                    }

                    if (!cmpLine.startsWith("#") && commentOut) {
                        fileText.append("#" + strLine + Const.newLine);
                    }
                } else {
                    fileText.append(strLine + Const.newLine);
                }
            }

            if (changed) {
                Utils.writeStringBufferToFile(new File(file), fileText, Charset.forName("UTF-8"));
            }
        }
    }

}
