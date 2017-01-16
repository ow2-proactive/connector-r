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
package org.ow2.parengine;

import java.io.File;

import javax.script.ScriptContext;
import javax.script.ScriptException;


/**
 * Abstract dialog with the actual R Engine implementation.
 *
 * @author Activeeon Team
 */
public interface PARConnection {

    /**
     * Evaluates the given R expression in the underlying R engine implementation
     *
     * @param expr expr to evaluate
     * @param ctx
     * @param <T>  return type expected
     * @return
     */
    <T> T engineEval(String expr, ScriptContext ctx);

    /**
     * Read the given variable in the R engine
     *
     * @param variable
     * @param ctx
     * @param <T>      return type expected
     * @return the given R variable or null if the variable is not present
     */
    <T> T engineGet(String variable, ScriptContext ctx);

    /**
     * Assigns in the R engine a new variable using the given java object value
     * This java object will be converted to R
     *
     * @param variableName name of the variable
     * @param javaValue    java object value
     * @param ctx
     */
    void engineSet(String variableName, Object javaValue, ScriptContext ctx);

    /**
     * Casts the given rObject value (received from eval or get calls) into a Java object
     *
     * @param rvalue r object
     * @param type   type to cast into, conversions will be done for various types
     * @param ctx
     * @param <T>
     * @return
     */
    <T> T engineCast(Object rvalue, Class<T> type, ScriptContext ctx);

    /**
     * Checks the parsing of the expression, throws an error if the parsing is incorrect
     *
     * @param expression
     * @param ctx
     * @throws ScriptException
     */
    void checkParsing(String expression, ScriptContext ctx) throws ScriptException;

    /**
     * Initializes the output file to which R will print its output (not needed in all implementations)
     *
     * @param outputFile
     * @param ctx
     */
    void initializeOutput(File outputFile, ScriptContext ctx);

    /**
     * Terminates the output file logging initialized previously
     *
     * @param ctx
     */
    void terminateOutput(ScriptContext ctx);

    /**
     * Terminates the R engine
     */
    void end();

    /**
     * Writes the exception to the script context writer
     *
     * @param ex
     * @param ctx
     */
    void writeExceptionToError(Exception ex, ScriptContext ctx);

    /**
     * Writes only a message to the script context writer
     *
     * @param ex
     * @param ctx
     */
    void writeMessageToError(Exception ex, ScriptContext ctx);
}
