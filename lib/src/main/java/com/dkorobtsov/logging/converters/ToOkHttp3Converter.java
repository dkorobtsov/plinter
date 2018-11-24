package com.dkorobtsov.logging.converters;

import static com.squareup.okhttp.internal.http.HttpMethod.permitsRequestBody;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Headers;
import okhttp3.TlsVersion;
import okio.Buffer;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;

public class ToOkHttp3Converter {

    private static final Logger logger = Logger.getLogger(ToOkHttp3Converter.class.getName());

    private static final String APPLICATION_JSON = "application/json";

    private ToOkHttp3Converter() {
    }

    private static okhttp3.MediaType convertOkHttpMediaTypeTo3(MediaType okhttpMediaType) {
        return okhttpMediaType == null ? okhttp3.MediaType.parse("")
            : okhttp3.MediaType.parse(okhttpMediaType.toString());
    }

    public static okhttp3.RequestBody convertOkHttpRequestBodyTo3(Request request) {
        final okhttp3.MediaType contentType = request.body() == null ? okhttp3.MediaType.parse("")
            : convertOkHttpMediaTypeTo3(request.body().contentType());
        try {
            final Request requestCopy = request.newBuilder().build();
            String requestBodyString = "";
            if (requestCopy.body() != null) {
                final Buffer buffer = new Buffer();
                requestCopy.body().writeTo(buffer);
                requestBodyString = buffer.readUtf8();
            }
            return okhttp3.RequestBody.create(contentType, requestBodyString);
        } catch (final IOException e) {
            return okhttp3.RequestBody
                .create(contentType, "[LoggingInterceptorError] : could not parse request body");
        }
    }

