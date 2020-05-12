package de.wvs.sw.webserver;


import de.progme.iris.Iris;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.progme.iris.config.Key;
import de.progme.iris.config.Value;
import de.progme.iris.exception.IrisException;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * Created by Marvin Erkes on 05.02.2020.
 */
public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        logger.info("Starting webserver");

        File config = new File("config.iris");
        if (!config.exists()) {
            try {
                Files.copy(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("config.iris")), config.toPath());
            } catch (IOException e) {
                logger.error("Unable to copy default config! No write permissions?", e);
                return;
            }
        }

        try {
            IrisConfig irisConfig = Iris.from(config)
                    .def(new Header("general"), new Key("debug"), new Value("true"))
                    .build();

            logger.info("Config loaded");

            Webserver webserver = WebserverFactory.create(irisConfig);
            webserver.start();
            webserver.console();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("SHUTDOWN HOOK BABY");
                    webserver.stop();
                } catch (LifecycleException e) {
                    System.exit(1);
                }
            }));
        } catch (IrisException error) {
            logger.error("Unable to load config", error);
        } catch (Exception error) {
            logger.error("Error while starting webserver.", error);
        }
    }
}
