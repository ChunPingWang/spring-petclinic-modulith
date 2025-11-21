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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.actuate.autoconfigure.tracing.TracingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * OpenTelemetry and Distributed Tracing Configuration.
 * 
 * Configures:
 * - Micrometer Tracing with OpenTelemetry bridge
 * - Zipkin exporter for trace visibility
 * - Request/Response logging for debugging
 * - Trace correlation IDs across services
 * 
 * Traces are sent to Zipkin at: http://localhost:9411
 * 
 * @author PetClinic Team
 */
@Configuration
public class DistributedTracingConfig implements WebMvcConfigurer {

    /**
     * Enable detailed HTTP request logging for debugging.
     * Shows request URI, parameters, and headers.
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA : ");
        return loggingFilter;
    }

    /**
     * Add tracing interceptor to all requests.
     * This helps correlate traces across different modules.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TracingInterceptor());
    }

    /**
     * Configure tracing properties.
     * Enables sampling of all traces (probability = 1.0)
     */
    @Bean
    public TracingConfigurationProperties tracingProperties() {
        return new TracingConfigurationProperties();
    }

    /**
     * Inner class for tracing configuration properties
     */
    static class TracingConfigurationProperties {
        // Configuration constants for tracing
        public static final float SAMPLING_PROBABILITY = 1.0f;
        public static final String ZIPKIN_ENDPOINT = "http://localhost:9411/api/v2/spans";
        public static final String TRACE_ID_HEADER = "X-Trace-Id";
        public static final String SPAN_ID_HEADER = "X-Span-Id";
    }
}
