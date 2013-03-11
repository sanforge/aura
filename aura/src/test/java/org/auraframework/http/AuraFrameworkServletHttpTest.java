/*
 * Copyright (C) 2012 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.http;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.auraframework.Aura;
import org.auraframework.test.AuraHttpTestCase;

/**
 * Automation to verify the implementation of AuraFrameworkServlet.
 * AuraFrameworkServlet responds to requests of pattern /auraFW/* This config is
 * stored in aura/dist/config/web.xml for aura running on jetty. In SFDC build,
 * the config is in main-sfdc/config/aura.conf AuraFrameworkServlet sets
 * resources to be cached for 45 days.
 * 
 * @since 0.0.298
 */
public class AuraFrameworkServletHttpTest extends AuraHttpTestCase {
    public final String sampleBinaryResourcePath = "/auraFW/resources/aura/auraIdeLogo.png";
    public final String sampleTextResourcePath = "/auraFW/resources/aura/resetCSS.css";
    public final String sampleJavascriptResourcePath = "/auraFW/javascript/aura_dev.js";
    public final String sampleBinaryResourcePathWithNonce = "/auraFW/resources/%s/aura/auraIdeLogo.png";
    public final String sampleTextResourcePathWithNonce = "/auraFW/resources/%s/aura/resetCSS.css";
    private final long timeWindowExpiry = 600000; // ten minute expiration test window

    public AuraFrameworkServletHttpTest(String name) {
        super(name);
    }

    private boolean ApproximatelyEqual(long a, long b, long delta) {
        return (Math.abs(a - b) < delta);
    }

    private SimpleDateFormat getHttpDateFormat() {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    }

    /**
     * Execute a get method and check that we got a long cache response.
     */
    private int checkLongCache(GetMethod get, String mimeType) throws Exception {
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to fetch a valid resource request.", HttpStatus.SC_OK, statusCode);
        assertNotNull(get.getResponseBodyAsString());

        if (mimeType.startsWith("text/")) {
            String responseMime = get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue();

            assertEquals("Framework servlet not responding with correct encoding type.", AuraBaseServlet.UTF_ENCODING,
                    get.getResponseCharSet());
            assertTrue("Framework servlet not responding with correct mime type expected " + mimeType
                    + " got " + responseMime, responseMime.startsWith(mimeType + ";"));
        } else {
            assertEquals("Framework servlet not responding with correct mime type", mimeType,
                    get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue());
        }

        SimpleDateFormat df = getHttpDateFormat();
        Date currentDate = new Date();
        long expirationMillis = (df.parse(get.getResponseHeader(HttpHeaders.EXPIRES).getValue()).getTime()
                - currentDate.getTime());
        assertTrue("AuraFrameworkServlet is not setting the right value for expires header.",
                ApproximatelyEqual(expirationMillis, AuraBaseServlet.LONG_EXPIRE, timeWindowExpiry));
        return statusCode;
    }

    private int checkExpired(GetMethod get, String mimeType) throws Exception {
        int statusCode = getHttpClient().executeMethod(get);
        SimpleDateFormat df = getHttpDateFormat();
        assertEquals("AuraFrameworkServlet failed to return ok.", HttpStatus.SC_OK, statusCode);

        if (mimeType.startsWith("text/")) {
            String responseMime = get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue();

            assertEquals("Framework servlet not responding with correct encoding type.", AuraBaseServlet.UTF_ENCODING,
                    get.getResponseCharSet());
            assertTrue("Framework servlet not responding with correct mime type expected " + mimeType
                    + " got " + responseMime, responseMime.startsWith(mimeType + ";"));
        } else {
            assertEquals("Framework servlet not responding with correct mime type", mimeType,
                    get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue());
        }
        long expirationMillis = df.parse(get.getResponseHeader(HttpHeaders.EXPIRES).getValue()).getTime();
        assertTrue("AuraFrameworkServlet is not setting the right value for expires header.",
                expirationMillis < System.currentTimeMillis());
        return statusCode;
    }

    protected static GetMethod obtainNoncedGetMethod(String noncedPath, boolean fake) throws Exception {
        String nonce;

        if (fake) {
            nonce = "thisisnotanonce";
        } else {
            nonce = Aura.getConfigAdapter().getAuraFrameworkNonce();
        }
        String realPath = String.format(noncedPath, nonce);

        return obtainGetMethod(realPath);
    }

