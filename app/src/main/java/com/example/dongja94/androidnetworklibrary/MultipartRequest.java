package com.example.dongja94.androidnetworklibrary;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dongja94 on 2015-12-06.
 */
public class MultipartRequest extends StringRequest {

    private static final String LOG_TAG = "SimpleMultipartEntity";

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final byte[] CR_LF = ("\r\n").getBytes();
    private static final byte[] TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary\r\n"
            .getBytes();

    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private String boundary;
    private byte[] boundaryLine;
    private byte[] boundaryEnd;
    private boolean isRepeatable = false;
    private List<FilePart> fileParts = new ArrayList<FilePart>();

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private int bytesWritten;

    private int totalSize;

    public MultipartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        init();
    }

    public void addPart(final String key, final String value, final String contentType) {
        try {
            out.write(boundaryLine);
            out.write(createContentDisposition(key));
            out.write(createContentType(contentType));
            out.write(CR_LF);
            out.write(value.getBytes());
            out.write(CR_LF);
        } catch (final IOException e) {
            // Shall not happen on ByteArrayOutputStream
            Log.e(LOG_TAG, "addPart ByteArrayOutputStream exception", e);
        }
    }

    public void addPart(final String key, final String value) {
        addPart(key, value, "text/plain; charset=UTF-8");
    }

    public void addPart(String key, File file) {
        addPart(key, file, null);
    }

    public void addPart(final String key, File file, String type) {
        if (type == null) {
            type = APPLICATION_OCTET_STREAM;
        }
        fileParts.add(new FilePart(key, file, type));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type)
            throws IOException {
        if (type == null) {
            type = APPLICATION_OCTET_STREAM;
        }
        out.write(boundaryLine);

        // Headers
        out.write(createContentDisposition(key, streamName));
        out.write(createContentType(type));
        out.write(TRANSFER_ENCODING_BINARY);
        out.write(CR_LF);

        // Stream (file)
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = inputStream.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }

        out.write(CR_LF);
        out.flush();
        try {
            inputStream.close();
        } catch (final IOException e) {
            // Not important, just log it
            Log.w(LOG_TAG, "Cannot close input stream", e);
        }
    }

    private void init() {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        boundary = buf.toString();
        boundaryLine = ("--" + boundary + "\r\n").getBytes();
        boundaryEnd = ("--" + boundary + "--\r\n").getBytes();
    }

    private byte[] createContentType(String type) {
        String result = "Content-Type: " + type + "\r\n";
        return result.getBytes();
    }

    private byte[] createContentDisposition(final String key) {
        return ("Content-Disposition: form-data; name=\"" + key + "\"\r\n")
                .getBytes();
    }

    private byte[] createContentDisposition(final String key, final String fileName) {
        return ("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n")
                .getBytes();
    }

    private class FilePart {
        public File file;
        public byte[] header;

        public FilePart(String key, File file, String type) {
            header = createHeader(key, file.getName(), type);
            this.file = file;
        }

        private byte[] createHeader(String key, String filename, String type) {
            ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            try {
                headerStream.write(boundaryLine);

                // Headers
                headerStream.write(createContentDisposition(key, filename));
                headerStream.write(createContentType(type));
                headerStream.write(TRANSFER_ENCODING_BINARY);
                headerStream.write(CR_LF);
            } catch (IOException e) {
                // Can't happen on ByteArrayOutputStream
                Log.e(LOG_TAG, "createHeader ByteArrayOutputStream exception", e);
            }
            return headerStream.toByteArray();
        }

        public long getTotalLength() {
            long streamLength = file.length() + CR_LF.length;
            return header.length + streamLength;
        }

        public void writeTo(OutputStream out) throws IOException {
            out.write(header);

            FileInputStream inputStream = new FileInputStream(file);
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = inputStream.read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.write(CR_LF);
            out.flush();
            try {
                inputStream.close();
            } catch (final IOException e) {
                // Not important, just log it
                Log.w(LOG_TAG, "Cannot close input stream", e);
            }
        }
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        out.writeTo(outstream);

        for (FilePart filePart : fileParts) {
            filePart.writeTo(outstream);
        }
        outstream.write(boundaryEnd);
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
