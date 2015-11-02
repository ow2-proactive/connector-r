package org.ow2.parserve.util.rsession;

import org.apache.log4j.Logger;
import org.rosuda.REngine.Rserve.RConnection;

import java.io.File;

/**
 * simple class that start Rserve locally if it's not running already - see mainly <code>checkLocalRserve</code> method. It spits out quite some debugging outout of the console, so feel free to modify it for your application if desired.<p>
 * <i>Important:</i> All applications should shutdown every Rserve that they started! Never leave Rserve running if you started it after your application quits since it may pose a security risk. Inform the user if you started an Rserve instance.
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
        boolean done = doInR("i=installed.packages();is.element(set=i,el='Rserve')", Rcmd, "--vanilla -q", true, result, result);
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
        boolean ok = doInR((http_proxy != null ? "Sys.setenv(http_proxy=" + http_proxy + ");" : "") + "install.packages('Rserve',repos='" + repository + "')", Rcmd, "--vanilla", true, null, null);
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
        logger.error("RServe installation failed");
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
    public static boolean doInR(String todo, String Rcmd, String rargs, boolean wait, StringBuffer out, StringBuffer err) {
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
                rProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
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
        return launchRserve(cmd, /*null,*/ "--no-save --slave", null, "--no-save --slave", false, false, -1);
    }

    /**
     * attempt to start Rserve. Note: parameters are <b>not</b> quoted, so avoid using any quotes in arguments
     *
     * @param cmd      command necessary to start R
     * @param rargs    arguments are are to be passed to R
     * @param rsrvargs arguments to be passed to Rserve
     * @return <code>true</code> if Rserve is running or was successfully started, <code>false</code> otherwise.
     */
    public static boolean launchRserve(String cmd, /*String libloc,*/ String rargs, File preloadFile, String rsrvargs, boolean daemon, boolean debug, int timeout) {
        logger.info("Waiting for Rserve to start ...");
        boolean startRserve;
        if (daemon) {
            startRserve = doInR("library(Rserve);" + (preloadFile != null ? "source('" + Utils.toRpath(preloadFile) + "');" : "") + "Rserve(" + (debug ? "TRUE" : "FALSE") + ",args='" + rsrvargs + "')", cmd, rargs, false, null, null);
        } else {
            startRserve = doInR("library(Rserve);" + (preloadFile != null ? "source('" + Utils.toRpath(preloadFile) + "');" : "") + "run.Rserve(" + rsrvargs + ")", cmd, rargs, false, null, null);
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
                if (rsrvargs.contains("--RS-port") || rsrvargs.contains("port")) {
                    c = new RConnection("localhost", port);
                } else {
                    c = new RConnection("localhost");
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
        String osname = System.getProperty("os.name");
        if (osname != null && osname.length() >= 7 && osname.substring(0, 7).equals("Windows")) {
            logger.info("Windows: query registry to find where R is installed ...");
            String installPath = null;
            try {
                Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
                Utils.RegistryHog regHog = new Utils.RegistryHog(rp.getInputStream(), true);
                rp.waitFor();
                regHog.join();
                installPath = regHog.getInstallPath();
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
        return (launchRserve("R")
                || /* try some common unix locations of R */ ((new File("/Library/Frameworks/R.framework/Resources/bin/R")).exists() && launchRserve("/Library/Frameworks/R.framework/Resources/bin/R"))
                || ((new File("/usr/local/lib/R/bin/R")).exists() && launchRserve("/usr/local/lib/R/bin/R"))
                || ((new File("/usr/lib/R/bin/R")).exists() && launchRserve("/usr/lib/R/bin/R"))
                || ((new File("/usr/local/bin/R")).exists() && launchRserve("/usr/local/bin/R"))
                || ((new File("/sw/bin/R")).exists() && launchRserve("/sw/bin/R"))
                || ((new File("/usr/common/bin/R")).exists() && launchRserve("/usr/common/bin/R"))
                || ((new File("/opt/bin/R")).exists() && launchRserve("/opt/bin/R")));
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
