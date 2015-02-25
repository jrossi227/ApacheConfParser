package apache.conf.samples;

import apache.conf.parser.Directive;
import apache.conf.parser.DirectiveParser;
import apache.conf.parser.Enclosure;
import apache.conf.parser.EnclosureParser;
import apache.conf.parser.File;
import apache.conf.parser.Parser;

import java.util.Calendar;

import apache.conf.modules.SharedModuleParser;
import apache.conf.modules.StaticModuleParser;

public class Main {

    private final static String help = "Usage: java -jar ApacheConfParser.jar [-c ConfFile] [-s ServerRoot] [-b ApacheBinary] [args...]\n"
            + "\n"
            + "where args include:\n"
            + "           -h                                : help (This page)\n"
            + "           -a                                : View all currently included configuration files.\n"
            + "           -d DirectiveName                  : Search for a directive by name.\n"
            + "           -e EnclosureName                  : Search for an enclosure by name.\n"
            + "           -ed EnclosureName DirectiveName   : Search for a directive inside an enclosure.\n"
            + "\n"
            + "Example searching for the \"Listen\" directive:\n"
            + "java -jar ApacheConfParser.jar -c /usr/local/apache/conf/httpd.conf -s /usr/local/apache -b /usr/local/apache/bin/apachectl -d Listen\n"
            + "java -jar ApacheConfParser.jar -c \"C:\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\\conf\\httpd.conf\" -s \"C:\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\" -b \"C:\\Program Files (x86)\\Apache Software Foundation\\Apache2.2\\bin\\httpd.exe\" -d Listen\n";

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            if (args.length == 0 || (args.length > 0 && args[0].equals("-h")) || args.length < 7) {
                System.out.println(help);
                return;
            }

            String rootConfFile = "", serverRoot = "", binFile = "";

            String option = "", directiveName = "", enclosureName = "";

            String currentArg = "", nextArg = "";
            for (int i = 0; i < args.length; i++) {
                currentArg = args[i].toLowerCase();

                nextArg = null;
                if ((i + 1) < args.length) {
                    nextArg = args[i + 1];
                }

                if (currentArg.startsWith("-") && !currentArg.equals("-a")) {
                    if (nextArg == null) {
                        System.out.println(help);
                        return;
                    }
                }

                if (currentArg.equals("-c")) {
                    rootConfFile = new File(nextArg).getAbsolutePath();
                }

                if (currentArg.equals("-s")) {
                    serverRoot = new File(nextArg).getAbsolutePath();
                }

                if (currentArg.equals("-b")) {
                    binFile = new File(nextArg).getAbsolutePath();
                }

                if (currentArg.equals("-a")) {
                    option = "-a";
                }

                if (currentArg.equals("-d")) {
                    option = "-d";
                    directiveName = nextArg;
                }

                if (currentArg.equals("-e")) {
                    option = "-e";
                    enclosureName = nextArg;
                }

                if (currentArg.equals("-ed")) {
                    option = "-ed";
                    enclosureName = nextArg;

                    if ((i + 2) < args.length) {
                        directiveName = args[i + 2];
                    } else {
                        System.out.println(help);
                        return;
                    }
                }
            }

            if (rootConfFile.equals("") || serverRoot.equals("") || binFile.equals("")) {
                System.out.println(help);
            }

            System.out.println("Searching...");

            long startTime = Calendar.getInstance().getTimeInMillis();

            StaticModuleParser staticParser = new StaticModuleParser(new File(binFile));
            SharedModuleParser sharedParser = new SharedModuleParser(new File(binFile));

            String results = "Results : \n\n";
            if (option.equals("-d")) {
                DirectiveParser parser = new DirectiveParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

                Directive directives[] = parser.getDirective(directiveName, true);
                for (int i = 0; i < directives.length; i++) {
                    results += "\nLine: " + directives[i].getConfigurationLine().getLineOfStart() + " File: " + directives[i].getConfigurationLine().getFile() + "\n";
                    results += directives[i].toString() + "\n";
                }
            }

            if (option.equals("-e")) {
                EnclosureParser parser = new EnclosureParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

                Enclosure enclosures[] = parser.getEnclosure(enclosureName, true);
                for (int i = 0; i < enclosures.length; i++) {
                    results += "\nLine: " + enclosures[i].getLineOfStart() + " File: " + enclosures[i].getFile() + "\n";
                    results += enclosures[i].toString() + "\n";
                }
            }

            if (option.equals("-ed")) {
                EnclosureParser parser = new EnclosureParser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

                Enclosure enclosures[] = parser.getEnclosure(enclosureName, true);
                for (int i = 0; i < enclosures.length; i++) {
                    Directive directives[] = enclosures[i].getDirectives();
                    for (int j = 0; j < directives.length; j++) {
                        if (directives[j].getType().toLowerCase().equals(directiveName.toLowerCase())) {
                            results += "\nLine: " + enclosures[i].getLineOfStart() + " File: " + enclosures[i].getFile() + "\n";
                            results += enclosures[i].toString() + "\n";
                        }
                    }
                }
            }

            if (option.equals("-a")) {
                Parser parser = new Parser(rootConfFile, serverRoot, staticParser.getStaticModules(), sharedParser.getSharedModules());

                results = "";
                String files[] = parser.getActiveConfFileList();
                for (int i = 0; i < files.length; i++) {
                    results += files[i] + "\n";
                }
            }

            long endTime = Calendar.getInstance().getTimeInMillis();

            results += "\nTime: " + (endTime - startTime) + " ms\n\n";

            System.out.print(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
