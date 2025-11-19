/**
 * API Configuration for Spring PetClinic Modulith
 * 
 * Maps API paths from the microservices-based paths to the Modulith-based paths.
 * In the Modulith architecture, all services are consolidated into a single application,
 * so API paths are simplified.
 */

(function() {
    'use strict';

    // API endpoint mappings
    const API_MAPPING = {
        // Customer Service APIs (formerly api/customer/)
        'api/customer/': '/',
        
        // Vet Service APIs (formerly api/vet/)
        'api/vet/': '/',
        
        // Visit Service APIs (formerly api/visit/)
        'api/visit/': '/',
        
        // GenAI Service APIs (formerly api/genai/)
        'api/genai/': '/',
        
        // Gateway APIs
        'api/gateway/': '/'
    };

    /**
     * Transform API path from microservices format to Modulith format
     * @param {string} path - Original API path
     * @returns {string} - Transformed API path
     */
    window.transformApiPath = function(path) {
        for (const [oldPrefix, newPrefix] of Object.entries(API_MAPPING)) {
            if (path.startsWith(oldPrefix)) {
                // Remove the service prefix and keep the rest of the path
                const relativePath = path.substring(oldPrefix.length);
                return newPrefix + relativePath;
            }
        }
        // If no mapping found, return original path
        return path;
    };

    /**
     * Create a proxy for HTTP methods to automatically transform API paths
     */
    angular.module('petClinicApp').factory('apiInterceptor', ['$q', function($q) {
        return {
            request: function(config) {
                // Transform the API URL for all requests
                if (config.url && !config.url.startsWith('/webjars') && !config.url.startsWith('/fonts')) {
                    config.url = window.transformApiPath(config.url);
                }
                return config;
            }
        };
    }]);

    // Register the interceptor
    angular.module('petClinicApp').config(['$httpProvider', function($httpProvider) {
        $httpProvider.interceptors.push('apiInterceptor');
    }]);
})();
