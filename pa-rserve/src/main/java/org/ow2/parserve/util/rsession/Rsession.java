package org.ow2.parserve.util.rsession;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.File;

/**
 * Handles all interactions with the R engine
 *
 * inspired from https://github.com/yannrichet/rsession
 *
 * @author Activeeon Team
 */
public class Rsession {

    public final static int MinRserveVersion = 103;
    public final static String STATUS_NOT_SET = "Unknown status", STATUS_READY = "Ready", STATUS_ERROR = "Error", STATUS_ENDED = "End", STATUS_NOT_CONNECTED = "Not connected", STATUS_CONNECTING = "Connecting...";
    public static final boolean UNIX_OPTIMIZE = true;
    public final static String HEAD_SET = "[set] ";
    public final static String HEAD_EVAL = "[eval] ";
    public final static String HEAD_SERVEREVAL = "[server-eval] ";
    public final static String HEAD_EXCEPTION = "[exception] ";
    public final static String HEAD_ERROR = "[error] ";
    public final static String ROUTPUT_END = "<rsession-end>";
    public final static String IO_HEAD = "[IO] ";

    private static final Logger logger = Logger.getLogger(Rsession.class);
    public static RServeConnectionFactory rserveConnectionFactory;
    public boolean TRY_MODE_DEFAULT = false;
    public boolean TRY_MODE = false;
    protected RConnection connection;
    public boolean connected = false;
    public String name;
    public RServeConf conf;

    public String status = STATUS_NOT_SET;
    boolean tryLocalRServe;

    private String outputFile;
    private boolean sinkActivated = false;

    /**
     * create rsession using System as a logger
     */
    protected Rsession(String name, RServeConf conf) {
        this.name = name;
        this.conf = conf;
        RServeConnectionFactory.initializeOnce(conf);
        begin();
    }


    /**
     * Build a new Rsession, connects to an existing server or fork a new one.
     *
     * @param conf config of RServe
     *             port, login, password, rProperties to pass to R (eg http_proxy)
     */
    public static Rsession newInstanceTry(String name, RServeConf conf) {
        return new Rsession(name, conf);
    }

    /**
     * @return status of Rsession
     */
    public String getStatus() {
        return status;
    }

    void begin() {
        status = STATUS_NOT_CONNECTED;

        status = STATUS_CONNECTING;

        connection = rserveConnectionFactory.connect();
        connected = (connection != null);

        if (!connected) {
            status = STATUS_ERROR;
            String message = "[" + name + "]" + "Rserve " + conf + " is not accessible.";
            logger.error(message);
        } else if (connection.getServerVersion() < MinRserveVersion) {
            status = STATUS_ERROR;
            String message = "[" + name + "]" + "Rserve " + conf + " version is too old.";
            logger.error(message);
        } else {
            status = STATUS_READY;
            return;
        }

        if (!connected) {//failed !

            String message2 = "[" + name + "]" + "Failed to connect to local Rserve. Unable to initialize Rsession.";
            logger.error(message2);
            throw new IllegalArgumentException(message2);
        } else {
            logger.info("[" + name + "]" + "Connected to Local Rserve. (Version " + connection.getServerVersion() + ")");
        }
    }

    /**
     * correctly (depending on execution platform) shutdown Rsession.
     */
    public void end() {
        if (connection == null) {
            logger.info("[" + name + "]" + "Void session terminated.");
            return;
        }

        logger.info("[" + name + "]" + "Ending local session...");
        connection.close();

        logger.info("[" + name + "]" + "Session teminated.");

        connection = null;
    }

    /**
     * Initialize the output file of this session
     *
     * @throws REngineException
     * @throws REXPMismatchException
     */
    public void initializeOutput(File outputFile) throws REngineException, REXPMismatchException {
        this.outputFile = Utils.toRpath(outputFile);
        // Warning, in the following expression, when using sink( ... , type=c('output', 'message'))
        // the message/stderr output is not redirected !
        String expr = ".sink.file.con <- file('" + this.outputFile + "', 'a')\n" +
                "sink(.sink.file.con, append=TRUE, type='output')\n" +
                "sink(.sink.file.con, append=TRUE, type='message')\n";
        eval(expr);
        sinkActivated = true;
    }

    /**
     * Terminate the output file of this session
     *
     * @throws REngineException
     * @throws REXPMismatchException
     */
    public void terminateOutput() throws REXPMismatchException, REngineException {
        String expr = "print(\"" + ROUTPUT_END + "\");\n" +
                "flush.console()\n" +
                "sink();sink(type='message');\n" +
                "flush(.sink.file.con);\n" +
                "close(.sink.file.con);\n" +
                "closeAllConnections()\n";

        // cat("" + ROUTPUT_END + "\n",file=.sink.file.con)
        sinkActivated = false;
        eval(expr);

    }


