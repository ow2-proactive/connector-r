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
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.rosuda.REngine.Rserve.RConnection;


/**
 * simple class that start Rserve locally if it's not running already - see mainly <code>checkLocalRserve</code> method. It spits out quite some debugging outout of the console, so feel free to modify it for your application if desired.<p>
 * <i>Important:</i> All applications should shutdown every Rserve that they started! Never leave Rserve running if you started it after your application quits since it may pose a security risk. Inform the user if you started an Rserve instance.
 *
 * from : https://github.com/s-u/REngine/blob/master/Rserve/test/StartRserve.java (with minor modifications)
 */
public class StartRserve {

    private static final Logger logger = Logger.getLogger(StartRserve.class);

    public static String DEFAULT_REPOS = "http://cran.irsn.fr/";

    public static Process rProcess;

    /**
     * R batch to check Rserve is installed
     *
     * @param Rcmd command necessary to start R
     * @return Rserve is already installed
     */
    public static boolean isRserveInstalled(String Rcmd) {
        StringBuffer result = new StringBuffer();
        boolean done = doInR("i=installed.packages();is.element(set=i,el='Rserve')",
                             Rcmd,
                             "--vanilla -q",
                             true,
                             result,
                             result);
        if (!done) {
            return false;
        }
        //System.err.println("output=\n===========\n" + result.toString() + "\n===========\n");
        if (result.toString().contains("TRUE")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * R batch to install Rserve
     *
     * @param Rcmd       command necessary to start R
     * @param http_proxy http://login:password@proxy:port string to enable internet access to rforge server
     * @return success
     */
    public static boolean installRserve(String Rcmd, String http_proxy, String repository) {
        if (repository == null || repository.length() == 0) {
            repository = DEFAULT_REPOS;
        }
        logger.info("Install Rserve from " + repository + " ... (http_proxy=" + http_proxy + ") ");
        StringBuffer out = new StringBuffer();
        StringBuffer err = new StringBuffer();
        boolean ok = doInR((http_proxy != null ? "Sys.setenv(http_proxy=" + http_proxy + ");" : "") +
                           "install.packages('Rserve',repos='" + repository + "')", Rcmd, "--vanilla", true, out, err);
        if (!ok) {
            logger.error("RServe installation failed");
            return false;
        }
        int n = 5;
        while (n > 0) {
            try {
                Thread.sleep(10000 / n);
                logger.info(".");
            } catch (InterruptedException ex) {
            }
            if (isRserveInstalled(Rcmd)) {
                logger.info("R Server installed");
                return true;
            }
            n--;
        }
        logger.error("RServe installation failed.");
        logger.error("R process output stream : ");
        logger.error(out.toString());
        logger.error("R process error stream : ");
        logger.error(err.toString());
        return false;
    }

    /**
     * attempt to start Rserve. Note: parameters are <b>not</b> quoted, so avoid using any quotes in arguments
     *
     * @param todo  command to execute in R
     * @param Rcmd  command necessary to start R
     * @param rargs arguments are are to be passed to R (e.g. --vanilla -q)
     * @return <code>true</code> if Rserve is running or was successfully started, <code>false</code> otherwise.
     */
    public static boolean doInR(String todo, String Rcmd, String rargs, boolean wait, StringBuffer out,
            StringBuffer err) {
        try {
            boolean isWindows = false;
            String osname = System.getProperty("os.name");
            String command = null;
            if (osname != null && osname.length() >= 7 && osname.substring(0, 7).equals("Windows")) {
                isWindows = true; /* Windows startup */
                command = "\"" + Rcmd + "\" -e \"" + todo + "\" " + rargs;
                rProcess = Runtime.getRuntime().exec(command);
            } else /* unix startup */ {
                command = "echo \"" + todo + "\"|" + Rcmd + " " + rargs;
                rProcess = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command });
            }
            logger.info("  executing " + command);
            // we need to fetch the output - some platforms will die if you don't ...
            Utils.StreamHog error = new Utils.StreamHog(rProcess.getErrorStream(), (err != null));
            Utils.StreamHog output = new Utils.StreamHog(rProcess.getInputStream(), (out != null));
            if (wait) {
                if (err != null) {
                    error.join();
                }
                if (out != null) {
                    output.join();
                }
                if (!isWindows) /* on Windows the process will never return, so we cannot wait */ {
                    rProcess.waitFor();
                }
                if (out != null) {
                    out.append(output.getOutput());
                }
                if (err != null) {
                    err.append(error.getOutput());
                }
            }
        } catch (Exception x) {
            logger.error(x);
            return false;
        }
        return true;
    }

    /**
     * shortcut to <code>launchRserve(cmd, "--no-save --slave", "--no-save --slave", false)</code>
     */
    public static boolean launchRserve(String cmd) {
        return launchRserve(cmd, /* null, */ "--no-save --slave", null, "--no-save --slave", false, false, -1);
    }

