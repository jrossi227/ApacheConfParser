package apache.conf.global;

import apache.conf.modules.SharedModuleParser;
import apache.conf.modules.StaticModuleParser;
import apache.conf.parser.ConfigurationLine;
import apache.conf.parser.Directive;
import apache.conf.parser.DirectiveParser;
import apache.conf.parser.Enclosure;
import apache.conf.parser.EnclosureParser;
import apache.conf.parser.File;
import apache.conf.parser.ParsableLine;
import apache.conf.parser.Parser;

public class Samples {

    //--------------------------------------CODE SAMPLES---------------------------------------------//
    
    // Change these values for your environment
    static String rootConfFile = "/usr/local/apache2/conf/httpd.conf";
    static String serverRoot = "/usr/local/apache2";
    static String binFile = "/usr/local/apache2/bin/apachectl";

    // Search for "Listen" directive
    public static void directiveSearch() throws Exception {

        StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
        SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

        DirectiveParser parser = new DirectiveParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

        String directiveName = "Listen";
        Directive directives[] = parser.getDirective(directiveName, true);
        for (int i = 0; i < directives.length; i++) {
            System.out.println(directives[i].toString());
        }

    }

    // Search for "VirtualHost" enclosure
    public static void enclosureSearch() throws Exception {

        StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
        SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

        EnclosureParser parser = new EnclosureParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

        String enclosureName = "VirtualHost";
        Enclosure enclosures[] = parser.getEnclosure(enclosureName, true);
        for (int i = 0; i < enclosures.length; i++) {
            System.out.println(enclosures[i].toString());
        }
    }

    // Search for ServerName inside VirtualHosts
    public static void directiveInEnclosureSearch() throws Exception {

        StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
        SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

        EnclosureParser parser = new EnclosureParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

        String enclosureName = "VirtualHost";
        String directiveName = "ServerName";
        Enclosure enclosures[] = parser.getEnclosure(enclosureName, true);
        for (int i = 0; i < enclosures.length; i++) {
            Directive directives[] = enclosures[i].getDirectives();
            for (int j = 0; j < directives.length; j++) {
                if (directives[j].getType().toLowerCase().equals(directiveName.toLowerCase())) {
                    System.out.println(enclosures[i].toString());
                }
            }
        }
    }

    // Grab Active File List
    public static void grabActiveFileList() throws Exception {

        StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
        SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

        Parser parser = new Parser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

        String files[] = parser.getActiveConfFileList();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
        }
    }

    /**
     * 
     * Prints the configuration tree. There are two options with this function:
     * 
     * clean - Ignores all comments, empty lines and lines that are not part of the configuration. A line inside of an <IfModule> statement where the module is not loaded would be considered a line
     * that is not part of the configuration. This also replaces multiple spaces and tabs with a single space and removes all leading and trailing whitespace from the printed line. Lines will be
     * printed in the order that they appear in the configuration.
     * 
     * not clean - prints the entire configuration tree as is. This will print all comments and lines as they appear in the configuration, in the order that they appear in the configuration.
     * 
     * @param clean
     *            true to print the clean configuration, false to print everything
     * @throws Exception
     */
    public static void printConfigTree(boolean clean) throws Exception {
        StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
        SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

        Parser parser = new Parser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

        ParsableLine lines[] = parser.getConfigurationParsableLines(true);

        ConfigurationLine configurationLine;
        String out;
        for (ParsableLine line : lines) {

            configurationLine = line.getConfigurationLine();

            if (clean) {

                if (line.isInclude()) {
                    out = configurationLine.getProcessedLine() + " File: " + configurationLine.getFile() + " Line Number: " + configurationLine.getLineOfStart();
                } else {
                    continue;
                }

            } else {
                out = configurationLine.getLine() + " File: " + configurationLine.getFile() + " Line Number: " + configurationLine.getLineOfStart();
            }

            System.out.println(out);

        }
    }

    
}
