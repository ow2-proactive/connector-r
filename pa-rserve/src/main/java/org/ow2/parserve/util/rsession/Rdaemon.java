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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;


/**
 * Represents a local RServe daemon.
 *
 * @author Activeeon Team
 */
public class Rdaemon {

    private static final Logger logger = Logger.getLogger(Rdaemon.class);

    static File APP_DIR = new File(System.getProperty("user.home") + File.separator + ".Rserve");

    static {
        boolean app_dir_ok = false;
        if (!APP_DIR.exists()) {
            app_dir_ok = APP_DIR.mkdir();
        } else {
            app_dir_ok = APP_DIR.isDirectory() && APP_DIR.canWrite();
        }
        if (!app_dir_ok) {
            System.err.println("Cannot write in " + APP_DIR.getAbsolutePath());
        }
    }

    RServeConf conf;

    Process process;

    public Rdaemon(RServeConf conf, String R_HOME) {
        this.conf = conf;
        Utils.findR_HOME(R_HOME);
        logger.info("Environment variables:\n  " + Utils.R_HOME_KEY + "=" +
                    Utils.R_HOME /* + "\n  " + Rserve_HOME_KEY + "=" + Rdaemon.Rserve_HOME */);
    }

    public Rdaemon(RServeConf conf) {
        this(conf, null);
    }

    public void startDaemon(String http_proxy) {
        if (Utils.R_HOME == null || !(new File(Utils.R_HOME).exists())) {
            throw new IllegalArgumentException("R_HOME environment variable not correctly set.\nYou can set it using 'java ... -D" +
                                               Utils.R_HOME_KEY + "=[Path to R] ...' startup command.");
        }

        if (!conf.isLocal()) {
            throw new UnsupportedOperationException("Unable to start a remote R daemon: " + conf.toString());
        }

        logger.info("checking Rserve is available... ");
        boolean RserveInstalled = StartRserve.isRserveInstalled(Utils.getRcommand());
        if (!RserveInstalled) {
            logger.info("  no");
            RserveInstalled = StartRserve.installRserve(Utils.getRcommand(), http_proxy, null);
            if (RserveInstalled) {
                logger.info("  ok");
            } else {
                logger.error("  failed.");
                String notice = "Please install Rserve manually in your R environment using \"install.packages('Rserve')\" command.";
                logger.error(notice);
                System.err.println(notice);
                return;
            }
        } else {
            logger.info("  ok");
        }

        logger.info("starting R daemon... " + conf);

        try {
            conf.writeConfToFile();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String rServeArgs;
        boolean daemon = "true".equals(conf.additionalConf.getProperty("daemon"));
        boolean debug = "true".equals(conf.additionalConf.getProperty("debug"));

        if (daemon) {
            // In daemon mode (detached), the syntax used to start RServe is Rserve()
            rServeArgs = "--no-save --slave --RS-conf " + Utils.toRpath(conf.getConfFilePath());
        } else {
            // In non-daemon mode, rServe is embedded to the R interpretor used, using the syntax run.Rserve()
            rServeArgs = (conf.port > 0 ? "port = " + conf.port + ", " : "") + "config.file = '" +
                         Utils.toRpath(conf.getConfFilePath()) + "'";
        }
        File preloadFile = null;
        String confPreload = conf.additionalConf.getProperty("source");
        if (confPreload != null) {
            preloadFile = new File(confPreload);
        }
        boolean started = StartRserve.launchRserve(Utils.getRcommand(),
                                                   /* Rserve_HOME + "\\\\..", */ "--no-save --slave",
                                                   preloadFile,
                                                   rServeArgs,
                                                   daemon,
                                                   debug,
                                                   (conf.timeout > 0 ? (int) conf.timeout / 1000 : -1));

        if (started) {
            logger.info("  ok");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    stopDaemon();
                }
            });
        } else {
            logger.error("  failed");
        }
    }

    public void stopDaemon() {
        logger.info("stopping R daemon... " + conf);
        if (!conf.isLocal()) {
            logger.warn("Not authorized to stopDaemon a remote R daemon: " + conf.toString());
            throw new UnsupportedOperationException("Not authorized to stopDaemon a remote R daemon: " +
                                                    conf.toString());
        }

        try {
            RConnection s = RServeConnectionFactory.connect();
            if (s == null || !s.isConnected()) {
                logger.info("R daemon already stoped.");
                return;
            }
            s.shutdown();

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        logger.info("R daemon stopped.");
    }

}
