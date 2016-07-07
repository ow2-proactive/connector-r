package org.ow2.pajri;

import com.google.common.io.CharStreams;
import org.jetbrains.annotations.NotNull;
import org.ow2.parengine.PAREngine;
import org.ow2.parengine.util.RLibPathConfigurator;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.common.task.flow.FlowScript;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.scripting.TaskScript;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineCallbacks;
import org.rosuda.REngine.REngineOutputInterface;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

/**
 * R implementation of ScriptEngine using REngine through JRI. Sub-class of the
 * RScriptEngine, adds support for types of objects filled into bindings by the
 * ProActive Scheduler ScriptExecutable.
 *
 * @author Activeeon Team
 */
public class PAJRIEngine extends PAREngine implements REngineCallbacks, REngineOutputInterface {

    private static String tmpDir = System.getProperty("java.io.tmpdir");


    private static PAJRIEngine instance;
    /**
     * Enabled if this engine is not running inside a forked node
     */
    private final boolean dumpErrorsIfNotForked;

    protected PAJRIEngine(PAJRIFactory factory) {
        this.factory = factory;
        // Fix for PRC-36: With Scheduling 6.0.1 if script tasks are not forked the error output is lost
        this.dumpErrorsIfNotForked = !isInForkedTask();
    }

    /**
     * Creates or retrieves a singleton instance of the PARScriptEngine,
     * that wraps an instance of JRIEngine.
     *
     * @return the singleton instance of the engine
     */
    public static synchronized PAJRIEngine create(PAJRIFactory factory) {
        if (instance == null) {
            instance = createScriptEngine(factory);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    instance.engine.end();
                }
            }));
        }

        return instance;
    }

    private static PAJRIEngine createScriptEngine(PAJRIFactory factory) {
        // Check if the path to rJava is already set
        String libPath = System.getProperty("java.library.path");
        if (libPath == null || !libPath.contains("jri")) {
            try {
                RLibPathConfigurator.configureLibraryPath();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to configure the library path for R", e);
            }
        }
        String[] args = {"--vanilla", "--slave"};

        PAJRIEngine instance = new PAJRIEngine(factory);
        try {
            instance.engine = new PAJRIConnection((JRIEngine) JRIEngine.createEngine(args, instance, false));
        } catch (Exception ex) {
            logger.error("Unable to instantiate the PAJRIEngine", ex);
            throw new IllegalStateException("Unable to instantiate the PAJRIEngine", ex);
        }

        return instance;
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

        Map<String, Serializable> jobVariables = (Map<String, Serializable>) bindings.get(SchedulerConstants.VARIABLES_BINDING_NAME);

        // Assign all script task related objects
        prepareExecution(ctx, bindings);

        engine.checkParsing(script, ctx);

        try {
            Object resultValue = false;
            REXP rexp = this.engine.engineEval(script, ctx);

            // PRC-32 A ScriptException() must be thrown if the script calls stop() function
            Exception toThrow = null;
            if (this.lastErrorMessage != null) {
                toThrow = new ScriptException(this.lastErrorMessage);
            }
            resultValue = retrieveResultVariable(ctx, bindings, rexp);

            retrieveOtherVariable(SelectionScript.RESULT_VARIABLE, ctx, bindings);

            retrieveOtherVariable(FlowScript.loopVariable, ctx, bindings);
            retrieveOtherVariable(FlowScript.branchSelectionVariable, ctx, bindings);
            retrieveOtherVariable(FlowScript.replicateRunsVariable, ctx, bindings);

            this.updateJobVariables(jobVariables, ctx);


            // PRC-32 A ScriptException() must be thrown if the script calls stop() function
            if (toThrow != null) {
                throw toThrow;
            }

            return resultValue;
        } catch (Exception e) {
            engine.writeExceptionToError(e, ctx);
            throw new ScriptException(e);
        } finally {
            // Clear last error message
            this.lastErrorMessage = null;

            // Fix for PRC-30: Always change working dir to avoid keeping a file handle on task temp dir
            engine.engineEval("setwd(\"" + toRpath(tmpDir) + "\")", ctx);
        }
    }

    /**
     * Retrieve another binding from the engine, such as selection, control flow, etc
     */
    private void retrieveOtherVariable(String variableName, ScriptContext ctx, Bindings bindings) {
        // in case the SelectionScript result is assigned in the engine, retrieve it
        REXP ssResultRexp = engine.engineGet(variableName, ctx);
        if (ssResultRexp != null) {
            bindings.put(variableName, engine.engineCast(ssResultRexp, null, ctx));
        }
    }

    @NotNull
    private Object retrieveResultVariable(ScriptContext ctx, Bindings bindings, REXP rexp) {
        Object resultValue;
        // If the 'result' variable is explicitly defined in the global
        // environment it is considered as the task result instead of the
        // result exp
        REXP resultRexp = engine.engineGet(TaskScript.RESULT_VARIABLE, ctx);
        if (resultRexp != null) {
            resultValue = engine.engineCast(resultRexp, null, ctx);
        } else {
            resultValue = engine.engineCast(rexp, null, ctx);
        }
        if (resultValue == null) {
            resultValue = true; // TaskResult.getResult() returns true by default
        }
        bindings.put(TaskScript.RESULT_VARIABLE, resultValue);
        return resultValue;
    }

    @Override
    public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
        String s;
        try {
            s = CharStreams.toString(reader);
        } catch (IOException ex) {
            throw new ScriptException(ex);
        }
        return eval(s, ctx);
    }


    /**
     * called when R prints output to the console.
     *
     * @param eng   calling engine
     * @param text  text to display in the console
     * @param oType output type (0=regular, 1=error/warning)
     */
    @Override
    public void RWriteConsole(REngine eng, String text, int oType) {
        Writer writer = null;

        text = filterErrorsAndProgress(text, false);

        if (oType == 0) {
            writer = getContext().getWriter();
        } else if (oType == 1) {

            writer = getContext().getErrorWriter();

            // Fix for PRC-36: With Scheduling 6.0.1 if script tasks are not forked the error output is lost
            // Dump errors if not inside a forked node
            if (this.dumpErrorsIfNotForked) {
                System.err.print(text);
            }
        }
        try {
            if (writer != null) {
                writer.write(text);
                writer.flush();
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    // REngineOutputInterface methods
    @Override
    public void RFlushConsole(REngine eng) {
        Writer outWriter = getContext().getWriter();
        Writer errWriter = getContext().getErrorWriter();
        try {
            outWriter.flush();
            errWriter.flush();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public void RShowMessage(REngine eng, String msg) {
        Writer writer = getContext().getErrorWriter();
        try {
            writer.write(msg);
            writer.flush();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
