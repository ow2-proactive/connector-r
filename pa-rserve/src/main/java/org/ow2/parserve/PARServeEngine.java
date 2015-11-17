package org.ow2.parserve;

import com.google.common.io.CharStreams;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.OperatingSystem;
import org.ow2.parengine.PAREngine;
import org.ow2.parserve.util.rsession.RServeConf;
import org.ow2.parserve.util.rsession.Rsession;
import org.ow2.parserve.util.rsession.Utils;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.TaskScript;
import org.ow2.proactive.scripting.helper.progress.ProgressFile;
import org.ow2.proactive.utils.CookieBasedProcessTreeKiller;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.net.InetAddress;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * R implementation of ScriptEngine using REngine through JRI. Sub-class of the
 * RScriptEngine, adds support for types of objects filled into bindings by the
 * ProActive Scheduler ScriptExecutable.
 *
 * @author Activeeon Team
 */
public class PARServeEngine extends PAREngine {

    public static final int PARSERVE_RSERVE_PORT = 6412;
    /**
     * Name of the variable used to trigger a RServe server ansync evaluation
     */
    public static final String PARSERVE_SERVEREVAL = "parserve.servereval";

    /**
     * period of tailer update
     */
    public static final int TAILER_PERIOD = 500;

    /**
     * timeout used to wait for the log file last messages
     */
    public static final int TAILER_TIMEOUT = 1000;
    /**
     * logger
     */
    protected static final Logger logger = Logger.getLogger(PARServeEngine.class);
    public static final String COOKIE_NAME_SUFFIX = "_RServe";
    public static final String NODE_COOKIE_NAME_SUFFIX = "node";

    /**
     * a file containing the configuration of the Rserve server, and of the PARServe integration
     */
    public static File rServePropertyFile = new File(System.getProperty("proactive.home"), "addons/" + PARServeEngine.class.getSimpleName() + ".ini");

    private static RServeConf rServeConf;

    /**
     * file storing the output from R
     */
    private File outputFile;

    /**
     * Thread and listener reading the outputFile
     */
    private Thread tailerThread;
    private PARScriptTailerListener listener;

    /**
     * Evaluation on RServe server instead of Rserve session
     */
    protected boolean serverEval;


    protected PARServeEngine(PARServeFactory factory) {
        this.factory = factory;
        // Fix for PRC-36: With Scheduling 6.0.1 if script tasks are not forked the error output is lost
        if (isInForkedTask()) {
            throw new IllegalStateException("PARServe engine cannot be used in fork mode, check your ProActive Server configuration.");
        }
    }

    /**
     * Creates or retrieves a singleton instance of the PARServeEngine,
     * that wraps an instance of JRIEngine.
     *
     * @return the singleton instance of the engine
     */
    public static synchronized PARServeEngine create(PARServeFactory factory) {
        if (isInForkedTask()) {
            // PARServe engines cannot work in fork mode, as it manages a process attached to the executing Node.
            // Fork mode, would kill and restart this process for each new task
            throw new IllegalStateException("PARServe engine cannot be used in fork mode, check your ProActive Server configuration.");
        }

        return createScriptEngine(factory);
    }

    private static PARServeEngine createScriptEngine(PARServeFactory factory) {


        final PARServeEngine instance = new PARServeEngine(factory);
        try {
            if (instance.rServeConf == null) {
                instance.rServeConf = createConfig();
                instance.initializePTK();
            }

        } catch (Exception ex) {
            logger.error("Unable to instantiate the PARserveEngine",ex);
            throw new IllegalStateException("Unable to instantiate the PARserveEngine", ex);
        }

        return instance;
    }

