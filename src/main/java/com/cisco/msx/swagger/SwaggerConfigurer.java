/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.function.Predicate;

/**
 * SwaggerConfigurer. Microservices could implement this interface on Java config class
 * to customize Swagger.
 *
 * The implementing could also implement {@link org.springframework.core.Ordered}interface
 * in case of multiple SwaggerConfigurer beans available. The configurer with highest
 * precedence will get applied last
 *
 * @author Livan Du
 * Created on 2018-03-13
 */
public interface SwaggerConfigurer {

    /**
     * Configure API Info
     * @param apiInfo
     * @return configured object, can be same instance as the provided
     */
    default ApiInfoBuilder configureApiInfo(ApiInfoBuilder apiInfo) {
        return apiInfo;
    }

    /**
     * Configure API selector predicates.
     *
     * @param apiRequestHandlerSelector
     * @return configured object, can be same instance as the provided
     */
    default Predicate<RequestHandler> configureApiRequestHandlerSelector(
            Predicate<RequestHandler> apiRequestHandlerSelector) {
        return apiRequestHandlerSelector;
    }

    /**
     * Configure API selector predicates.
     *
     * @param apiPathSelector
     * @return configured object, can be same instance as the provided
     */
    default Predicate<String> configureApiPathSelector(
            Predicate<String> apiPathSelector) {
        return apiPathSelector;
    }

    /**
     * add security contexts (you can remove one from list as well)
     *
     * @param securityContexts
     * @return configured object, can be same instance as the provided
     */
    default List<SecurityContext> addSecurityContexts(List<SecurityContext> securityContexts) {
        return securityContexts;
    }

    /**
     * add alternate type rules (you can remove one from list as well). Useful when defining custome model descriptions
     *
     * @param alternateTypeRules
     * @return configured object, can be same instance as the provided
     */
    default List<AlternateTypeRule> addAlternateTypeRules(List<AlternateTypeRule> alternateTypeRules) {
        return alternateTypeRules;
    }

    /**
     * add security schemes (you can remove one from list as well)
     *
     * @param securitySchemes
     * @return configured object, can be same instance as the provided
     */
    default List<SecurityScheme> addSecuritySchemes(List<SecurityScheme> securitySchemes) {
        return securitySchemes;
    }

    /**
     * General configuration for swagger docket. You can configure everything here, because it's
     * executed after all component configuration such as configureApiInfo() and configureApiSelector().
     * However, it's highly recommended to use individual configuration method
     *
     * @param docket a SWAGGER_2 Docket
     * @return fully configured Docket, can be same as given instance
     */
    default Docket configure(Docket docket) {
        return docket;
    }
}
