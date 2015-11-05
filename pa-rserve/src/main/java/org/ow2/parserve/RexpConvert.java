package org.ow2.parserve;

import org.ow2.parengine.RObject;
import org.rosuda.REngine.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Routines to convert between Java Objects and R expressions.
 * <p/>
 * <table>
 * <tr><td> null                                                <td> REXPNull
 * <tr><td> boolean, Boolean, boolean[], Boolean[]              <td> REXPLogical
 * <tr><td> int, Integer, int[], Integer[]                      <td> REXPInteger
 * <tr><td> double, Double, double[], double[][], Double[]	<td> REXPDouble
 * <tr><td> String, String[]					<td> REXPString
 * <tr><td> byte[]						<td> REXPRaw
 * <tr><td> Enum						<td> REXPString
 * <tr><td> Object[], List, Map					<td> REXPGenericVector
 * <tr><td> RObject, java bean (experimental)                   <td> REXPGenericVector
 * </table>
 *
 * @author Martyn Tebby (https://www.rforge.net/rscript/svn.html)
 */
public class RexpConvert {

    /**
     * Treat unsupported types as beans?
     */
    public static boolean useBean;

    /**
     * Handle unsupported types or throw exception?
     */
    public static boolean handleUnsupported;

    /**
     * Convert from R expression to default Java type.
     */
    public static Object rexp2jobj(REXP rexp) throws REXPMismatchException {
        if (rexp == null || rexp.isNull() || rexp instanceof REXPUnknown) {
            return null;
        }
        if (rexp.isVector()) {
            int len = rexp.length();
            if (rexp.isString()) {
                return len == 1 ? rexp.asString() : rexp.asStrings();
            }
            if (rexp.isInteger()) {
                return len == 1 ? rexp.asInteger() : rexp.asIntegers();
            }
            if (rexp.isNumeric()) {
                int[] dim = rexp.dim();
                return (dim != null && dim.length == 2) ? rexp.asDoubleMatrix()
                        : (len == 1) ? rexp.asDouble() : rexp.asDoubles();
            }
            if (rexp.isLogical()) {
                boolean[] bools = ((REXPLogical) rexp).isTRUE();
                return len == 1 ? bools[0] : bools;
            }
            if (rexp.isRaw()) {
                return rexp.asBytes();
            }
        }
        if (rexp.isList()) {
            return rexp.asList().isNamed() ? asMap(rexp) : asList(rexp);
        }
        // missing reference, environment and complex
        // are there any more worth doing ???
        if (handleUnsupported) {
            return rexp;
        }
        throw new RuntimeException("Unsupported REXP type " + rexp);
    }

    /**
     * Convert from R expression to given Java type.
     */
    public static Object rexp2jobj(REXP rexp, Class<?> type) throws REXPMismatchException {
        if (rexp == null || rexp.isNull() || type == Void.TYPE || type == Void.class) {
            return null;
        }
        if (type == null || type == Object.class) {
            return rexp2jobj(rexp);
        }
        if (type == REXP.class) {
            return rexp;
        }
        if (type == String.class) {
            return rexp.asString();
        }
        if (type == Boolean.TYPE || type == Boolean.class) {
            return asBooleans(rexp)[0];
        }
        if (type == Integer.TYPE || type == Integer.class) {
            return rexp.asInteger();
        }
        if (type == Double.TYPE || type == Double.class) {
            return rexp.asDouble();
        }
        if (type == String[].class) {
            return rexp.asStrings();
        }
        if (type == boolean[].class) {
            return asBooleans(rexp);
        }
        if (type == Boolean[].class) {
            return copyArray(asBooleans(rexp), Boolean[].class);
        }
        if (type == int[].class) {
            return rexp.asIntegers();
        }
        if (type == Integer[].class) {
            return copyArray(rexp.asIntegers(), Integer[].class);
        }
        if (type == double[].class) {
            return rexp.asDoubles();
        }
        if (type == Double[].class) {
            return copyArray(rexp.asDoubles(), Double[].class);
        }
        if (type == double[][].class) {
            return rexp.asDoubleMatrix();
        }
        if (type == byte[].class) {
            return rexp.asBytes();
        }
        if (type == RList.class) { // remove ???
            return rexp.asList();
        }
        if (type == List.class) {
            return asList(rexp);
        }
        if (type == Map.class) {
            return asMap(rexp);
        }
        if (type.isArray() && !type.getComponentType().isPrimitive()) {
            return asArray(rexp, type.getComponentType());
        }
        if (type.isEnum()) {
            return asEnum(rexp, type);
        }
        if (RObject.class.isAssignableFrom(type)) {
            return asRObject(rexp, type);
        }
        if (useBean && !type.isPrimitive() && !type.isArray()) {
            return asBean(rexp, type);
        }
        if (handleUnsupported) {
            return null;
        }
        throw new RuntimeException("Unsupported return type " + type);
    }

    /**
     * Convert from R expression to boolean[].
     */
    static boolean[] asBooleans(REXP rexp) {
        return ((REXPLogical) rexp).isTRUE();
    }

    /**
     * Convert from R expression to Java List.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static List asList(REXP rexp) throws REXPMismatchException {
        RList rlist = rexp.asList();
        List list = new ArrayList(rlist.size());
        for (Object o : rlist) {
            list.add(rexp2jobj((REXP) o));
        }
        return list;
    }

    /**
     * Convert from R expression to Java Map.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Map asMap(REXP rexp) throws REXPMismatchException {
        RList rlist = rexp.asList();
        int len = rlist.size();
        Map map = new LinkedHashMap(len * 2);
        if (rlist.isNamed()) {
            for (int i = 0; i < len; ++i) {
                map.put(rlist.names.get(i), rexp2jobj(rlist.at(i)));
            }
        }
        return map;
    }

    /**
     * Convert from R expression to Java array.
     */
    static Object asArray(REXP rexp, Class<?> type) throws REXPMismatchException {
        RList rlist = rexp.asList();
        int len = rlist.size();
        Object array = Array.newInstance(type, len);
        for (int i = 0; i < len; ++i) {
            Object val = rexp2jobj(rlist.at(i), type);
            Array.set(array, i, val);
        }
        return array;
    }

    /**
     * Convert from R expression to Java RObject.
     */
    static Object asRObject(REXP rexp, Class<?> type) {
        try {
            RList rlist = rexp.asList();
            Object obj = type.newInstance();
            if (rlist.isNamed()) {
                for (int i = 0; i < rlist.size(); ++i) {
                    String name = rlist.names.get(i).toString();
                    Field fld = type.getField(name);
                    REXP value = (REXP) rlist.get(i);
                    Object val = rexp2jobj(value, fld.getType());
                    fld.set(obj, val);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert from R expression to Java Bean. No real checking.
     */
    static Object asBean(REXP rexp, Class<?> type) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            Map<String, PropertyDescriptor> map
                    = new HashMap<String, PropertyDescriptor>(props.length * 2);
            for (PropertyDescriptor prop : props) {
                map.put(prop.getName(), prop);
            }
            RList rlist = rexp.asList();
            Object obj = type.newInstance();
            if (rlist.isNamed()) {
                for (int i = 0; i < rlist.size(); ++i) {
                    String name = rlist.names.get(i).toString();
                    PropertyDescriptor prop = map.get(name);
                    Method method = prop.getWriteMethod();
                    REXP value = (REXP) rlist.get(i);
                    Object val = rexp2jobj(value, prop.getPropertyType());
                    method.invoke(obj, val);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert from R expression to Java Enum.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static Enum asEnum(REXP rexp, Class<?> type) throws REXPMismatchException {
        return Enum.valueOf((Class<Enum>) type, rexp.asString());
    }

    /**
     * Convert from Java Object to R expression.
     */
    public static REXP jobj2rexp(Object obj) {
        if (obj == null) {
            return new REXPNull();
        }
        Class<?> cls = obj.getClass();
        if (obj instanceof REXP) {
            return (REXP) obj;
        } else if (obj instanceof int[]) {
            return new REXPInteger((int[]) obj);
        } else if (obj instanceof double[]) {
            return new REXPDouble((double[]) obj);
        } else if (obj instanceof double[][]) {
            return matrix2rexp((double[][]) obj);
        } else if (obj instanceof String[]) {
            return new REXPString((String[]) obj);
        } else if (obj instanceof boolean[]) {
            return new REXPLogical((boolean[]) obj);
        } else if (obj instanceof byte[]) {
            return new REXPRaw((byte[]) obj);
        } else if (obj instanceof Integer) {
            return new REXPInteger((Integer) obj);
        } else if (obj instanceof Double) {
            return new REXPDouble((Double) obj);
        } else if (obj instanceof String) {
            return new REXPString((String) obj);
        } else if (obj instanceof Boolean) {
            return new REXPLogical((Boolean) obj);
        } else if (obj instanceof Enum<?>) {
            return new REXPString(obj.toString());
        } else if (obj instanceof Map<?, ?>) {
            return map2rexp((Map<?, ?>) obj);
        } else if (obj instanceof List<?>) {
            return list2rexp((List<?>) obj);
        } else if (cls.isArray() && !cls.getComponentType().isPrimitive()) {
            return array2rexp(obj);
        } else if (obj instanceof RObject) {
            return robject2rexp(obj);
        } else if (useBean && !cls.isPrimitive() && !cls.isArray()) {
            return bean2rexp(obj);
        } else if (handleUnsupported) {
            return new REXPString(obj.toString());
        }
        throw new IllegalArgumentException("Unsupported arg type " + cls);
    }

    /**
     * Convert from Java double matrix to R expression.
     */
    static REXP matrix2rexp(double[][] mat) {
        int nrow = mat.length;
        int ncol = mat[0].length;
        double[] ret = new double[ncol * nrow];
        for (int i = 0; i < nrow; ++i) {
            for (int j = 0; j < ncol; ++j) {
                ret[j * nrow + i] = mat[i][j];
            }
        }
        RList rlist = new RList();
        rlist.put("dim", new REXPInteger(new int[]{nrow, ncol}));
        REXPList attrs = new REXPList(rlist);
        return new REXPDouble(ret, attrs);
    }

    /**
     * Convert from Java Map to R expression.
     */
    static REXP map2rexp(Map<?, ?> map) {
        int len = map.size();
        String[] names = new String[len];
        REXP[] rexps = new REXP[len];
        int pos = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            names[pos] = entry.getKey().toString();
            rexps[pos] = jobj2rexp(entry.getValue());
            ++pos;
        }
        return namevalues2rexp(names, rexps);
    }

    /**
     * Convert name value pairs to R expression.
     */
    static REXP namevalues2rexp(String[] names, REXP[] rexps) {
        return new REXPGenericVector(new RList(rexps, names));
    }

    /**
     * Convert from Java List to R expression.
     */
    static REXP list2rexp(Collection<?> list) {
        List<REXP> rexps = new ArrayList<REXP>(list.size());
        for (Object o : list) {
            rexps.add(jobj2rexp(o));
        }
        return new REXPGenericVector(new RList(rexps));
    }

    /**
     * Convert from Java Array to R expression.
     */
    static REXP array2rexp(Object array) {
        Class<?> type = array.getClass();
        if (type == Boolean[].class) {
            return jobj2rexp(copyArray(array, boolean[].class));
        }
        if (type == Double[].class) {
            return jobj2rexp(copyArray(array, double[].class));
        }
        if (type == Integer[].class) {
            return jobj2rexp(copyArray(array, int[].class));
        }
        return list2rexp(Arrays.asList((Object[]) array));
    }

    /**
     * Copy array contents to new array type.
     */
    static <T> T copyArray(Object array, Class<T> newType) {
        int length = Array.getLength(array);
        @SuppressWarnings("unchecked")
        T array2 = (T) Array.newInstance(newType.getComponentType(), length);
        for (int i = 0; i < length; ++i) {
            Object value = Array.get(array, i);
            Array.set(array2, i, value);
        }
        return array2;
    }

    /**
     * Convert from Java RObject to R expression.
     */
    static REXP robject2rexp(Object obj) {
        Field[] fields = obj.getClass().getFields();
        int len = fields.length;
        REXP[] rexps = new REXP[len];
        String[] names = new String[len];
        for (int i = 0; i < len; ++i) {
            try {
                names[i] = fields[i].getName();
                rexps[i] = jobj2rexp(fields[i].get(obj));
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae);
            }
        }
        return namevalues2rexp(names, rexps);
    }

    /**
     * Convert from Java Bean to R expression.
     */
    static REXP bean2rexp(Object obj) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            int len = props.length;
            REXP[] rexps = new REXP[len];
            String[] names = new String[len];
            for (int i = 0; i < len; ++i) {
                names[i] = props[i].getName();
                Method method = props[i].getReadMethod();
                Object o = method.invoke(obj);
                rexps[i] = jobj2rexp(o);
            }
            return namevalues2rexp(names, rexps);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
