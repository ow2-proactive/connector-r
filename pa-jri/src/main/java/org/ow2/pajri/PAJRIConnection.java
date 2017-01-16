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
package org.ow2.pajri;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.ow2.parengine.PARConnection;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;


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
            if (r.inherits("try-error"))
                throw new ScriptException(r.asString());
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
