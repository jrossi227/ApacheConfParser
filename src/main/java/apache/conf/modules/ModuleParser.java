package apache.conf.modules;

import java.io.IOException;

import apache.conf.global.Utils;
import apache.conf.parser.File;

public class ModuleParser {

    protected File binFile;

    public ModuleParser(File binFile) throws Exception {
        if (!binFile.exists()) {
            throw new Exception("The specified binary file does not exist.");
        }

        this.binFile = binFile;
    }

    protected String runModuleCommand() throws IOException, InterruptedException {

        String commandString;
        if (Utils.isWindows()) {
            commandString = "cmd,/c," + binFile.getAbsolutePath() + ",-M";
        } else {
            commandString = binFile.getAbsolutePath() + ",-M";
        }

        String command[] = commandString.split(",");
        String output = Utils.RunProcessWithOutput(command);

        return output;

    }

}