    public static okhttp3.RequestBody convertApacheHttpRequestBodyTo3(HttpRequest request) {
        if (request instanceof HttpRequestWrapper) {
            final HttpRequest original = ((HttpRequestWrapper) request).getOriginal();
            if (original instanceof HttpEntityEnclosingRequestBase) {
                final HttpEntity entity = ((HttpEntityEnclosingRequestBase) original).getEntity();
                if (entity != null) {
                    String requestBodyString;
                    try {
                        requestBodyString = readApacheHttpEntity(entity);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        return okhttp3.RequestBody
                            .create(okhttp3.MediaType.parse(APPLICATION_JSON),
                                "[LoggingInterceptorError] : could not parse request body");
                    }

                    final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString,
                        entity);
                    ((HttpEntityEnclosingRequestBase) ((HttpRequestWrapper) request).getOriginal())
                        .setEntity(newEntity);

                    final Header contentTypeHeader = Arrays
                        .stream(((HttpRequestWrapper) request).getOriginal().getAllHeaders())
                        .filter(header -> header.getName().equals("Content-Type"))
                        .findFirst()
                        .orElse(new BasicHeader("Content-Type", APPLICATION_JSON));

                    return okhttp3.RequestBody
                        .create(okhttp3.MediaType.parse(contentTypeHeader.getValue()),
                            requestBodyString);
                }
            }
        }
        return okhttp3.RequestBody.create(okhttp3.MediaType.parse(APPLICATION_JSON), "");
    }

    public static okhttp3.ResponseBody convertApacheHttpResponseBodyTo3(HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if (entity != null) {
            String requestBodyString;
            try {
                requestBodyString = readApacheHttpEntity(entity);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                return okhttp3.ResponseBody.create(okhttp3.MediaType.parse(APPLICATION_JSON),
                    "[LoggingInterceptorError] : could not parse response body");
            }
            final Header contentType = response.getEntity().getContentType();
            final String contentTypeString = contentType == null ? ""
                : String.format("%s, %s", contentType.getName(), contentType.getValue());
            final HttpEntity newEntity = recreateHttpEntityFromString(requestBodyString, entity);
            response.setEntity(newEntity);
            return okhttp3.ResponseBody
                .create(okhttp3.MediaType.parse(contentTypeString), requestBodyString);
        }
        return okhttp3.ResponseBody.create(okhttp3.MediaType.parse(APPLICATION_JSON), "");
    }

    private static String readApacheHttpEntity(HttpEntity entity) throws IOException {
        if (entity != null) {
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                (entity.getContent(), Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                return textBuilder.toString();
            }
        } else {
            return "";
        }
    }

    private static HttpEntity recreateHttpEntityFromString(String httpEntityContent,
        HttpEntity entity) {
        final Header contentType = entity.getContentType();
        final String contentTypeString = contentType == null ? "Content-Type=" + APPLICATION_JSON
            : String.format("%s=%s", contentType.getName(), contentType.getValue());
        final Header contentEncodingHeader = entity.getContentEncoding();
        final EntityBuilder entityBuilder = EntityBuilder
            .create()
            .setContentType(ContentType.parse(contentTypeString))
            .setStream(new ByteArrayInputStream(httpEntityContent.getBytes()));
        if (contentEncodingHeader != null) {
            return entityBuilder
                .setContentEncoding(String
                    .format("%s/%s", contentEncodingHeader.getName(),
                        contentEncodingHeader.getValue()))
                .build();
        }
        return entityBuilder.build();
    }

    public static okhttp3.CacheControl convertOkHttpCacheControlTo3(CacheControl cacheControl) {
        return new okhttp3.CacheControl
            .Builder()
            .maxAge(cacheControl.maxAgeSeconds() == -1 ? 0 : cacheControl.maxAgeSeconds(),
                TimeUnit.SECONDS)
            .maxStale(cacheControl.maxStaleSeconds() == -1 ? 0 : cacheControl.maxStaleSeconds(),
                TimeUnit.SECONDS)
            .minFresh(cacheControl.minFreshSeconds() == -1 ? 9 : cacheControl.minFreshSeconds(),
                TimeUnit.SECONDS)
            .build();
    }

    private static okhttp3.Request convertOkHttpRequestTo3(Request request) {
        final okhttp3.Request.Builder builder = new okhttp3.Request
            .Builder();
        builder.url(request.url());
        final Map<String, List<String>> headersMap = request.headers().toMultimap();
        headersMap.forEach(
            (String name, List<String> values) -> builder
                .addHeader(name, String.join(";", values)));
        if (permitsRequestBody(request.method())) {
            builder.method(request.method(), convertOkHttpRequestBodyTo3(request));
        } else {
            builder.method(request.method(), null);
        }
        builder.tag(request.tag());
        builder.cacheControl(convertOkHttpCacheControlTo3(request.cacheControl()));
        return builder.build();
    }

    private static okhttp3.Handshake convertOkHttpHandshakeTo3(
        com.squareup.okhttp.Handshake handshake) {
        if (handshake == null) {
            return null;
        } else {
            final String cipherSuite = handshake.cipherSuite();
            final List<Certificate> peerCertificates = handshake.peerCertificates();
            final List<Certificate> localCertificates = handshake.localCertificates();
            return okhttp3.Handshake.get(TlsVersion.SSL_3_0,
                okhttp3.CipherSuite.forJavaName(cipherSuite),
                peerCertificates,
                localCertificates
            );
        }
    }

    private static okhttp3.Headers convertOkHttpHeadersTo3(com.squareup.okhttp.Headers headers) {
        final Headers.Builder headersBuilder = new Headers.Builder();
        headers.names().forEach(name -> headersBuilder.add(name, headers.get(name)));
        return headersBuilder.build();
    }

    private static okhttp3.ResponseBody convertOkHttpResponseBodyTo3(
        com.squareup.okhttp.ResponseBody responseBody) {
        if (responseBody == null) {
            return null;
        } else {
            final MediaType mediaType = responseBody.contentType();
            String responseBodyString = "";
            try {
                responseBodyString = new String(responseBody.bytes(), Charset.defaultCharset());
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
            return okhttp3.ResponseBody
                .create(convertOkHttpMediaTypeTo3(mediaType), responseBodyString);
        }
    }

    private static okhttp3.Protocol convertOkHttpProtocolTo3(Protocol protocol) {
        switch (protocol) {
            case HTTP_1_0:
                return okhttp3.Protocol.HTTP_1_0;
            case HTTP_1_1:
                return okhttp3.Protocol.HTTP_1_1;
            case HTTP_2:
                return okhttp3.Protocol.HTTP_2;
            default:
                return okhttp3.Protocol.HTTP_1_1;
        }
    }

    public static okhttp3.Response convertOkHttpResponseTo3(Response response) {
        if (response == null) {
            return null;
        } else {
            final okhttp3.Response okHttp3Response = convertBaseOkHttpResponseTo3(response);
            return okHttp3Response
                .newBuilder()
                .cacheResponse(convertBaseOkHttpResponseTo3(response.cacheResponse()))
                .networkResponse(convertBaseOkHttpResponseTo3(response.networkResponse()))
                .build();
        }
    }

    private static okhttp3.Response convertBaseOkHttpResponseTo3(Response response) {
        if (response == null) {
            return null;
        } else {
            return new okhttp3.Response
                .Builder()
                .request(convertOkHttpRequestTo3(response.request()))
                .protocol(convertOkHttpProtocolTo3(response.protocol()))
                .code(response.code())
                .message(response.message())
                .handshake(convertOkHttpHandshakeTo3(response.handshake()))
                .headers(convertOkHttpHeadersTo3(response.headers()))
                .body(convertOkHttpResponseBodyTo3(response.body()))
                .build();
        }
    }
}
