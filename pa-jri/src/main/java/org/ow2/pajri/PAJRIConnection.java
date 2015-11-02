package org.ow2.pajri;

import org.ow2.parengine.PARConnection;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Abstract dialog with JRI sessions.
 *
 * @author Activeeon Team
 */
public class PAJRIConnection implements PARConnection {

    private JRIEngine engine;

    public PAJRIConnection(JRIEngine engine) {
        this.engine = engine;
    }


    @Override
    public REXP engineEval(String expr, ScriptContext ctx) {
        try {
            return engine.parseAndEval(expr);
        } catch (REngineException e) {
            writeMessageToError(e, ctx);
        } catch (REXPMismatchException e) {
            writeExceptionToError(e, ctx);
        }
        return null;
    }

    @Override
    public REXP engineGet(String variable, ScriptContext ctx) {

        if (engineCast(engineEval("exists(\"" + variable + "\")", ctx), Boolean.class, ctx)) {
            try {
                return engine.get(variable, null, true);
            } catch (REngineException e) {
                writeMessageToError(e, ctx);
            } catch (REXPMismatchException e) {
                writeExceptionToError(e, ctx);
            }
        }
        return null;
    }

    @Override
    public void engineSet(String variableName, Object javaValue, ScriptContext ctx) {
        try {
            engine.assign(variableName, RexpConvert.jobj2rexp(javaValue));
        } catch (Exception ex) {
            writeExceptionToError(ex, ctx);
        }
    }

    @Override
    public <T> T engineCast(Object rvalue, Class<T> type, ScriptContext ctx) {
        try {
            return (T) RexpConvert.rexp2jobj((REXP) rvalue, type);
        } catch (REXPMismatchException e) {
            writeExceptionToError(e, ctx);
        }
        return null;
    }


    @Override
    public void checkParsing(String expression, ScriptContext ctx) throws ScriptException {

        try {
            engineSet(".tmp.", expression, ctx);
            REXP r = engineEval("try(parse(text=.tmp.), silent=TRUE)", ctx);
            engineEval("rm(.tmp.)", ctx);
            if (r.inherits("try-error")) throw new ScriptException(r.asString());
        } catch (REXPMismatchException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public void initializeOutput(File outputFile, ScriptContext ctx) {

    }

    @Override
    public void terminateOutput(ScriptContext ctx) {

    }

    @Override
    public void end() {
        engine.close();
    }


    @Override
    public void writeExceptionToError(Exception ex, ScriptContext ctx) {
        Writer contextErrorWriter = ctx.getErrorWriter();
        PrintWriter st = new PrintWriter(contextErrorWriter);
        ex.printStackTrace(st);
        st.flush();
    }

    @Override
    public void writeMessageToError(Exception ex, ScriptContext ctx) {
        Writer contextErrorWriter = ctx.getErrorWriter();
        PrintWriter st = new PrintWriter(contextErrorWriter);
        st.println(ex.getMessage());
        st.flush();
    }
}
