package core;

/* Simone 30/05/13 13.21 */

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties implements IAppProperties {
    static Object lock = new Object();
    public static IAppProperties instance;
    private final File propFile;

    public AppProperties(File propFile) {
        this.propFile = propFile;
    }

    private void load(Properties prop, File propFile) throws IOException {
        FileInputStream file = new FileInputStream(propFile);
        prop.load(file);
        file.close();
    }

    @Override
    public String getProperty(String name) {
        return getProperties().getProperty(name);
    }

    @Override
    public Properties getProperties() {
        synchronized (lock) {
            try {

                ensureExist();

                Properties prop = new Properties();
                load(prop, propFile);
                return prop;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getPropertiesAsString() {

        try {
            String result = FileUtils.readFileToString(propFile);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void setPropertyAsString(String properties) {
        try {
            FileUtils.writeStringToFile(propFile, properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String value = getProperty(name, null);
        if (value == null || value.isEmpty())
            return defaultValue;
        return "1".equals(value);
    }


    @Override
    public void setProperty(String name, String value) {

        synchronized (lock) {

            try {
                Properties prop = new Properties();

                ensureExist();
                load(prop, propFile);
                if (value == null)
                    prop.remove(name);
                else
                    prop.setProperty(name, value);
                FileOutputStream out = new FileOutputStream(propFile);
                prop.store(out, "");
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void ensureExist() throws IOException {
        if (propFile.exists())
            return;
        File dir = propFile.getAbsoluteFile().getParentFile();

        if (!dir.exists()) {
            boolean directoryCreated = dir.mkdirs();
            if (!directoryCreated) {
                throw new IOException("Could not create parent directory " + dir);
            }
        }

        boolean propFileCreated = propFile.createNewFile();

        if (!propFileCreated) {
            throw new IOException("Could not create property file " + propFile);
        }
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (value != null) return value;
        return defaultValue;

    }


}