    /**
     * Creates all configuration data related to Rserve
     *
     * @return
     * @throws IOException
     */
    private static RServeConf createConfig() throws IOException {
        long timeout = -1;
        boolean debug = false;
        boolean daemon = false;
        String login = null;
        String password = null;
        int rServePort = PARSERVE_RSERVE_PORT;
        Properties rServeProperties = null;
        Properties rEnvProperties = null;
        if (rServePropertyFile.exists()) {
            rServeProperties = new Properties();
            rServeProperties.load(new FileReader(rServePropertyFile));
            Set<String> propNames = rServeProperties.stringPropertyNames();
            // filter properties starting with "R." or special properties
            for (String key : propNames) {
                if (key.startsWith("R.")) {
                    String newkey = key.substring(2);
                    if (rEnvProperties == null) {
                        rEnvProperties = new Properties();
                    }
                    rEnvProperties.put(newkey, rServeProperties.remove(key));
                } else if (key.equals("rserve.daemon")) {
                    daemon = Boolean.parseBoolean((String) rServeProperties.remove(key));
                    rServeProperties.remove(key);
                } else if (key.equals("rserve.debug")) {
                    debug = Boolean.parseBoolean((String) rServeProperties.remove(key));
                    rServeProperties.remove(key);
                } else if (key.equals("port")) {
                    rServePort = Integer.parseInt((String) rServeProperties.get("port"));
                } else if (key.equals("rserve.login")) {
                    login = (String) rServeProperties.remove(key);
                } else if (key.equals("rserve.password")) {
                    password = (String) rServeProperties.remove(key);
                } else if (key.equals("rserve.timeout")) {
                    timeout = Long.parseLong((String) rServeProperties.remove(key));
                }
            }
        }
        return new RServeConf(null, rServePort, login, password, timeout, daemon, debug, rServeProperties, rEnvProperties);
    }

