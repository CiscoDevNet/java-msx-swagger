/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import springfox.documentation.annotations.ApiIgnore;

import java.util.Optional;

/**
 * @author Livan Du
 * Created on Dec 5, 2016
 *
 */
@Controller
@ApiIgnore
@RequestMapping("/swagger-resources")
public class AdditionalApiResourcesController {

	@Autowired(required = false)
	protected SsoSecurityConfiguration ssoSecurityConfiguration;

	@RequestMapping(value = "/configuration/security/sso")
	@ResponseBody
	public ResponseEntity<SsoSecurityConfiguration> userSecurityConfiguration() {
		return ResponseEntity.of(Optional.ofNullable(ssoSecurityConfiguration));
	}

	@Data
	@Builder
	public static class SsoSecurityConfiguration {
		private String tokenUrl;
		private String authorizeUrl;
		private String clientId;
		private String clientSecret;
	}
}