    /**
     * attempt to start Rserve. Note: parameters are <b>not</b> quoted, so avoid using any quotes in arguments
     *
     * @param cmd      command necessary to start R
     * @param rargs    arguments are are to be passed to R
     * @param rsrvargs arguments to be passed to Rserve
     * @return <code>true</code> if Rserve is running or was successfully started, <code>false</code> otherwise.
     */
    public static boolean launchRserve(String cmd, /* String libloc, */ String rargs, File preloadFile, String rsrvargs,
            boolean daemon, boolean debug, int timeout) {
        logger.info("Waiting for Rserve to start ...");
        boolean startRserve;
        if (daemon) {
            startRserve = doInR("library(Rserve);" +
                                (preloadFile != null ? "source('" + Utils.toRpath(preloadFile) + "');" : "") +
                                "Rserve(" + (debug ? "TRUE" : "FALSE") + ",args='" + rsrvargs + "')",
                                cmd,
                                rargs,
                                false,
                                null,
                                null);
        } else {
            startRserve = doInR("library(Rserve);" +
                                (preloadFile != null ? "source('" + Utils.toRpath(preloadFile) + "');" : "") +
                                "run.Rserve(" + rsrvargs + ")", cmd, rargs, false, null, null);
        }
        if (startRserve) {
            logger.info("Rserve startup done, let us try to connect ...");
        } else {
            return false;
        }

        // try up to 120 seconds before giving up. We can be conservative here, because at this point the process execution
        // itself was successful and the startup is usually asynchronous
        int attempts = (timeout > 0 ? timeout : 120);
        while (attempts > 0) {
            try {
                RConnection c = null;
                int port = -1;
                if (rsrvargs.contains("--RS-port")) {
                    String rsport = rsrvargs.split("--RS-port")[1].trim().split(" ")[0];
                    port = Integer.parseInt(rsport);
                } else if (rsrvargs.contains("port")) {
                    String rsport = rsrvargs.split("port")[1].trim().split("=")[1].trim().split(",")[0].trim();
                    port = Integer.parseInt(rsport);
                }
                logger.info("Trying to connect to RServe on port : " + port);
                String loopback = InetAddress.getLoopbackAddress().getHostAddress();
                if (rsrvargs.contains("--RS-port") || rsrvargs.contains("port")) {
                    c = new RConnection(loopback, port);
                } else {
                    c = new RConnection(loopback);
                }
                logger.info("Rserve is running.");
                c.close();
                return true;
            } catch (Exception e2) {
                logger.debug("Try failed with: " + e2.getMessage());
            }
            /* a safety sleep just in case the start up is delayed or asynchronous */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ix) {
            }

            attempts--;
        }
        return false;
    }

    /**
     * checks whether Rserve is running and if that's not the case it attempts to start it using the defaults for the platform where it is run on.
     * This method is meant to be set-and-forget and cover most default setups. For special setups you may get more control over R with <code>launchRserve</code> instead.
     */
    public static boolean checkLocalRserve() {
        if (isRserveRunning()) {
            return true;
        }
        if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
            logger.info("Windows: query registry to find where R is installed ...");
            String installPath = null;
            try {
                installPath = Utils.findRInstallPathWindow();
            } catch (Exception rge) {
                logger.error("ERROR: unable to run REG to find the location of R: " + rge);
                return false;
            }
            if (installPath == null) {
                logger.error("ERROR: canot find path to R. Make sure reg is available and R was installed with registry settings.");
                return false;
            }
            return launchRserve(installPath + "\\bin\\R.exe");
        }
        /* try some common unix locations of R */
        return (launchRserve("R") || launchRserveFromAlternateLocations());
    }

    private static boolean launchRserveFromAlternateLocations() {
        for (String dir : Utils.COMMON_R_INSTALL_DIRS) {
            File fdir = new File(dir);
            if (!fdir.exists() || !fdir.isDirectory())
                continue;
            File rExec = new File(dir, "bin/R");
            if (rExec.exists() && rExec.canRead() && rExec.canExecute()) {
                boolean launched = launchRserve(rExec.getAbsolutePath());
                if (launched)
                    return true;
            }
        }
        return false;
    }

    /**
     * check whether Rserve is currently running (on local machine and default port).
     *
     * @return <code>true</code> if local Rserve instance is running, <code>false</code> otherwise
     */
    public static boolean isRserveRunning() {
        try {
            RConnection c = new RConnection();
            logger.debug("Rserve is running.");
            c.close();
            return true;
        } catch (Exception e) {
            logger.debug("First connect try failed with: " + e.getMessage());
        }
        return false;
    }

    /**
     * just a demo main method which starts Rserve and shuts it down again
     */
    public static void main(String[] args) {
        System.err.println("result=" + checkLocalRserve());
        try {
            RConnection c = new RConnection();
            c.shutdown();
        } catch (Exception x) {
        }
    }
}
