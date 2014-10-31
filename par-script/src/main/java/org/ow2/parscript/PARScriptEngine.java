package org.ow2.parscript;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.AbstractScriptEngine;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.TaskScript;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPJavaReference;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineCallbacks;
import org.rosuda.REngine.REngineException;
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
     * Initially we don't know how many messages will be callbacked
     */
    private final LinkedList<String> callbackedErrorMessages;
    /**
     * The instance of factory that has created this engine
     */
    private PARScriptFactory factory;
    /**
     * Underlying R implementation.
     */
    private REngine engine;

    /**
     * Create a instance of the JREngine by reflection. This method is not
     * thread-safe.
     *
     * @return the instance of the engine
     */
    public static PARScriptEngine create(PARScriptFactory factory) {
        // Create the JRI engine by reflection
        String cls = "org.rosuda.REngine.JRI.JRIEngine";
        String[] args = {"--vanilla", "--slave"};

        PARScriptEngine paRengine = new PARScriptEngine(factory);
        try {
            paRengine.engine = REngine.engineForClass(cls, args, paRengine, /* runREPL */
                    false);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate the REngine by reflection", e);
        }
        return paRengine;
    }

    protected PARScriptEngine(PARScriptFactory factory) {
        this.factory = factory;
        this.callbackedErrorMessages = new LinkedList<String>();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        // Transfer all bindings from context into the rengine env
        if (context == null) {
            throw new ScriptException("No script context specified");
        }
        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        if (bindings == null) {
            throw new ScriptException("No bindings specified in the script context");
        }

        // Assign all script task related objects
        this.assignArguments(bindings);
        this.assignProgress(bindings);
        this.assignResults(bindings);
        this.assignLocalSpace(bindings);
        this.assignUserSpace(bindings);
        this.assignGlobalSpace(bindings);
        this.assignInputSpace(bindings);
        this.assignOutputSpace(bindings);
        Map<String,Serializable> variablesMap = this.assignVariables(bindings);

        Object resultValue;
        try {
            REXP rexp = engine.parseAndEval(script);
            // If the 'result' variable is explicitly defined in the global
            // environment it is considered as the task result instead of the
            // result exp

            REXP resultRexp = engine.get(TaskScript.RESULT_VARIABLE, null, true);
            if (resultRexp != null) {
                resultValue = RexpConvert.rexp2jobj(resultRexp);
            } else {
                resultValue = RexpConvert.rexp2jobj(rexp);
            }
            if (resultValue == null) {
                resultValue = true; // TaskResult.getResult() returns true by
                // default
            }
            bindings.put(TaskScript.RESULT_VARIABLE, resultValue);
                        
            // Retrieve variables map from R and merge them with the java one
            if (variablesMap != null) {
                REXP variablesRexp = engine.get(TASK_SCRIPT_VARIABLES, null, true);
                Map newMap = RexpConvert.asMap(variablesRexp);
                variablesMap.putAll(newMap);
            }
        } catch (Exception rme) {
            rme.printStackTrace();
            throw new ScriptException(rme);
        }

        if (!this.callbackedErrorMessages.isEmpty()) {
            String mess = Joiner.on(System.getProperty("line.separator")).join(this.callbackedErrorMessages);
            throw new ScriptException(mess);
        }

        return resultValue;
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

    private void assignArguments(Bindings bindings) {
        String[] args = (String[]) bindings.get(Script.ARGUMENTS_NAME);
        if (args == null) {
            return;
        }
        try {
            engine.assign("args", new REXPString(args));
        } catch (REXPMismatchException e) {
            e.printStackTrace();
        } catch (REngineException e) {
            e.printStackTrace();
        }
    }

    private void assignProgress(Bindings bindings) {
        AtomicInteger progress = (AtomicInteger) bindings.get(TaskScript.PROGRESS_VARIABLE);
        if (progress == null) {
            return;
        }
        try {
            engine.parseAndEval("{ library(rJava); .jinit() }");
            engine.assign("jTaskProgress", new REXPJavaReference(progress));
            engine.parseAndEval("set_progress = function(x) { .jcall(jTaskProgress, \"V\", \"set\", as.integer(x)) }");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignResults(Bindings bindings) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Map<String, Serializable> assignVariables(Bindings bindings) {
         Map<String, Serializable> variables = (Map<String, Serializable>)bindings.get(TASK_SCRIPT_VARIABLES);
         if (variables != null) {
             try {
                 REXP rexp = RexpConvert.jobj2rexp(variables);
                 engine.assign(TASK_SCRIPT_VARIABLES, rexp);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return variables;
    }

    /**
     * Sets a the variable 'localspace' variable in the env and the working dir
     * to the local space of the task.
     */
    private void assignLocalSpace(Bindings bindings) {
        DataSpacesFileObject dsfo = (DataSpacesFileObject) bindings.get(DS_SCRATCH_BINDING_NAME);
        if (dsfo == null) {
            return;
        }
        try {
            String path = convertToRPath(dsfo);
            engine.parseAndEval("setwd('" + path + "')");
            engine.assign("localspace", new REXPString(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignUserSpace(Bindings bindings) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignGlobalSpace(Bindings bindings) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignInputSpace(Bindings bindings) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assignOutputSpace(Bindings bindings) {
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
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void RWriteConsole(REngine eng, String msg, int otype) {
        Writer writer;
        if (otype != 0) {
            // The message should be something like "Error: "
            this.callbackedErrorMessages.add(msg);
            writer = getContext().getErrorWriter();
        } else {
            writer = getContext().getWriter();
        }
        try {
            writer.write(msg);
            writer.flush();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    // REngineOutputInterface methods
    @Override
    public void RFlushConsole(REngine eng) {
        Writer writer = getContext().getWriter();
        try {
            writer.flush();
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
}
