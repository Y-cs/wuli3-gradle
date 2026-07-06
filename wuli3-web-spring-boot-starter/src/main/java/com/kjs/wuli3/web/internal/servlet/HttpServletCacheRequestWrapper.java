package com.kjs.wuli3.web.internal.servlet;

import com.kjs.wuli3.core.error.ErrorCodeException;
import com.kjs.wuli3.web.error.WebErrors;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Request wrapper that allows the body to be read more than once.
 */
public final class HttpServletCacheRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public HttpServletCacheRequestWrapper(final HttpServletRequest request) throws IOException {
        this(request, Long.MAX_VALUE);
    }

    public HttpServletCacheRequestWrapper(final HttpServletRequest request, final long maxBodyBytes) throws IOException {
        super(request);
        this.cachedBody = HttpServletCacheRequestWrapper.readBody(request, maxBodyBytes);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), this.charset()));
    }

    public byte[] getCachedBody() {
        return Arrays.copyOf(this.cachedBody, this.cachedBody.length);
    }

    private Charset charset() {
        final String encoding = this.getCharacterEncoding();
        return encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
    }

    private static byte[] readBody(final HttpServletRequest request, final long maxBodyBytes) throws IOException {
        final long contentLength = request.getContentLengthLong();
        if (contentLength > maxBodyBytes) {
            throw HttpServletCacheRequestWrapper.requestBodyTooLarge();
        }
        final ServletInputStream inputStream = request.getInputStream();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8192];
        long total = 0;
        int read = inputStream.read(buffer);
        while (read >= 0) {
            total += read;
            if (total > maxBodyBytes) {
                throw HttpServletCacheRequestWrapper.requestBodyTooLarge();
            }
            output.write(buffer, 0, read);
            read = inputStream.read(buffer);
        }
        return output.toByteArray();
    }

    private static ErrorCodeException requestBodyTooLarge() {
        return new ErrorCodeException(WebErrors.PAYLOAD_TOO_LARGE);
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        private CachedBodyServletInputStream(final byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return this.inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(final ReadListener readListener) {
            throw new UnsupportedOperationException("Async read is not supported");
        }

        @Override
        public int read() {
            return this.inputStream.read();
        }

        @Override
        public int read(final byte[] buffer, final int offset, final int length) {
            return this.inputStream.read(buffer, offset, length);
        }
    }
}
