package com.begentgroup.simplenetwork;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class Utils {

    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    /**
     * Returns a String that is suitable for use as an <code>application/x-www-form-urlencoded</code>
     * list of parameters in an HTTP PUT or HTTP POST.
     *
     * @param parameters  The parameters to include.
     * @param encoding The encoding to use.
     */
    public static String format (
            final List <? extends NameValuePair> parameters,
            final String encoding) {
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            final String encodedName = encode(parameter.getName(), encoding);
            final String value = parameter.getValue();
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0)
                result.append(PARAMETER_SEPARATOR);
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();
    }

    private static String decode (final String content, final String encoding) {
        try {
            return URLDecoder.decode(content,
                    encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    private static String encode (final String content, final String encoding) {
        try {
            return URLEncoder.encode(content,
                    encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    public static final int CLASS_PRIMITIVE = 0;
    public static final int CLASS_ARRAY = 1;
    public static final int CLASS_COLLECTION = 2;
    public static final int CLASS_OBJECT = 3;

    public static int getClassType(Class clazz) {
        if (clazz.isPrimitive() ||
                clazz == Integer.class || clazz == Long.class || clazz == Short.class ||
                clazz == String.class ||
                clazz == Character.class || clazz == Boolean.class ||
                clazz == Float.class || clazz == Double.class) {
            return CLASS_PRIMITIVE;
        } else if (clazz.isArray()) {
            return CLASS_ARRAY;
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return CLASS_COLLECTION;
        }
        return CLASS_OBJECT;
    }

    public static boolean isPrimitive(Class clazz) {
        return getClassType(clazz) == CLASS_PRIMITIVE;
    }

}
