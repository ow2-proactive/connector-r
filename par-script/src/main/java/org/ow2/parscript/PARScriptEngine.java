package org.ow2.parscript;

import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.AbstractScriptEngine;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.parscript.util.RLibPathConfigurator;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPJavaReference;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineCallbacks;
import org.rosuda.REngine.REngineOutputInterface;

/**
 * R implementation of ScriptEngine using REngine through JRI. Sub-class of the
 * RScriptEngine, adds support for types of objects filled into bindings by the
 * ProActive Scheduler ScriptExecutable.
 *
 * @author Activeeon Team
 */
public class PARScriptEngine extends AbstractScriptEngine implements REngineCallbacks, REngineOutputInterface {

    public static final String DS_SCRATCH_BINDING_NAME = "localspace";
    public static final String DS_INPUT_BINDING_NAME = "input";
    public static final String DS_OUTPUT_BINDING_NAME = "output";
    public static final String DS_GLOBAL_BINDING_NAME = "global";
    public static final String DS_USER_BINDING_NAME = "user";
    public static final String TASK_SCRIPT_VARIABLES = "variables";

    /**
     * The instance of factory that has created this engine
     */
    private final PARScriptFactory factory;

    /**
     * Underlying R implementation
     */
    private JRIEngine engine;

    private String lastErrorMessage;

    /**
     * Creates a instance of the PARScriptEngine, that wraps an instance of
     * JRIEngine. This method is not thread-safe.
     *
     * @return the instance of the engine
     */
    public static PARScriptEngine create(PARScriptFactory factory) {
        // System properties are used to store the single instance of the
        // engine to allow sharing an engine between mutliple class loaders
        Properties props = System.getProperties();
        PARScriptEngine eng = (PARScriptEngine) props.get(PARScriptFactory.ENGINE_NAME);
        if (eng != null) {
            return eng;
        }

        // Check if the path to rJava is already setted
        String libPath = System.getProperty("java.library.path");
        if (libPath == null || !libPath.contains("jri")) {
            try {
                RLibPathConfigurator.configureLibraryPath();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to configure the library path for R", e);
            }
        }
        String[] args = {"--vanilla", "--slave"};

        PARScriptEngine e = new PARScriptEngine(factory);
        try {
            e.engine = (JRIEngine) JRIEngine.createEngine(args, e, /* runREPL */ false);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to instantiate the JRIEngine", ex);
        }
        props.put(PARScriptFactory.ENGINE_NAME, e);
        return e;
    }

