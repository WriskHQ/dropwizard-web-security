/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.websecurity.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import com.google.common.net.HttpHeaders;
import com.palantir.websecurity.WebSecurityConfiguration;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link WebSecurityFilter}.
 */
public final class WebSecurityFilterTests {

    private final WebSecurityConfiguration config = new WebSecurityConfiguration.Builder().build();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @Test
    public void testInjectInHttpServletRequests() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");

        WebSecurityFilter filter = new WebSecurityFilter(this.config, "/jersey/root/*");
        request.setPathInfo("/api");

        filter.doFilter(request, response, chain);

        // only testing 1 header, since the WebSecurityHeaderInjector is tested separately
        assertEquals(WebSecurityHeaderInjector.DEFAULT_FRAME_OPTIONS, response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }

    @Test
    public void testNotInjectForJerseyPathWithStar() throws IOException, ServletException {
        WebSecurityFilter filter = new WebSecurityFilter(this.config, "/api/*");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoStar() throws IOException, ServletException {
        WebSecurityFilter filter = new WebSecurityFilter(this.config, "/api/");
        assertNotInjecting(filter);
    }

    @Test
    public void testNotInjectForJerseyPathNoSlash() throws IOException, ServletException {
        WebSecurityFilter filter = new WebSecurityFilter(this.config, "/api");
        assertNotInjecting(filter);
    }

    private void assertNotInjecting(WebSecurityFilter filter) throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/hello");
        // the servlet path is used to check if the request is for Jersey
        request.setServletPath("/api");

        filter.doFilter(request, response, chain);

        assertNull(response.getHeader(HttpHeaders.X_FRAME_OPTIONS));
    }
}