    /**
     * This method initialize PTK env variables on Rserve
     *
     * @throws REXPMismatchException
     * @throws REngineException
     */
    private void initializePTK() throws REXPMismatchException, REngineException {

        // deactivate Node process tree killer (by replacing the existing one), to avoid duplicate killing
        final CookieBasedProcessTreeKiller processKillerDeactivated = CookieBasedProcessTreeKiller.createAllChildrenKiller(
                NODE_COOKIE_NAME_SUFFIX);
        final CookieBasedProcessTreeKiller processKiller = CookieBasedProcessTreeKiller.createAllChildrenKiller(
                COOKIE_NAME_SUFFIX);

        Rsession initSession = Rsession.newInstanceTry(PARServeEngine.class.getSimpleName(), rServeConf);

        initSession.eval("Sys.setenv(" + processKiller.getCookieName() + "=\"" + processKiller.getCookieValue() + "\")");
        final int rServePid = initSession.eval("Sys.getpid()").asInteger();

        logger.info("Rserve PID : " + rServePid);

        initSession.end();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Killing PARServe.");
                if (OperatingSystem.getOperatingSystem().equals(OperatingSystem.windows)) {
                    try {
                        // on windows, PTK does not seem to work for RServe, rely on taskkill command.
                        Runtime.getRuntime().exec(new String[]{"taskkill", "/F", "/PID", "" + rServePid, "/T"}).waitFor();
                    } catch (Exception e) {
                        logger.error(e);
                    }
                } else {
                    processKiller.kill();
                }
                logger.info("PARServe killed.");
            }
        });
    }

    @Override
    public Object eval(String script, ScriptContext ctx) throws ScriptException {
        // Transfer all bindings from context into the rengine env
        if (ctx == null) {
            throw new ScriptException("No script context specified");
        }
        Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        if (bindings == null) {
            throw new ScriptException("No bindings specified in the script context");
        }

        serverEval = false;
        Map<String, Serializable> jobVariables = (Map<String, Serializable>) bindings.get(TASK_SCRIPT_VARIABLES);
        if (jobVariables != null) {
            serverEval = "true".equals(jobVariables.get(PARSERVE_SERVEREVAL));
        }

        engine = new PARServeConnection(Rsession.newInstanceTry("Script", rServeConf), serverEval);

        initializeTailer(bindings, ctx);

        Object resultValue = null;

        try {

            if (!serverEval) {
                prepareExecution(ctx, bindings);
            }
            // if there is an exception during the parsing, a ScriptException is immediately thrown
            engine.checkParsing(script, ctx);

            // otherwise each step is followed till the end
            REXP rexp = engine.engineEval(script, ctx);

            // If the 'result' variable is explicitly defined in the global
            // environment it is considered as the task result instead of the
            // result exp
            REXP resultRexp = null;
            if (!serverEval) {
                resultRexp = engine.engineGet(TaskScript.RESULT_VARIABLE, ctx);
                if (resultRexp != null) {
                    resultValue = engine.engineCast(resultRexp, null, ctx);
                } else {
                    resultValue = engine.engineCast(rexp, null, ctx);
                }
            }
            if (resultValue == null) {
                resultValue = true; // TaskResult.getResult() returns true by default
            }
            bindings.put(TaskScript.RESULT_VARIABLE, resultValue);

            // in case the SelectionScript result is assigned in the engine, retrieve it
            if (!serverEval) {
                REXP ssResultRexp = engine.engineGet(SelectionScript.RESULT_VARIABLE, ctx);
                if (ssResultRexp != null) {
                    bindings.put(SelectionScript.RESULT_VARIABLE, engine.engineCast(ssResultRexp, null, ctx));
                }
            }

            this.updateJobVariables(jobVariables, ctx);

            // server evaluation is for one task only, it must not be propagated
            if (serverEval) {
                jobVariables.put(PARSERVE_SERVEREVAL, "false");
            }

            return resultValue;
        } catch (Exception ex) {
            engine.writeExceptionToError(ex, ctx);
            throw new ScriptException(ex.getMessage());
        } finally {
            engine.terminateOutput(ctx);

            if (!serverEval) {
                engine.engineEval("setwd('" + Utils.toRpath(System.getProperty("java.io.tmpdir")) + "')", ctx);
            }
            engine.end();

            terminateTailer();

            if (!serverEval) {
                // PRC-32 A ScriptException() must be thrown if the script calls stop() function
                ScriptException toThrow = null;
                if (lastErrorMessage != null) {
                    toThrow = new ScriptException(lastErrorMessage);
                }
                if (toThrow != null) {
                    throw toThrow;
                }
            }
        }
    }

    /**
     * Initialize the Tailer thread used to read R output
     */
    private void initializeTailer(Bindings bindings, ScriptContext ctx) throws ScriptException {
        if (!serverEval) {
            try {
                Tailer tailer = null;
                outputFile = createOuputFile(bindings);

                listener = new PARScriptTailerListener(ctx.getWriter());
                tailer = new Tailer(outputFile, listener, TAILER_PERIOD);

                engine.initializeOutput(outputFile, ctx);

                tailerThread = new Thread(tailer, "PARServeEngine Tailer");
                tailerThread.setDaemon(true);
                tailerThread.start();
            } catch (Exception e) {
                logger.error("Error during tailer init:", e);
                throw new ScriptException(e);
            }
        }
    }

    /**
     * Terminate the Tailer thread used to read R output
     */
    private void terminateTailer() {
        if (!serverEval) {
            try {
                tailerThread.join(TAILER_TIMEOUT);
            } catch (InterruptedException e) {

            }
            if (tailerThread.isAlive()) {
                tailerThread.interrupt();
            }
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        String s;
        try {
            s = CharStreams.toString(reader);
        } catch (IOException ex) {
            throw new ScriptException(ex);
        }
        return eval(s, context);
    }


    /**
     * This class reads the content of the .Rout file produced by the Rsession as the tail command.
     */
    public class PARScriptTailerListener extends TailerListenerAdapter {

        Writer writer;

        Tailer tailer;

        public PARScriptTailerListener(Writer wr) {
            this.writer = wr;
        }

        @Override
        public void handle(Exception ex) {
            if (ex instanceof InterruptedIOException) {
                try {
                    writer.close();
                } catch (IOException e) {

                }
            }
            super.handle(ex);
        }

        @Override
        public void init(Tailer tailer) {
            super.init(tailer);
            this.tailer = tailer;
        }

        public void handle(String line) {
            try {
                line = filterErrorsAndProgress(line, true);
                if (line.contains(Rsession.ROUTPUT_END)) {
                    writer.close();
                    tailer.stop();
                    return;
                } else {
                    writer.append(line + "\n");
                    writer.flush();
                }

            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }
}
