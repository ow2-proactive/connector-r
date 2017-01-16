/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.parserve.util.rsession;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;


/**
 * Handles the connections to RServe and the start of R sessions
 *
 * @author Activeeon Team
 */
public class RServeConnectionFactory {

    private static final Logger logger = Logger.getLogger(RServeConnectionFactory.class);

    public static RServeConf conf;

    static Rdaemon rServeDaemon;

    public RConnection connection;

    public RServeConnectionFactory(RServeConf conf) {
        this.conf = conf;
    }

    public synchronized static void initializeOnce(RServeConf conf) {
        if (RServeConnectionFactory.conf == null) {
            RServeConnectionFactory.conf = conf;
        }

    }

    /**
     * Returns a connection to the RServe engine
     *
     * @return a RConnection
     */
    public synchronized static RConnection connect() {

        final RServeConnectionFactory instance = new RServeConnectionFactory(conf);

        instance.tryToConnect();

        if (instance.connection == null) {
            startDaemonOnce();
            instance.tryToConnect();
        }

        if (instance.connection != null && instance.connection.isConnected()) {
            if (conf.localRProperties != null && !conf.localRProperties.isEmpty()) {
                for (String p : conf.localRProperties.stringPropertyNames()) {
                    try {
                        instance.connection.eval("Sys.setenv(" + p + "=" + conf.localRProperties.getProperty(p) + ")");
                    } catch (RserveException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return instance.connection;
        } else {
            logger.error("Connection " + conf.toString() + " failed.");
            return null;
        }

    }

    /**
     * initiate a RServe start if an existing server could not be reached
     */
    private static void startDaemonOnce() {
        if (rServeDaemon == null) {
            logger.info("Trying to spawn " + conf.toString());
            rServeDaemon = new Rdaemon(conf);
            String http_proxy = null;
            if (conf.localRProperties != null && conf.localRProperties.containsKey("http_proxy")) {
                http_proxy = conf.localRProperties.getProperty("http_proxy");
            }
            rServeDaemon.startDaemon(http_proxy);
        }
    }

    /**
     * Try to connect to the engine, using the current configuration
     */
    private void tryToConnect() {
        Utils.TimeOut t = new Utils.TimeOut() {

            protected Object defaultResult() {
                return -2;
            }

            protected Object command() {
                try {
                    logger.info("Connecting to " + conf);
                    if (conf.port > 0) {
                        connection = new RConnection(conf.host, conf.port);
                    } else {
                        connection = new RConnection(conf.host);
                    }
                    if (connection.needLogin()) {
                        connection.login(conf.login, conf.password);
                    }
                    logger.info("Connected");

                    return 0;
                } catch (RserveException ex) {
                    logger.error("Failed to connect: " + ex.getMessage());
                    return -1;
                }
            }
        };
        try {
            t.execute(conf.timeout);
        } catch (Exception e) {
            logger.error("  failed: " + e.getMessage());
        }
    }

}
