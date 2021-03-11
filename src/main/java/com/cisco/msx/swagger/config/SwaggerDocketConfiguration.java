/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger.config;

import com.cisco.msx.swagger.SwaggerConfigurer;
import com.cisco.msx.swagger.config.SwaggerProperties.SwaggerOAuthProperties;
import com.cisco.msx.utils.FunctionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.ApiSelector;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Configuration for swagger Docket. If service want to customize it, it should implement
 * {@link SwaggerConfigurer in one of their configuration class}
 * 
 * @author Livan Du
 * Created on Mar 14, 2018
 *
 */
public class SwaggerDocketConfiguration {

	protected static final String DEFAULT_OAUTH2_SECURITY_DEFINITION_NAME = "oauth2";
	
	@Autowired
	protected SwaggerProperties swaggerProperties;

    @Autowired(required = false)
    protected List<SwaggerConfigurer> swaggerConfigurers = new ArrayList<>();

    @Autowired
    protected ServerProperties serverProperties;

    @PostConstruct
    public void init() {
        swaggerConfigurers = swaggerConfigurers.stream()
                .sorted(FunctionUtils.reversedOrderedFirst())
                .collect(Collectors.toList());
    }

    @Bean
    @ConditionalOnProperty(name="swagger.enabled", matchIfMissing=true)
    public Docket swaggerDocket() {

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName(getDefaultApiGroupName());

        // Setup some defaults
        ApiInfoBuilder apiInfoBuilder = getDefaultApiInfoBuilder();
        Predicate<String> pathSelector = getDefaultApiPathPredicate();
        Predicate<RequestHandler> requestHandlerSelector = getDefaultRequestHandlerPredicate();

        // configure apiInfo
        apiInfoBuilder = applyConfigurers(apiInfoBuilder, (target, configurer) -> configurer.configureApiInfo(target));

        // configure apiPath
        pathSelector = applyConfigurers(
                pathSelector,
                (target, configurer) -> configurer.configureApiPathSelector(target)
        );
        requestHandlerSelector = applyConfigurers(
                requestHandlerSelector,
                (target, configurer) -> configurer.configureApiRequestHandlerSelector(target));

        docket = docket
                .select()
                .paths(pathSelector)
                .apis(requestHandlerSelector)
                .build()
                .apiInfo(apiInfoBuilder.build());

        // configure security
        List<SecurityContext> securityContexts = new ArrayList<>();
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        if (swaggerProperties.getSecurity().getOauth2().isEnabled()) {
            securityContexts.add(getDefaultSecurityContext(pathSelector));
            securitySchemes.add(getOAuth2SecurityDefinition());
        }

        if (swaggerProperties.getUi().getHost() != null && !swaggerProperties.getUi().getHost().isEmpty()) {
            docket.host(swaggerProperties.getUi().getHost());
        }

        // Alternate Models
        List<AlternateTypeRule> alternateTypeRules = new ArrayList<>();

        // configure components
        securityContexts = applyConfigurers(securityContexts, (target, configurer) -> configurer.addSecurityContexts(target));
        securitySchemes = applyConfigurers(securitySchemes, (target, configurer) -> configurer.addSecuritySchemes(target));
        alternateTypeRules = applyConfigurers(alternateTypeRules, (target, configurer) -> configurer.addAlternateTypeRules(target));
        docket
                .alternateTypeRules(alternateTypeRules.toArray(new AlternateTypeRule[0]))
                .securityContexts(securityContexts)
                .securitySchemes(securitySchemes);

        // Last chance to configure it
        docket = applyConfigurers(docket, (target, configurer) -> configurer.configure(target));

        return docket;
    }
    
    protected String getDefaultApiGroupName() {
    	return "default-api";
    }

    protected ApiInfoBuilder getDefaultApiInfoBuilder() {
        return new ApiInfoBuilder()
                .title("Default API")
                .description("Description of API")
                .termsOfServiceUrl("http://www.cisco.com")
                .contact(new Contact("Cisco Systems Inc.", "http://www.cisco.com", ""))
                .license("Apache License Version 2.0")
                .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
                .version("2.0");
    }
    
	protected Predicate<String> getDefaultApiPathPredicate() {
    	return regex("/api/.*");
    }

    protected Predicate<RequestHandler> getDefaultRequestHandlerPredicate() {
        return ApiSelector.DEFAULT.getRequestHandlerSelector();
    }
    
    protected List<AuthorizationScope> getDefaultAuthorizationScopes() {
        return new ArrayList<>();
    }

    protected SecurityContext getDefaultSecurityContext(Predicate<String>apiPathSelector) {

        AuthorizationScope[] scopes = getDefaultAuthorizationScopes().toArray(new AuthorizationScope[] {});

        SecurityReference securityReference = SecurityReference.builder()
                .reference(DEFAULT_OAUTH2_SECURITY_DEFINITION_NAME)
                .scopes(scopes)
                .build();

        return SecurityContext.builder()
                .securityReferences(List.of(securityReference))
                .operationSelector(oc -> apiPathSelector.test(oc.requestMappingPattern()))
                .build();
    }

    public SecurityScheme getOAuth2SecurityDefinition() {
        return buildOAuth2SecurityScheme(swaggerProperties.getSecurity().getOauth2());
    }

    protected <T> T applyConfigurers(T target, BiFunction<T, SwaggerConfigurer, T> operator) {
        return swaggerConfigurers.stream()
                .sequential()
                .reduce(target, operator, (target1, target2) -> target1);
	}

	public static SecurityScheme buildOAuth2SecurityScheme(SwaggerOAuthProperties oAuthProperties) {
        List<AuthorizationScope> scopes = new ArrayList<>();

        List<GrantType> grantTypes = List.of(
                new AuthorizationCodeGrantBuilder()
                        .tokenRequestEndpoint(tokenRequestEndpointBuilder ->
                                tokenRequestEndpointBuilder
                                        .url(oAuthProperties.getBaseUrl() + oAuthProperties.getAuthorizePath())
                        )
                        .tokenEndpoint(tokenEndpointBuilder ->
                                tokenEndpointBuilder
                                        .url(oAuthProperties.getBaseUrl() + oAuthProperties.getTokenPath())
                        )
                        .build()
        );
        return new OAuthBuilder()
                .name(DEFAULT_OAUTH2_SECURITY_DEFINITION_NAME)
                .grantTypes(grantTypes)
                .scopes(scopes)
                .build()
                ;
    }
}
