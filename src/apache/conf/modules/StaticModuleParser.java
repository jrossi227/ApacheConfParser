package apache.conf.modules;

import java.io.BufferedReader;
import apache.conf.parser.File;
import java.io.StringReader;
import java.util.ArrayList;

import apache.conf.global.Const;
import apache.conf.global.Utils;

/**
 * 
 * <p>
 * This class is used to parse and grab Static Modules from the Apache configuration.
 * </p>
 * <p>
 * Static Modules are directly compiled into Apache.
 * </p>
 */
public class StaticModuleParser extends ModuleParser {

    /**
     * <p>
     * StaticModuleParser constructor. A valid Apache binary file must be specified. Modules are obtained by running the binary file with the -M option.
     * </p>
     * <p>
     * Example: "/usr/local/apache/bin/apachectl -M"<br/>
     * /usr/local/apache/bin/apachectl would be the binary file in this case.
     * </p>
     * 
     * @param binFile
     *            An Apache binary file.
     * @throws Exception
     *             if the specified binFile does not exist.
     */
    public StaticModuleParser(File binFile) throws Exception {
        super(binFile);
    }

    /**
     * Gets a list of all statically loaded modules. These modules can never change so it is recommended that the result of this function be cached whenever possible.
     * 
     * @return an array of all Static Modules.
     * @throws Exception
     */
    public StaticModule[] getStaticModules() throws Exception {

        String output = runModuleCommand();

        ArrayList<StaticModule> modules = new ArrayList<StaticModule>();

        BufferedReader reader = new BufferedReader(new StringReader(output));

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches(".*(?i:syntax error).*")) {
                throw new Exception("There is an error when obtaining modules");
            }

            if (line.matches(Const.staticModulesSearchString)) {
                modules.add(new StaticModule(line.replaceAll(Const.staticModulesReplaceString, "").trim()));
            }
        }
        reader.close();

        Utils.removeDuplicates(modules);

        return modules.toArray(new StaticModule[modules.size()]);
    }
}
