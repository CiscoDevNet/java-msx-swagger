/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.autoconfigure.swagger;

import com.cisco.msx.swagger.SwaggerConfigurer;
import com.cisco.msx.swagger.config.SwaggerDocketConfiguration;
import com.cisco.msx.swagger.config.SwaggerProperties;
import com.cisco.msx.swagger.config.SwaggerProperties.SwaggerOAuthProperties;
import com.cisco.msx.swagger.config.SwaggerProperties.SwaggerSsoProperties;
import com.cisco.msx.swagger.config.SwaggerUiConfiguration;
import com.cisco.msx.swagger.controller.AdditionalApiResourcesController.SsoSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Auto Configuration of Springfox swagger.
 * Microservices should provide bean implementing {@link SwaggerConfigurer}
 * to customize swagger configurations
 *  
 * @author Livan Du
 * Created on Dec 1, 2016
 *
 */
@EnableSwagger2
@Configuration
@Import({
		SwaggerDocketConfiguration.class,
		SwaggerUiConfiguration.class,
})
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@EnableConfigurationProperties(SwaggerProperties.class)
@PropertySource({"classpath:defaults-swagger.properties"})
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnProperty(name="swagger.enabled", matchIfMissing=true)
public class SwaggerAutoConfiguration {

	@Bean
	@ConditionalOnProperty(name="swagger.security.oauth2.enabled")
	public SecurityConfiguration oauth2SecurityConfiguration(SwaggerProperties swaggerProperties) {
		SwaggerOAuthProperties oAuthProperties = swaggerProperties.getSecurity().getOauth2();
		return SecurityConfigurationBuilder.builder()
				.clientId(oAuthProperties.getClientId())
				.clientSecret(oAuthProperties.getClientSecret())
				.build();
	}

	@Bean
	@ConditionalOnProperty(name="swagger.security.sso.enabled", matchIfMissing=true)
	public SsoSecurityConfiguration ssoSecurityConfiguration(SwaggerProperties swaggerProperties) {
		SwaggerSsoProperties ssoProperties = swaggerProperties.getSecurity().getSso();
		return SsoSecurityConfiguration.builder()
				.clientId(ssoProperties.getClientId())
				.clientSecret(ssoProperties.getClientSecret())
				.tokenUrl(ssoProperties.getBaseUrl() + ssoProperties.getTokenPath())
				.authorizeUrl(ssoProperties.getBaseUrl() + ssoProperties.getAuthorizePath())
				.build();
	}
}
