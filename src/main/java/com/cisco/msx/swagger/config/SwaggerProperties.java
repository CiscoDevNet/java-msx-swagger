/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

/**
 * @author Livan Du
 * Created on Dec 1, 2016
 *
 */
@Data
@ConfigurationProperties(prefix="swagger", ignoreUnknownFields=true)
@Validated
public class SwaggerProperties {
	
	private boolean enabled = true;
	private SwaggerSecurityProperties security = new SwaggerSecurityProperties();
	private SwaggerUiProperties ui = new SwaggerUiProperties();
	private Map<String, ?> metadata = new HashMap<>();
	
	
	@Data
	public static class SwaggerSecurityProperties {
		private SwaggerOAuthProperties oauth2 = new SwaggerOAuthProperties();
		private SwaggerSsoProperties sso = new SwaggerSsoProperties();
	}
	
	@Data
	public static class SwaggerUiProperties {
		private boolean enabled = true;
		private String endpoint = "";
		private String view = "";
		private String host = "";
	}

	@Data
	public static class SwaggerSsoProperties {
		private boolean enabled = true;
		private String baseUrl = "";
		private String tokenPath = "";
		private String authorizePath = "";
		private String clientId = "";
		private String clientSecret = "";
	}

	@Data
	public static class SwaggerOAuthProperties {
		private boolean enabled = true;
		private String baseUrl = "";
		private String tokenPath = "";
		private String authorizePath = "";
		private String clientId = "";
		private String clientSecret = "";
	}
}
