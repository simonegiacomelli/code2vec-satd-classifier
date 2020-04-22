package core;

/* Simone 30/05/13 13.21 */

import java.util.Properties;

public interface IAppProperties {
    public String getProperty(String name);

    public void setProperty(String name, String value);

    String getProperty(String name, String defaultValue);

    Properties getProperties();

    String getPropertiesAsString();

    void setPropertyAsString(String properties);

    boolean getBooleanProperty(String name, boolean defaultValue);
}
