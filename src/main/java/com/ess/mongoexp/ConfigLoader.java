package com.ess.mongoexp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class ConfigLoader {
    public static AppConfig load(Logger logger) throws Exception {
        logger.info("Inside Method : ConfigLoader.load");
        try {
            File jsonConfig = getConfigFile(logger);
            try (InputStream inputStream = new FileInputStream(jsonConfig)) {
                Yaml yaml = new Yaml();
                // The load method returns a Map (or ArrayList for sequences)
                logger.info("Exiting Method : ConfigLoader.load : Return Object");
                return yaml.loadAs(inputStream, AppConfig.class);
            } catch (Exception e) {
                // Handle other IOExceptions
                logger.error("Exiting Method : ConfigLoader.load 1 : " + e.getStackTrace().toString());
                e.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            logger.error("Exiting Method : ConfigLoader.load 2 : " + e.getStackTrace().toString());
            logger.error(e.toString());
            return null;
        }
    }
    private static File getConfigFile(Logger _logger) throws IOException {
        _logger.info("Inside Method : ConfigLoader.getConfigFile");
        File ret = new File("config.yaml");
        if (ret.exists()) {
            _logger.info("Using config file: %s", ret.toString());
            _logger.info(ret.getAbsoluteFile().toString());
            _logger.info("Exiting Method : ConfigLoader.getConfigFile 1 ");
            return ret.getAbsoluteFile();
        }
        _logger.info("Exiting Method : ConfigLoader.getConfigFile 2 ");
        return writeResourcesToFile(_logger, ret, "config.yaml");
    }
    private static File writeResourcesToFile(Logger _logger, File _dest, String _resource) throws IOException {
        _logger.info("Inside Method : ConfigLoader.writeResourcesToFile");
        InputStream in = App.class.getResourceAsStream(_resource);
        if (null == in) {
            _logger.error("Exiting Method : ConfigLoader.writeResourcesToFile : Could not find resource to create "+ _dest.getName());
            throw new IOException("Could not find resource to create " + _dest.getName());
        }
        try (FileOutputStream out = new FileOutputStream(_dest, false)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } 
        _logger.info("Exiting Method : ConfigLoader.writeResourcesToFile");
        return _dest;
    }
}