    protected static GetMethod obtainUidedGetMethod(String path, boolean fake) throws Exception {
        String nonce;

        if (fake) {
            nonce = "thisisnotanonce";
        } else {
            nonce = Aura.getConfigAdapter().getAuraFrameworkNonce();
        }
        String realPath = path + "?aura.fwuid=" + nonce;
        return obtainGetMethod(realPath);
    }

    /**
     * Verify that AuraFrameworkServlet can handle bad resource paths. 1. Non
     * existing resource path. 2. Empty resource path. 3. Access to root
     * directory or directory walking.
     */
    public void testBadResourcePaths() throws Exception {
        String[] badUrls = { "/auraFW", "/auraFW/", "/auraFW/root/",
                // BUG "/auraFW/resources/aura/..", Causes a 500
                "/auraFW/resources/aura/../../",
                // BUG "/auraFW/resources/aura/../../../../", causes a 400
                "/auraFW/home/", "/auraFW/resources/aura/home", "/auraFW/resources/foo/bar",
                // Make sure the regex used in implementation doesn't barf
                "/auraFW/resources/aura/resources/aura/auraIdeLogo.png",
                "/auraFW/resources/aura/auraIdeLogo.png/resources/aura/" };
        for (String url : badUrls) {
            int statusCode = getHttpClient().executeMethod(obtainGetMethod(url));
            assertEquals("Expected:" + HttpStatus.SC_NOT_FOUND + " but found " + statusCode + ", when trying to reach:"
                    + url, HttpStatus.SC_NOT_FOUND, statusCode);
        }
    }

    private void verifyResourceAccess(String resourcePath, int expectedResponseStatus, String failureMsg)
            throws Exception {
        GetMethod get = obtainGetMethod(resourcePath);
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals(failureMsg, expectedResponseStatus, statusCode);
    }

    /**
     * Verify that incomplete resource path returns SC_NOT_FOUND(404).
     * Subsequent requests for valid resource on the same path are successful.
     * 
     * @throws Exception
     */
    public void testRequestingFolderAsFileNotAllowed() throws Exception {
        String[] parts = sampleBinaryResourcePath.split("/");
        // Accessing folder(which might have had previous valid access) as file
        String incompletePath = StringUtils.join(Arrays.copyOfRange(parts, 0, parts.length - 1), "/");
        verifyResourceAccess(incompletePath, HttpStatus.SC_NOT_FOUND,
                "Expected server to return a 404 status for folder as file.");

        // Accessing a valid folder
        verifyResourceAccess(incompletePath + "/", HttpStatus.SC_NOT_FOUND,
                "Expected server to return a 404 status for folders(incomplete paths).");

        // Subsequent requests for filed on same path are accepted and serviced
        verifyResourceAccess(sampleBinaryResourcePath, HttpStatus.SC_OK,
                "Expected server to return a 200 status for valid resource.");

    }

    /**
     * Test that AuraFrameworkServlet inspects the date header in the request
     * and sends 304(SC_NOT_MODIFIED) if the If-Modified-Since header indicates
     * that resource is not stale.
     */
    public void testResourceCaching() throws Exception {
        GetMethod get = obtainUidedGetMethod(sampleBinaryResourcePath, false);
        Calendar stamp = Calendar.getInstance();
        stamp.add(Calendar.DAY_OF_YEAR, 45);
        get.setRequestHeader(HttpHeaders.IF_MODIFIED_SINCE,
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(stamp.getTime()));
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals("Expected server to return a 304 for unexpired cache.", HttpStatus.SC_NOT_MODIFIED, statusCode);
        assertNull(get.getResponseBodyAsString());
    }

