package de.wvs.sw.webserver.command.impl;

import de.wvs.sw.webserver.Webserver;
import de.wvs.sw.webserver.command.Command;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
public class DebugCommand extends Command {

    public DebugCommand(String name, String description, String... aliases) {

        super(name, description, aliases);
    }

    @Override
    public boolean execute(String[] args) {

        Webserver.getInstance().changeDebug();

        return true;
    }
}
