package apache.conf.parser;

import java.io.IOException;
import java.net.URI;

import apache.conf.global.Utils;

/**
 * 
 * <p>
 * This class provides an extension of the built in java.io.File class. It has been extended to normalize file paths on Windows.
 * </p>
 * <p>
 * For Example the absolute or canonical path on Windows will be converted from C:\\Apache24 to C://Apache24
 * </p>
 */
public class File extends java.io.File {

    private static final long serialVersionUID = 1L;

    public File(java.io.File file) {
        super(file.getAbsolutePath());
    }

    public File(String pathname) {
        super(pathname);
    }

    public File(String parent, String child) {
        super(parent, child);
    }

    public File(File parent, String child) {
        super(parent, child);
    }

    public File(URI uri) {
        super(uri);
    }

    @Override
    public String getAbsolutePath() {
        if (Utils.isWindows()) {
            try {
                return getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.getAbsolutePath().replaceAll("\\\\", "/");
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return super.getCanonicalPath().replaceAll("\\\\", "/");
    }

}
