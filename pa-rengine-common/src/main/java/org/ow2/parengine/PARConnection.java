package org.ow2.parengine;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.File;

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
