package com.dkorobtsov.logging;

import com.squareup.okhttp.Request;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

public class RequestDetailsUnitTests {

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingWith0ClientsSpecified() {
        new RequestDetails.Builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildingWithMoreThenOneClientsSpecified() {
        new RequestDetails.Builder()
            .from(new com.squareup.okhttp.Request.Builder().url("https://google.com").build())
            .from(new HttpGet("https://google.com"))
            .build();
    }

    @Test
    public void testBuildingOkhttpClientWithHeaders() {
        final String contentType = "application/json";
        final String authorizationHeader = "Bearer bla";
        final Request request = new Request
            .Builder()
            .url("https://google.com")
            .header("Content-Type", contentType)
            .header("Authorization", authorizationHeader)
            .build();
        final okhttp3.Request builtRequest = new RequestDetails.Builder()
            .from(request)
            .build();
        Assert.assertEquals("Content-Type header", request.headers().get("Content-Type"), builtRequest.headers().get("Content-Type"));
        Assert.assertEquals("Authorization header", request.headers().get("Authorization"), builtRequest.headers().get("Authorization"));
    }
}
