/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Tracing Interceptor for request/response tracking.
 * 
 * Adds trace IDs to requests and logs request duration.
 * This enables correlation of logs across distributed system components.
 * 
 * @author PetClinic Team
 */
public class TracingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TracingInterceptor.class);
    private static final String TRACE_ID_ATTRIBUTE = "traceId";
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Generate trace ID if not present
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // Store in request for later use
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        // Add trace ID to response headers
        response.addHeader("X-Trace-Id", traceId);

        logger.debug("Incoming request - Method: {}, URI: {}, Trace-Id: {}",
                request.getMethod(), request.getRequestURI(), traceId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) throws Exception {
        // Not needed for tracing, but implement interface requirement
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) throws Exception {
        String traceId = (String) request.getAttribute(TRACE_ID_ATTRIBUTE);
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Request completed - Method: {}, URI: {}, Status: {}, Duration: {}ms, Trace-Id: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, traceId);
        }

        if (ex != null) {
            logger.error("Request failed - Method: {}, URI: {}, Trace-Id: {}", 
                    request.getMethod(), request.getRequestURI(), traceId, ex);
        }
    }
}
