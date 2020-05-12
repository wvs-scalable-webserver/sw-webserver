package de.wvs.sw.webserver.command.impl;

import de.wvs.sw.webserver.Webserver;
import de.wvs.sw.webserver.command.Command;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class EndCommand extends Command {

    private static Logger logger = LoggerFactory.getLogger(EndCommand.class);

    public EndCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        try {
            Webserver.getInstance().stop();
        } catch (LifecycleException error) {
            logger.error("Error while stopping webserver.", error);
        }

        return true;
    }
}
