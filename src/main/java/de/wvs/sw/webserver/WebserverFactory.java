package de.wvs.sw.webserver;

import de.progme.iris.IrisConfig;

/**
 * Created by Marvin Erkes on 05.02.20.
 */
public class WebserverFactory {

    public WebserverFactory() {}

    public static Webserver create(IrisConfig config) {

        return new Webserver(config);
    }
}