    /**
     * Verify that AuraFrameworkServlet responds successfully to valid request
     * for a binary resource.
     */
    public void testRequestBinaryResourceWithNonce() throws Exception {
        GetMethod get = obtainNoncedGetMethod(sampleBinaryResourcePathWithNonce, false);
        SimpleDateFormat df = getHttpDateFormat();
        int statusCode;
        checkLongCache(get, "image/png");

        get = obtainNoncedGetMethod(sampleBinaryResourcePathWithNonce, false);
        // set the if modified since to a long time ago.
        get.setRequestHeader("If-Modified-Since", df.format(new Date(1)));
        statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to return not modified.", HttpStatus.SC_NOT_MODIFIED, statusCode);

        get = obtainNoncedGetMethod(sampleBinaryResourcePathWithNonce, false);
        // set the if modified since to the future.
        get.setRequestHeader("If-Modified-Since", df.format(new Date(System.currentTimeMillis() + 24 * 3600 * 1000)));
        statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to return not modified.", HttpStatus.SC_NOT_MODIFIED, statusCode);

        get = obtainNoncedGetMethod(sampleBinaryResourcePathWithNonce, true);
        checkExpired(get, "image/png");
    }

    /**
     * Verify that AuraFrameworkServlet responds successfully to valid request
     * for a binary resource.
     */
    public void testRequestBinaryResourceShortExpire() throws Exception {
        GetMethod get = obtainGetMethod(sampleBinaryResourcePath, false);
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to fetch a valid resource request.", HttpStatus.SC_OK, statusCode);
        assertNotNull(get.getResponseBodyAsString());
        assertEquals("Framework servlet not responding with correct mime type", "image/png",
                get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue());
        SimpleDateFormat df = getHttpDateFormat();
        Date currentDate = new Date();
        long expirationMillis = (df.parse(get.getResponseHeader(HttpHeaders.EXPIRES).getValue()).getTime()
                - currentDate.getTime());
        assertTrue("AuraFrameworkServlet is not setting the right value for expires header.",
                ApproximatelyEqual(expirationMillis, AuraBaseServlet.SHORT_EXPIRE, timeWindowExpiry));
    }

    /**
     * Verify that AuraFrameworkServlet responds successfully to valid request
     * for a text resource.
     */
    public void testRequestTextResourceWithNonce() throws Exception {
        GetMethod get = obtainNoncedGetMethod(sampleTextResourcePathWithNonce, false);
        SimpleDateFormat df = getHttpDateFormat();
        checkLongCache(get, "text/css");

        get = obtainNoncedGetMethod(sampleTextResourcePathWithNonce, false);
        // set the if modified since to a long time ago.
        get.setRequestHeader("If-Modified-Since", df.format(new Date(1)));
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to return not modified.", HttpStatus.SC_NOT_MODIFIED, statusCode);

        get = obtainNoncedGetMethod(sampleTextResourcePathWithNonce, true);
        checkExpired(get, "text/css");
    }

    /**
     * Verify that AuraFrameworkServlet responds successfully to valid request
     * for a text resource.
     */
    public void testRequestTextResourceShortExpire() throws Exception {
        GetMethod get = obtainGetMethod(sampleTextResourcePath);
        int statusCode = getHttpClient().executeMethod(get);
        assertEquals("AuraFrameworkServlet failed to fetch a valid resource request.", HttpStatus.SC_OK, statusCode);
        assertNotNull(get.getResponseBodyAsString());

        assertEquals("Framework servlet not responding with correct encoding type.", AuraBaseServlet.UTF_ENCODING,
                get.getResponseCharSet());
        assertTrue("Framework servlet not responding with correct mime type",
                get.getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue().startsWith("text/css;"));
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        Date currentDate = new Date();
        long expirationMillis = (df.parse(get.getResponseHeader(HttpHeaders.EXPIRES).getValue()).getTime()
                - currentDate.getTime());
        assertTrue("AuraFrameworkServlet is not setting the right value for expires header.",
                ApproximatelyEqual(expirationMillis, AuraBaseServlet.SHORT_EXPIRE, timeWindowExpiry));
    }

    /**
     * Verify that AuraFrameworkServlet responds successfully to valid request
     * for a javascript resource.
     */
    public void testRequestJavascriptResourceLongExpire() throws Exception {
        GetMethod get = obtainGetMethod(sampleJavascriptResourcePath);
        checkExpired(get, "text/javascript");

        get = obtainUidedGetMethod(sampleJavascriptResourcePath, false);
        checkLongCache(get, "text/javascript");

        get = obtainUidedGetMethod(sampleJavascriptResourcePath, true);
        checkExpired(get, "text/javascript");
    }
}
