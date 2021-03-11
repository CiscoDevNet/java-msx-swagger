/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger.controller;

import com.cisco.msx.autoconfigure.swagger.SwaggerAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.cisco.msx.swagger.controller.AdditionalApiResourcesControllerTests.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={AdditionalApiResourcesControllerTests.LocalTestConfiguration.class})

@WebMvcTest(AdditionalApiResourcesController.class)
@TestPropertySource(properties = {
		"swagger.security.sso.enabled=true",
		"swagger.security.sso.clientId=" + TEST_CLIENT,
		"swagger.security.sso.clientSecret=" + TEST_SECRET,
		"swagger.security.sso.baseUrl=" + TEST_BASE_URL,
		"swagger.security.sso.authorizePath=" + TEST_AUTH_PATH,
		"swagger.security.sso.tokenPath=" + TEST_TOKEN_PATH,
		})
@ImportAutoConfiguration({
        SwaggerAutoConfiguration.class
})
public class AdditionalApiResourcesControllerTests {

	public static final String TEST_CLIENT = "test-client";
	public static final String TEST_SECRET = "test-secret";
	public static final String TEST_BASE_URL = "http://localhost:9103/idm";
	public static final String TEST_AUTH_PATH = "/v2/authorize";
	public static final String TEST_TOKEN_PATH = "/v2/token";
	public static final String TEST_AUTH_URL = "http://localhost:9103/idm/v2/authorize";
	public static final String TEST_TOKEN_URL = "http://localhost:9103/idm/v2/token";

    @Autowired
    private MockMvc mvc;


    @Test
	@SuppressWarnings({"squid:S00112","squid:S2068"})
    public void testLoginSuccessEndpoint() throws Exception {
    	
    	this.mvc
		.perform(get("/swagger-resources/configuration/security/sso")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.clientId").value(TEST_CLIENT))
				.andExpect(jsonPath("$.clientSecret").value(TEST_SECRET))
				.andExpect(jsonPath("$.authorizeUrl").value(TEST_AUTH_URL))
				.andExpect(jsonPath("$.tokenUrl").value(TEST_TOKEN_URL))
		;
    }
    
    @Configuration
	public static class LocalTestConfiguration {
		
	}
}
