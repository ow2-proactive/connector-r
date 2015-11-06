package org.ow2.parserve.util.rsession;

import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Various utility functions used by the rsession package
 *
 * @author Activeeon Team
 */
public class Utils {

    public final static String R_HOME_KEY = "R_HOME";
    public static final String[] COMMON_R_INSTALL_DIRS = {
            "/Library/Frameworks/R.framework/Resources",
            "/usr/local/lib/R",
            "/usr/lib/R",
            "/usr/local",
            "/sw",
            "/usr/common",
            "/opt"
    };

    public static String R_HOME = null;
    static String separator = ",";

    private static final Logger logger = Logger.getLogger(Utils.class);

    public static boolean isPortAvailable(int p) {
        try {
            ServerSocket test = new ServerSocket(p);
            test.close();
        } catch (BindException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * cast R object in java object
     *
     * @param eval REXP R object
     * @return java object
     * @throws REXPMismatchException
     */
    public static Object cast(REXP eval) throws REXPMismatchException {
        if (eval == null) {
            return null;
        }
        if (eval.isNumeric()) {
            if (eval.dim() == null || eval.dim().length == 1) {
                double[] array = eval.asDoubles();
                if (array.length == 0) {
                    return null;
                }
                if (array.length == 1) {
                    return array[0];
                }
                return array;
            } else {
                double[][] mat = eval.asDoubleMatrix();
                if (mat.length == 0) {
                    return null;
                } else if (mat.length == 1) {
                    if (mat[0].length == 0) {
                        return null;
                    } else if (mat[0].length == 1) {
                        return mat[0][0];
                    } else {
                        return mat[0];
                    }
                } else {
                    if (mat[0].length == 0) {
                        return null;
                    } else if (mat[0].length == 1) {
                        double[] dmat = new double[mat.length];
                        for (int i = 0; i < dmat.length; i++) {
                            dmat[i] = mat[i][0];
                        }
                        return dmat;
                    } else {
                        return mat;
                    }
                }
            }
        }

        if (eval.isString()) {
            String[] s = eval.asStrings();
            if (s.length == 1) {
                return s[0];
            } else {
                return s;
            }
        }

        if (eval.isLogical()) {
            return eval.asInteger() == 1;
        }

        if (eval.isList()) {
            return eval.asList();
        }

        if (eval.isNull()) {
            return null;
        } else {
            System.err.println("Unsupported type: " + eval.toDebugString());
        }
        return eval.toString();
    }

    /**
     * create a R list with given R strings
     *
     * @param vars R strings
     * @return String list expression
     */
    public static String buildListString(String... vars) {
        if (vars.length > 1) {
            StringBuffer b = new StringBuffer("c(");
            for (String v : vars) {
                b.append("'" + v + "',");
            }

            return b.substring(0, b.length() - 1) + ")";
        } else {
            return "'" + vars[0] + "'";
        }
    }

    /**
     * Map java File object to R path (as string)
     *
     * @param path java File object
     */
    public static String toRpath(File path) {
        return toRpath(path.getAbsolutePath());
    }

    /**
     * Map java path to R path (as string)
     *
     * @param path java string path
     */
    public static String toRpath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    // <editor-fold defaultstate="collapsed" desc="Conveniency static String methods">
    public static String toString(Object o) {
        if (o == null) {
            return "NULL";
        } else if (o instanceof double[]) {
            return cat((double[]) o);
        } else if (o instanceof double[][]) {
            return cat((double[][]) o);
        } else if (o instanceof int[]) {
            return cat((int[]) o);
        } else if (o instanceof int[][]) {
            return cat((int[][]) o);
        } else if (o instanceof Object[]) {
            return cat((Object[]) o);
        } else if (o instanceof Object[][]) {
            return cat((Object[][]) o);
        } else if (o instanceof RList) {
            return cat((RList) o);
        } else {
            return o.toString();
        }
    }

    public static String cat(RList list) {
        try {
            StringBuffer sb = new StringBuffer("\t");
            double[][] data = new double[list.names.size()][];
            for (int i = 0; i < list.size(); i++) {
                String n = list.keyAt(i);
                sb.append(n + "\t");
                data[i] = list.at(n).asDoubles();
            }
            sb.append("\n");
            for (int i = 0; i < data[0].length; i++) {
                sb.append((i + 1) + "\t");
                for (int j = 0; j < data.length; j++) {
                    sb.append(data[j][i] + "\t");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (REXPMismatchException r) {
            return "(Not a numeric dataframe)\n" + new REXPList(list).toDebugString();
        }
    }

    public static String cat(double[] array) {
        if (array == null || array.length == 0) {
            return "NA";
        }

        String o = array[0] + "";
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += (separator + (array[i] + ""));
            }
        }
        return o;
    }

    public static String cat(int[] array) {
        if (array == null || array.length == 0) {
            return "NA";
        }

        String o = array[0] + "";
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += (separator + (array[i] + ""));
            }
        }
        return o;
    }

    public static String cat(double[][] array) {
        if (array == null || array.length == 0 || array[0].length == 0) {
            return "NA";
        }

        String o = cat(array[0]);
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += "\n" + cat(array[i]);
            }
        }
        return o;
    }

    public static String cat(int[][] array) {
        if (array == null || array.length == 0 || array[0].length == 0) {
            return "NA";
        }

        String o = cat(array[0]);
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += "\n" + cat(array[i]);
            }
        }
        return o;
    }

    public static String cat(Object[] array) {
        if (array == null || array.length == 0 || array[0] == null) {
            return "";
        }

        String o = array[0].toString();
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += (separator + (array[i] == null ? "" : array[i].toString()));
            }
        }

        return o;
    }

    public static String cat(String sep, String[] array) {
        if (array == null || array.length == 0 || array[0] == null) {
            return "";
        }

        String o = array[0].toString();
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += (sep + (array[i] == null ? "" : array[i].toString()));
            }
        }

        return o;
    }

    public static String cat(Object[][] array) {
        if (array == null || array.length == 0 || array[0].length == 0) {
            return "NA";
        }

        String o = cat(array[0]);
        if (array.length > 1) {
            for (int i = 1; i < array.length; i++) {
                o += "\n" + cat(array[i]);
            }
        }
        return o;
    }

    public static String findRInstallPathWindow() throws IOException, InterruptedException {
        Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
        Utils.RegistryHog regHog = new Utils.RegistryHog(rp.getInputStream(), true);
        rp.waitFor();
        regHog.join();
        return regHog.getInstallPath();
    }

    public static String findRInstallPathLinuxMac() {
        for (String dir : COMMON_R_INSTALL_DIRS) {
            File fdir = new File(dir);
            if (fdir.exists() && fdir.isDirectory()) {
                File rExec = new File(dir, "bin/R");
                if (rExec.exists() && rExec.canRead() && rExec.canExecute()) {
                    return fdir.getAbsolutePath();
                }
            }
        }
        return null;
    }

    public static String getRcommand() {
        String suffix = "";
        if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
            suffix = ".exe";
        }
        if ((R_HOME == null) || !(new File(R_HOME).isDirectory())) {
            return "R" + suffix;
        } else {
            return new File(R_HOME, "bin/R").getAbsolutePath() + suffix;
        }
    }

    public static boolean findR_HOME(String r_HOME) {
        Map<String, String> env = System.getenv();
        Properties prop = System.getProperties();

        if (r_HOME != null) R_HOME = r_HOME;
        if (R_HOME == null || !(new File(R_HOME).isDirectory())) {
            if (env.containsKey(R_HOME_KEY)) {
                R_HOME = env.get(R_HOME_KEY);
            }

            if (R_HOME == null || prop.containsKey(R_HOME_KEY) || !(new File(R_HOME).isDirectory())) {
                R_HOME = prop.getProperty(R_HOME_KEY);
            }

            if (R_HOME == null || !(new File(R_HOME).isDirectory())) {
                R_HOME = null;
                if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
                    try {
                        R_HOME = Utils.findRInstallPathWindow();
                    } catch (Exception e) {
                        logger.error("Error when querying registry for R installation",e);
                    }
                } else {
                    R_HOME = findRInstallPathLinuxMac();
                }
            }
        }

        if (R_HOME == null) {
            return false;
        }

        return new File(R_HOME).isDirectory();
    }

    public static String timeDigest() {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        StringBuffer sb = new StringBuffer();
        sb =
                sdf.format(new Date(time), sb, new java.text.FieldPosition(0));
        return sb.toString();
    }

    static void setRecursiveExecutable(File path) {
        for (File f : path.listFiles()) {
            if (f.isDirectory()) {
                f.setExecutable(true);
                setRecursiveExecutable(f);
            } else if (!f.canExecute() && (f.getName().endsWith(".so") || f.getName().endsWith(".dll"))) {
                f.setExecutable(true);
            }
        }

    }

    public abstract static class TimeOut {

        private boolean timedOut = false;
        private Object result = null;

        protected TimeOut() {
        }

        /**
         * @return the result
         */
        public Object getResult() {
            return result;
        }

        public synchronized void execute(long timeout) throws TimeOutException {
            new Thread(new TimeoutThread()).start();

            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                if (getResult() == null) {
                    timedOut = true;
                    result = defaultResult();
                    throw new TimeOutException("timed out");
                } else {
                    return;
                }
            }

            if (getResult() != null) {
                return;
            } else {
                timedOut = true;
                result = defaultResult();
                throw new TimeOutException("timed out");
            }
        }

        protected abstract Object defaultResult();

        protected abstract Object command();

        public class TimeOutException extends Exception {

            public TimeOutException(String why) {
                super(why);
            }
        }

        public class TimeoutThread implements Runnable {

            public void run() {
                Object res = command();
                synchronized (TimeOut.this) {
                    if (timedOut && res != null) {
                    } else {
                        result = res;
                        TimeOut.this.notify();
                    }
                }
            }
        }
    }

    /**
     * helper class that consumes output of a process. In addition, it filter output of the REG command on Windows to look for InstallPath registry entry which specifies the location of R.
     */
    public static class RegistryHog extends Thread {

        public Process rProcess;
        InputStream is;
        boolean capture;
        String installPath;

        RegistryHog(InputStream is, boolean capture) {
            this.is = is;
            this.capture = capture;
            start();
        }

        public String getInstallPath() {
            return installPath;
        }

        public void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (capture) { // we are supposed to capture the output from REG command

                        int i = line.indexOf("InstallPath");
                        if (i >= 0) {
                            String s = line.substring(i + 11).trim();
                            int j = s.indexOf("REG_SZ");
                            if (j >= 0) {
                                s = s.substring(j + 6).trim();
                            }
                            installPath = s;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    static class StreamHog extends Thread {

        InputStream is;
        boolean capture;
        StringBuffer out = new StringBuffer();

        StreamHog(InputStream is, boolean capture) {
            this.is = is;
            this.capture = capture;
            start();
        }

        public String getOutput() {
            return out.toString();
        }

        public void run() {
            BufferedReader br = null;
            InputStreamReader isr = null;
            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (capture) {
                        out.append("\n").append(line);
                    } else {
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