    protected PARScriptEngine(PARScriptFactory factory) {
        this.factory = factory;
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

        // Assign all script task related objects
        this.enableWarnings(ctx);
        this.assignArguments(bindings, ctx);
        this.assignProgress(bindings, ctx);
        this.assignResults(bindings, ctx);
        this.assignLocalSpace(bindings, ctx);
        this.assignUserSpace(bindings, ctx);
        this.assignGlobalSpace(bindings, ctx);
        this.assignInputSpace(bindings, ctx);
        this.assignOutputSpace(bindings, ctx);
        Map<String, Serializable> variablesMap = this.assignVariables(bindings, ctx);

        try {
            Object resultValue = false;

            REXP rexp = this.engine.parseAndEval(script);

            // PRC-32 A ScriptException() must be thrown if the script calls stop() function
            Exception toThrow = null;
            if (this.lastErrorMessage != null) {
                toThrow = new ScriptException(this.lastErrorMessage);
            }

            // If the 'result' variable is explicitly defined in the global
            // environment it is considered as the task result instead of the
            // result exp
            try {
                REXP resultRexp = engine.get(TaskScript.RESULT_VARIABLE, null, true);
                if (resultRexp != null) {
                    resultValue = RexpConvert.rexp2jobj(resultRexp);
                } else {
                    resultValue = RexpConvert.rexp2jobj(rexp);
                }
                if (resultValue == null) {
                    resultValue = true; // TaskResult.getResult() returns true by default
                }
                bindings.put(TaskScript.RESULT_VARIABLE, resultValue);

                // Retrieve variables map from R and merge them with the java one
                if (variablesMap != null) {
                    REXP variablesRexp = engine.get(TASK_SCRIPT_VARIABLES, null, true);
                    Map newMap = RexpConvert.asMap(variablesRexp);
                    variablesMap.putAll(newMap);
                }
            } catch (Exception ex) {
                this.writeExceptionToError(ex, ctx);
            }

            // PRC-32 A ScriptException() must be thrown if the script calls stop() function
            if (toThrow != null) {
                throw toThrow;
            }

            return resultValue;
        } catch (ScriptException ex) {
            throw ex;
        } catch (Exception ex) {
            this.writeExceptionToError(ex, ctx);
            throw new ScriptException(ex.getMessage());
        } finally {
            // Clear last error message
            this.lastErrorMessage = null;

            // Fix for PRC-30: Always change working dir to avoid keeping a file handle on task temp dir
            try {
                engine.parseAndEval("setwd(tempdir())");
            } catch (Exception ex) {
                this.writeExceptionToError(ex, ctx);
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

    private void enableWarnings(ScriptContext ctx) {
        try {
            engine.parseAndEval("options(warn=1)");
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignArguments(Bindings bindings, ScriptContext ctx) {
        String[] args = (String[]) bindings.get(Script.ARGUMENTS_NAME);
        if (args == null) {
            return;
        }
        try {
            engine.assign("args", new REXPString(args));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignProgress(Bindings bindings, ScriptContext ctx) {
        AtomicInteger progress = (AtomicInteger) bindings.get(TaskScript.PROGRESS_VARIABLE);
        if (progress == null) {
            return;
        }
        try {
            engine.parseAndEval("{ library(rJava); .jinit() }");
            engine.assign("jTaskProgress", new REXPJavaReference(progress));
            engine.parseAndEval("set_progress = function(x) { .jcall(jTaskProgress, \"V\", \"set\", as.integer(x)) }");
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignResults(Bindings bindings, ScriptContext ctx) {
        TaskResult[] results = (TaskResult[]) bindings.get(TaskScript.RESULTS_VARIABLE);
        if (results == null) {
            return;
        }
        Map<String, Object> resultsMap = new HashMap<String, Object>(results.length);
        for (TaskResult r : results) {
            Object value;
            try {
                value = r.value();
            } catch (Throwable e) {
                value = null;
            }
            resultsMap.put(r.getTaskId().getReadableName(), value);
        }
        try {
            REXP rexp = RexpConvert.jobj2rexp(resultsMap);
            engine.assign(TaskScript.RESULTS_VARIABLE, rexp);
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private Map<String, Serializable> assignVariables(Bindings bindings, ScriptContext ctx) {
        Map<String, Serializable> variables = (Map<String, Serializable>) bindings.get(TASK_SCRIPT_VARIABLES);
        if (variables != null) {
            try {
                REXP rexp = RexpConvert.jobj2rexp(variables);
                engine.assign(TASK_SCRIPT_VARIABLES, rexp);
            } catch (Exception ex) {
                writeExceptionToError(ex, ctx);
            }
        }
        return variables;
    }

    /**
     * Sets a the variable 'localspace' variable in the env and the working dir
     * to the local space of the task.
     */
    private void assignLocalSpace(Bindings bindings, ScriptContext ctx) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_SCRATCH_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        try {
            String path = convertToRPath(dsfo);
            engine.parseAndEval("setwd('" + path + "')");
            engine.assign("localspace", new REXPString(path));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignUserSpace(Bindings bindings, ScriptContext ctx) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_USER_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        String path = null;
        try {
            path = convertToRPath(dsfo);
        } catch (Exception e) {
            path = dsfo.getRealURI();
        }
        try {
            engine.assign("userspace", new REXPString(path));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignGlobalSpace(Bindings bindings, ScriptContext ctx) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_GLOBAL_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        String path = null;
        try {
            path = convertToRPath(dsfo);
        } catch (Exception e) {
            path = dsfo.getRealURI();
        }
        try {
            engine.assign("globalspace", new REXPString(path));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignInputSpace(Bindings bindings, ScriptContext ctx) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_INPUT_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        String path = null;
        try {
            path = convertToRPath(dsfo);
        } catch (Exception e) {
            path = dsfo.getRealURI();
        }
        try {
            engine.assign("inputspace", new REXPString(path));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    private void assignOutputSpace(Bindings bindings, ScriptContext ctx) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_OUTPUT_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        String path = null;
        try {
            path = convertToRPath(dsfo);
        } catch (Exception e) {
            path = dsfo.getRealURI();
        }
        try {
            engine.assign("outputspace", new REXPString(path));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    /**
     * R paths are not antislash friendly
     */
    private String convertToRPath(DataSpacesFileObject dsfo) throws Exception {
        String path = dsfo.getRealURI();
        URI uri = new URI(path);
        File f = new File(uri);
        path = f.getCanonicalPath();
        return path.replace("\\", "/");
    }

    /**
     * called when R prints output to the console.
     *
     * @param eng calling engine
     * @param text text to display in the console
     * @param oType output type (0=regular, 1=error/warning)
     */
    @Override
    public void RWriteConsole(REngine eng, String text, int oType) {
        Writer writer = null;
        if (oType == 0) {
            writer = getContext().getWriter();
        } else if (oType == 1) {
            // Intercept error message
            if (text.startsWith("Error:")) {
                this.lastErrorMessage = text;
            }
            writer = getContext().getErrorWriter();
        } else {
            // unkwnown output type
        }
        try {
            writer.write(text);
            writer.flush();
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

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return this.factory;
    }

    /**
     * Write the exception to the error writer
     */
    private void writeExceptionToError(Exception ex, ScriptContext ctx) {
        Writer contextErrorWriter = ctx.getErrorWriter();
        PrintWriter st = new PrintWriter(contextErrorWriter);
        ex.printStackTrace(st);
        st.flush();
    }
}
