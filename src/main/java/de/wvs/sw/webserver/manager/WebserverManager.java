package de.wvs.sw.webserver.manager;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;

/**
 * Created by Marvin Erkes on 10.05.20.
 */
public class WebserverManager {

    private Tomcat tomcat;

    public WebserverManager(String host, int port) {
        this.tomcat = new Tomcat();
        this.tomcat.setHostname(host);
        this.tomcat.setPort(port);
    }

    public void start() throws LifecycleException, ServletException {
        File baseDirectory = new File("./www");
        this.tomcat.setBaseDir(baseDirectory.getAbsolutePath());
        StandardContext context = (StandardContext) this.tomcat.addWebapp("/", baseDirectory.getAbsolutePath());
        context.getServletContext().setAttribute(Globals.ALT_DD_ATTR, baseDirectory.getAbsolutePath() + "/web.xml");
        this.tomcat.start();
    }

    public void stop() throws LifecycleException {
        this.tomcat.stop();
    }
}
