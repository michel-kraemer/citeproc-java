package de.undercouch.citeproc.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utilities for the CSL processor
 * @author Michel Kraemer
 */
public class CSLUtils {
    /**
     * Reads a string from a URL
     * @param u the URL
     * @param encoding the character encoding
     * @return the string
     * @throws IOException if the URL contents could not be read
     */
    public static String readURLToString(URL u, String encoding) throws IOException {
        for (int i = 0; i < 30; ++i) {
            URLConnection conn = u.openConnection();

            // handle HTTP URLs
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection hconn = (HttpURLConnection)conn;

                // set timeouts
                hconn.setConnectTimeout(15000);
                hconn.setReadTimeout(15000);

                // handle redirects
                switch (hconn.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        String location = hconn.getHeaderField("Location");
                        u = new URL(u, location);
                        continue;
                }
            }

            return readStreamToString(conn.getInputStream(), encoding);
        }

        throw new IOException("Too many HTTP redirects");
    }

    /**
     * Reads a string from a stream. Closes the stream after reading.
     * @param is the stream
     * @param encoding the character encoding
     * @return the string
     * @throws IOException if the stream contents could not be read
     */
    public static String readStreamToString(InputStream is, String encoding) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] buf = new byte[1024 * 10];
            int read;
            while ((read = is.read(buf)) >= 0) {
                sb.append(new String(buf, 0, read, encoding));
            }
            return sb.toString();
        } finally {
            is.close();
        }
    }
}
