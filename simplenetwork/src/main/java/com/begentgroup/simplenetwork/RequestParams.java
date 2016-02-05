package com.begentgroup.simplenetwork;

import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class RequestParams {

    protected final static String LOG_TAG = "RequestParams";
    protected boolean isRepeatable;
    protected boolean useJsonStreamer;
    protected ConcurrentHashMap<String, String> urlParams;
    protected ConcurrentHashMap<String, StreamWrapper> streamParams;
    protected ConcurrentHashMap<String, List<FileWrapper>> fileParams;
    protected ConcurrentHashMap<String, FileWrapper> fileSingleParams;
    protected ConcurrentHashMap<String, Object> urlParamsWithObjects;
    protected String contentEncoding = HTTP.UTF_8;

    private boolean isForceFormData = false;

    public void setIsForceFormData(boolean isForce) {
        isForceFormData = isForce;
    }

    public void setContentEncoding(final String encoding) {
        if (encoding != null)
            this.contentEncoding = encoding;
        else
            Log.d(LOG_TAG, "setContentEncoding called with null attribute");
    }

    /**
     * Constructs a new empty {@code RequestParams} instance.
     */
    public RequestParams() {
        this((Map<String, String>) null);
    }

    /**
     * Constructs a new RequestParams instance containing the key/value string params from the
     * specified map.
     *
     * @param source the source key/value string map to add.
     */
    public RequestParams(Map<String, String> source) {
        init();
        if (source != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Constructs a new RequestParams instance and populate it with a single initial key/value
     * string param.
     *
     * @param key   the key name for the intial param.
     * @param value the value string for the initial param.
     */
    public RequestParams(final String key, final String value) {
        this(new HashMap<String, String>() {{
            put(key, value);
        }});
    }

    /**
     * Constructs a new RequestParams instance and populate it with multiple initial key/value
     * string param.
     *
     * @param keysAndValues a sequence of keys and values. Objects are automatically converted to
     *                      Strings (including the value {@code null}).
     * @throws IllegalArgumentException if the number of arguments isn't even.
     */
    public RequestParams(Object... keysAndValues) {
        init();
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException("Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    /**
     * Adds a key/value string pair to the request.
     *
     * @param key   the key name for the new param.
     * @param value the value string for the new param.
     */
    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    /**
     * Adds a file to the request.
     *
     * @param key  the key name for the new param.
     * @param file the file to add.
     * @throws java.io.FileNotFoundException throws if wrong File argument was passed
     */
    public void add(String key, File file) throws FileNotFoundException {
        add(key, file, null);
    }

    public void put(String key, File... fileArray) throws FileNotFoundException {
        if (fileArray.length > 1) {
            for (File file : fileArray) {
                add(key, file, null);
            }
        } else if (fileArray.length == 1) {
            put(key, fileArray[0], null);
        }
    }

    public void put(String key, List<File> fileList) throws FileNotFoundException {
        for (File file : fileList) {
            add(key, file, null);
        }
    }

    public void put(String key, String contentType, File... fileArray) throws FileNotFoundException {
        if (fileArray.length > 1) {
            for (File file : fileArray) {
                add(key, file, contentType);
            }
        } else if (fileArray.length == 1) {
            put(key, fileArray[0], contentType);
        }
    }

    public void put(String key, String contentType, List<File> fileList) throws FileNotFoundException {
        for (File file : fileList) {
            add(key, file, contentType);
        }
    }

    /**
     * Adds a file to the request.
     *
     * @param key         the key name for the new param.
     * @param file        the file to add.
     * @param contentType the content type of the file, eg. application/json
     * @throws java.io.FileNotFoundException throws if wrong File argument was passed
     */
    public void put(String key, File file, String contentType) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        if (key != null) {
            fileSingleParams.put(key, new FileWrapper(file, contentType));
        }
    }

    public void add(String key, File file, String contentType) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        if (key != null) {
            List<FileWrapper> fileList = fileParams.get(key);
            if (fileList == null) {
                fileList = new ArrayList<FileWrapper>();
                fileParams.put(key, fileList);
            }
            fileList.add(new FileWrapper(file, contentType));
        }
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key    the key name for the new param.
     * @param stream the input stream to add.
     */
    public void put(String key, InputStream stream) {
        put(key, stream, null);
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key    the key name for the new param.
     * @param stream the input stream to add.
     * @param name   the name of the stream.
     */
    public void put(String key, InputStream stream, String name) {
        put(key, stream, name, null);
    }

    /**
     * Adds an input stream to the request.
     *
     * @param key         the key name for the new param.
     * @param stream      the input stream to add.
     * @param name        the name of the stream.
     * @param contentType the content type of the file, eg. application/json
     */
    public void put(String key, InputStream stream, String name, String contentType) {
        if (key != null && stream != null) {
            streamParams.put(key, new StreamWrapper(stream, name, contentType));
        }
    }

    /**
     * Adds param with non-string value (e.g. Map, List, Set).
     *
     * @param key   the key name for the new param.
     * @param value the non-string value object for the new param.
     */
    public void put(String key, Object value) {
        if (key != null && value != null) {
            if (Utils.isPrimitive(value.getClass())) {
                put(key, value.toString());
            } else {
                urlParamsWithObjects.put(key, value);
            }
        }
    }

    /**
     * Adds string value to param which can have more than one value.
     *
     * @param key   the key name for the param, either existing or new.
     * @param value the value string for the new param.
     */
    public void add(String key, String value) {
        if (key != null && value != null) {
            Object params = urlParamsWithObjects.get(key);
            if (params == null) {
                // Backward compatible, which will result in "k=v1&k=v2&k=v3"
                params = new HashSet<String>();
                this.put(key, params);
            }
            if (params instanceof List) {
                ((List<Object>) params).add(value);
            } else if (params instanceof Set) {
                ((Set<Object>) params).add(value);
            }
        }
    }

    /**
     * Removes a parameter from the request.
     *
     * @param key the key name for the parameter to remove.
     */
    public void remove(String key) {
        urlParams.remove(key);
        streamParams.remove(key);
        fileParams.remove(key);
        fileSingleParams.remove(key);
        urlParamsWithObjects.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("STREAM");
        }

        for (ConcurrentHashMap.Entry<String, List<FileWrapper>> entry : fileParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileSingleParams.entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        List<BasicNameValuePair> params = getParamsList(null, urlParamsWithObjects);
        for (BasicNameValuePair kv : params) {
            if (result.length() > 0)
                result.append("&");

            result.append(kv.getName());
            result.append("=");
            result.append(kv.getValue());
        }

        return result.toString();
    }

    public void setHttpEntityIsRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    public void setUseJsonStreamer(boolean useJsonStreamer) {
        this.useJsonStreamer = useJsonStreamer;
    }

    public RequestBody getRequestBody() throws IOException {
        if (useJsonStreamer) {
            return createJsonStreamerBody();
        } else if (streamParams.isEmpty() && fileParams.isEmpty() && fileSingleParams.isEmpty() && !isForceFormData) {
            return createFormBody();
        } else {
            return createMultipartBody();
        }
    }

    private String convertArrayName(String name) {
        if (name.contains("[")) return name;
        return name + "[]";
    }

    private RequestBody createJsonStreamerBody() throws IOException {
        JsonStreamerRequestBody entity = new JsonStreamerRequestBody();

        // Add string params
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            entity.addPart(entry.getKey(), entry.getValue());
        }

        // Add non-string params
        for (ConcurrentHashMap.Entry<String, Object> entry : urlParamsWithObjects.entrySet()) {
            entity.addPart(entry.getKey(), entry.getValue());
        }

        // Add file params
        for (ConcurrentHashMap.Entry<String, List<FileWrapper>> entry : fileParams.entrySet()) {
            List<FileWrapper> fileWrapperList = entry.getValue();
            for (FileWrapper fileWrapper : fileWrapperList) {
                entity.addPart(convertArrayName(entry.getKey()),
                        new FileInputStream(fileWrapper.file),
                        fileWrapper.file.getName(),
                        fileWrapper.contentType);
            }
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileSingleParams.entrySet()) {
            FileWrapper fileWrapper = entry.getValue();
            entity.addPart(entry.getKey(),
                    new FileInputStream(fileWrapper.file),
                    fileWrapper.file.getName(),
                    fileWrapper.contentType);
        }

        // Add stream params
        for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            StreamWrapper stream = entry.getValue();
            if (stream.inputStream != null) {
                entity.addPart(entry.getKey(),
                        stream.inputStream,
                        stream.name,
                        stream.contentType);
            }
        }

        return entity;
    }

    private RequestBody createFormBody() throws IOException {
        FormEncodingBuilder builder = new FormEncodingBuilder();
        List<BasicNameValuePair> pairs = getParamsList();
        for (BasicNameValuePair pair : pairs) {
            builder.add(pair.getName(), pair.getValue());
        }
        return builder.build();
    }

    private RequestBody createMultipartBody() throws IOException {
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            builder.addFormDataPart(entry.getKey(), entry.getValue());
        }
        List<BasicNameValuePair> params = getParamsList(null, urlParamsWithObjects);
        for (BasicNameValuePair kv : params) {
            builder.addFormDataPart(kv.getName(), kv.getValue());
        }
        for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : streamParams.entrySet()) {
            StreamWrapper stream = entry.getValue();
            if (stream.inputStream != null) {
                builder.addFormDataPart(entry.getKey(), stream.name, create(MediaType.parse(stream.contentType), stream.inputStream));
            }
        }

        for (ConcurrentHashMap.Entry<String, List<FileWrapper>> entry : fileParams.entrySet()) {
            List<FileWrapper> fileWrapperList = entry.getValue();
            for (FileWrapper fileWrapper : fileWrapperList) {
                builder.addFormDataPart(convertArrayName(entry.getKey()), fileWrapper.file.getName(),
                        RequestBody.create(MediaType.parse(fileWrapper.contentType), fileWrapper.file));
            }
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileSingleParams.entrySet()) {
            FileWrapper fileWrapper = entry.getValue();
            builder.addFormDataPart(entry.getKey(), fileWrapper.file.getName(), RequestBody.create(MediaType.parse(fileWrapper.contentType), fileWrapper.file));
        }
        return null;
    }

    public static RequestBody create(final MediaType contentType, final InputStream is) {
        if (is == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                try {
                    return is.available();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(is);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    private void init() {
        urlParams = new ConcurrentHashMap<String, String>();
        streamParams = new ConcurrentHashMap<String, StreamWrapper>();
        fileParams = new ConcurrentHashMap<String, List<FileWrapper>>();
        fileSingleParams = new ConcurrentHashMap<String, FileWrapper>();
        urlParamsWithObjects = new ConcurrentHashMap<String, Object>();
    }

    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

        for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        lparams.addAll(getParamsList(null, urlParamsWithObjects));

        return lparams;
    }

    private List<BasicNameValuePair> getParamsList(String key, Object value) {
        List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            List<String> list = new ArrayList<String>(map.keySet());
            // Ensure consistent ordering in query string
            Collections.sort(list);
            for (String nestedKey : list) {
                Object nestedValue = map.get(nestedKey);
                if (nestedValue != null) {
                    params.addAll(getParamsList(key == null ? nestedKey : String.format("%s[%s]", key, nestedKey),
                            nestedValue));
                }
            }
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (Object nestedValue : list) {
                params.addAll(getParamsList(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            for (Object nestedValue : array) {
                params.addAll(getParamsList(String.format("%s[]", key), nestedValue));
            }
        } else if (value instanceof Set) {
            Set<Object> set = (Set<Object>) value;
            for (Object nestedValue : set) {
                params.addAll(getParamsList(key, nestedValue));
            }
        } else if (value instanceof String) {
            params.add(new BasicNameValuePair(key, (String) value));
        } else if (Utils.isPrimitive(value.getClass())){
            params.add(new BasicNameValuePair(key, value.toString()));
        } else {
            // object ...
        }
        return params;
    }

    protected String getParamString() {
        return Utils.format(getParamsList(), contentEncoding);
    }

    public String getValue(String name) {
        List<BasicNameValuePair> pairs = getParamsList();
        for (BasicNameValuePair pair : pairs) {
            if (pair.getName().equals(name)) {
                return pair.getValue();
            }
        }
        return null;
    }

    public static class FileWrapper {
        public File file;
        public String contentType;

        public FileWrapper(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }

    public static class StreamWrapper {
        public InputStream inputStream;
        public String name;
        public String contentType;

        public StreamWrapper(InputStream inputStream, String name, String contentType) {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
        }
    }
}