    /**
     * This method is used to verify that an expression is correctly parsed, it throws error either
     * if there is a problem with the engine, or there is a parsing error
     *
     * @param expression the  expression to parse
     * @throws REngineException
     * @throws REXPMismatchException
     */
    public void checkParsing(String expression) throws REngineException, REXPMismatchException {
        synchronized (connection) {
            set(".tmp.", new REXPString(expression));
            try {
                REXP r = connection.parseAndEval("try(parse(text=.tmp.), silent=TRUE)");
                connection.parseAndEval("rm(.tmp.)");
                if (r.inherits("try-error")) throw new REngineException(connection, r.asString());
            } catch (REngineException e) {
                logger.error("[" + name + "]" + HEAD_EXCEPTION + e.getMessage() + "\n  " + expression);
                throw e;
            } catch (REXPMismatchException e) {
                logger.error("[" + name + "]" + HEAD_EXCEPTION + e.getMessage() + "\n  " + expression);
                throw e;
            }
        }
    }

    /**
     * Evaluate the expression but add a try catch mechanism
     *
     * @param expression
     * @throws REngineException
     * @throws REXPMismatchException
     */
    public void voidEvalWithTry(String expression) throws REngineException, REXPMismatchException {
        logger.info("[" + name + "]" + HEAD_EVAL + expression);
        synchronized (connection) {
            try {
                REXP r = connection.parseAndEval("try({" + expression + "}, silent=TRUE)");
                if (r != null && r.inherits("try-error")) throw new REngineException(connection, r.asString());
            } catch (REngineException e) {
                logger.error("[" + name + "]" + HEAD_EXCEPTION + e.getMessage() + "\n  " + expression);
                throw e;
            } catch (REXPMismatchException e) {
                logger.error("[" + name + "]" + HEAD_EXCEPTION + e.getMessage() + "\n  " + expression);
                throw e;
            }
        }
    }


    /**
     * launch R command and return value.
     *
     * @param expression R expression to evaluate
     * @return REXP R expression
     */
    public REXP eval(String expression) throws REXPMismatchException, REngineException {
        assert connected : "R environment not initialized.";
        if (expression == null) {
            return null;
        }
        if (expression.trim().length() == 0) {
            return null;
        }
        logger.info("[" + name + "]" + HEAD_EVAL + expression);
        REXP e = null;

        try {
            synchronized (connection) {
                e = connection.parseAndEval(expression);
            }
            if (sinkActivated) {
                connection.parseAndEval("flush(.sink.file.con)");
            }
        } catch (REngineException ex) {
            logger.error("[" + name + "]" + HEAD_EXCEPTION + ex.getMessage() + "\n  " + expression);
            throw ex;
        } catch (REXPMismatchException ex) {
            logger.error("[" + name + "]" + HEAD_EXCEPTION + ex.getMessage() + "\n  " + expression);
            throw ex;
        }


        return e;
    }


    /**
     * launch R command on RServe server directly (asynchronous).
     *
     * @param expression R expression to evaluate
     */
    public void serverEval(String expression) throws RserveException {
        assert connected : "R environment not initialized.";
        if (expression == null) {
            return;
        }
        if (expression.trim().length() == 0) {
            return;
        }
        logger.info("[" + name + "]" + HEAD_SERVEREVAL + expression);

        try {
            synchronized (connection) {
                connection.serverEval(expression);
            }
        } catch (RserveException ex) {
            logger.error("[" + name + "]" + HEAD_EXCEPTION + ex.getMessage() + "\n  " + expression);
            throw ex;
        }
    }


    /**
     * Set R object in R env.
     *
     * @param varname R object name
     * @param var     R object value
     */
    public void set(String varname, REXP var) {
        //assert connected : "R environment not initialized. Please make sure that R.init() method was called first.";
        if (!connected) {
            logger.error("[" + name + "]" + HEAD_EXCEPTION + "R environment not initialized. Please make sure that R.init() method was called first.");
            throw new IllegalStateException("R environment not initialized. Please make sure that R.init() method was called first.");
        }

        logger.info("[" + name + "]" + HEAD_SET + varname + " <- " + var);

        if (var == null) {
            throw new NullPointerException("var object must not be null");
        } else {
            try {
                synchronized (connection) {
                    connection.assign(varname, var);
                }
            } catch (RserveException ex) {
                logger.error("[" + name + "]" + HEAD_EXCEPTION + ex.getMessage() + "\n  set(String varname=" + varname + ",Object (REXP) var)");
                throw new RuntimeException("[" + name + "]" + HEAD_EXCEPTION + ex.getMessage() + "\n  set(String varname=" + varname + ",Object (REXP) var)", ex);
            }
        }
    }
}
