package de.wvs.sw.webserver;

import ch.qos.logback.classic.Level;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Key;
import de.wvs.sw.api.SWAPI;
import de.wvs.sw.api.SWAPIConfig;
import de.wvs.sw.api.SWAPIFactory;
import de.wvs.sw.api.modules.application.ApplicationModule;
import de.wvs.sw.shared.application.Deployment;
import de.wvs.sw.webserver.command.Command;
import de.wvs.sw.webserver.command.CommandManager;
import de.wvs.sw.webserver.command.impl.DebugCommand;
import de.wvs.sw.webserver.command.impl.EndCommand;
import de.wvs.sw.webserver.command.impl.HelpCommand;
import de.wvs.sw.webserver.command.impl.StatsCommand;
import de.wvs.sw.webserver.manager.WebserverManager;
import lombok.Getter;
import org.apache.catalina.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class Webserver {

    @Getter
    public static Webserver instance;

    private static final String WEBSERVER_PACKAGE_NAME = "de.wvs.sw.webserver";
    private static final Pattern ARGS_PATTERN = Pattern.compile(" ");
    private static Logger logger = LoggerFactory.getLogger(Webserver.class);
    private static ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WEBSERVER_PACKAGE_NAME);

    @Getter
    private IrisConfig config;

    private SWAPI swApi;
    @Getter
    private ApplicationModule applicationApi;

    @Getter
    private CommandManager commandManager;

    private Scanner scanner;

    private WebserverManager webserverManager;

    public Webserver(IrisConfig config) {

        Webserver.instance = this;

        this.config = config;
    }

    public void start() throws Exception {

        this.swApi = SWAPIFactory.create(new SWAPIConfig() {});
        this.applicationApi = (ApplicationModule) this.swApi.initializeModule(ApplicationModule.class);

        this.applicationApi.changeStatus(Deployment.Status.STARTING);

        this.commandManager = new CommandManager();
        commandManager.addCommand(new HelpCommand("help", "List of available commands", "h"));
        commandManager.addCommand(new EndCommand("end", "Stops the load balancer", "stop", "exit"));
        commandManager.addCommand(new DebugCommand("debug", "Turns the debug mode on/off", "d"));
        commandManager.addCommand(new StatsCommand("stats", "Shows live stats", "s"));

        this.webserverManager = new WebserverManager(this.applicationApi.getHost(), this.applicationApi.getPort());
        this.webserverManager.start();

        this.applicationApi.changeStatus(Deployment.Status.RUNNING);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() throws LifecycleException {

        this.applicationApi.changeStatus(Deployment.Status.TERMINATING);

        logger.info("Webserver is going to be stopped");

        // Close the scanner
        scanner.close();

        this.webserverManager.stop();

        this.applicationApi.destroy();

        logger.info("Webserver has been stopped");
    }

    public void console() {

        scanner = new Scanner(System.in);

        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                if (!line.isEmpty()) {
                    String[] split = ARGS_PATTERN.split(line);

                    if (split.length == 0) {
                        continue;
                    }

                    // Get the command name
                    String commandName = split[0].toLowerCase();

                    // Try to get the command with the name
                    Command command = commandManager.findCommand(commandName);

                    if (command != null) {
                        logger.info("Executing command: {}", line);

                        String[] cmdArgs = Arrays.copyOfRange(split, 1, split.length);
                        command.execute(cmdArgs);
                    } else {
                        logger.info("Command not found!");
                    }
                }
            }
        } catch (IllegalStateException ignore) {}
    }

    public void changeDebug(Level level) {

        // Set the log level to debug or info based on the config value
        rootLogger.setLevel(level);

        logger.info("Logger level is now {}", rootLogger.getLevel());
    }

    public void changeDebug() {

        // Change the log level based on the current level
        changeDebug((rootLogger.getLevel() == Level.INFO) ? Level.DEBUG : Level.INFO);
    }
}
