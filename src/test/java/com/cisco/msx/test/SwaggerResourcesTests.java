/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.test;

import com.cisco.msx.autoconfigure.swagger.SwaggerAutoConfiguration;
import com.cisco.msx.swagger.SwaggerConfigurer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.function.Predicate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * SwaggerResourcesTests
 *
 * @author Livan Du
 * Created on 2018-03-14
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={SwaggerResourcesTests.LocalTestConfiguration.class})
@WebMvcTest({
        SwaggerResourcesTests.TestV2Controller.class,
        SwaggerResourcesTests.TestV1Controller.class,

})
@TestPropertySource(properties = {
        "swagger.security.oauth2.enabled=true",
})
@ImportAutoConfiguration({
        SwaggerAutoConfiguration.class
})
public class SwaggerResourcesTests {

    private static final String API_GROUP = "test";
    private static final String API_TITLE = "Test API";
    private static final String API_VERSION = "2.0-TEST";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SuppressWarnings("squid:S00112")
    public void SwaggerApiDocTest() throws Exception {
        mockMvc.perform(get("/v2/api-docs")
                .accept(MediaType.APPLICATION_JSON)
                .param("group", API_GROUP)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.swagger").value("2.0"))
        .andExpect(jsonPath("$.tags[?(@.name =~ /^tag[0-9]+/i)]").isNotEmpty())
        .andExpect(jsonPath("$.info.title").value(API_TITLE))
        .andExpect(jsonPath("$.info.version").value(API_VERSION))
        .andExpect(jsonPath("$.paths['/test/v1/do']").exists())
        .andExpect(jsonPath("$.paths['/test/v2/{action}']").exists())
        .andExpect(jsonPath("$.paths[*][*].security").isNotEmpty())
        .andExpect(jsonPath("$.securityDefinitions.oauth2").exists())
        ;
    }

    /****************************
     * Test mocks and configs
     ****************************/
    @RestController
    @RequestMapping("/test/v2")
    public static class TestV2Controller {
        @GetMapping("{action}")
        public String doAction(@PathVariable("action") String action) {
            return action;
        }
    }

    @RestController
    @RequestMapping("/test/v1")
    public static class TestV1Controller {
        @GetMapping("do")
        public String doAction(@RequestParam("action") String action) {
            return action;
        }
    }

    @TestConfiguration
    @SpringBootConfiguration
    public static class LocalTestConfiguration {

        @Bean
        public TestV2Controller testV2Controller() {
            return new TestV2Controller();
        }

        @Bean
        public TestV1Controller testV1Controller() {
            return new TestV1Controller();
        }

        @TestConfiguration
        public static class SwaggerConfigurerV2 implements SwaggerConfigurer, Ordered {

            @Override
            public int getOrder() {
                return Ordered.HIGHEST_PRECEDENCE;
            }

            @Override
            public ApiInfoBuilder configureApiInfo(ApiInfoBuilder apiInfo) {
                return apiInfo.title(API_TITLE)
                        .description("Description of API")
                        .termsOfServiceUrl("http://www.cisco.com")
                        .contact(new Contact("Cisco Systems Inc.", "http://www.cisco.com", ""))
                        .license("Apache License Version 2.0")
                        .licenseUrl("https://github.com/springfox/springfox/blob/master/LICENSE")
                        .version(API_VERSION);
            }

            /**
             * Configure API selector predicates.
             *
             * @param apiPathSelector
             * @return configured object, can be same instance as the provided
             */
            @Override
            public Predicate<String> configureApiPathSelector(Predicate<String> apiPathSelector) {
                return apiPathSelector.or(regex("/test/v2/.*"));
            }

            @Override
            public Docket configure(Docket docket) {
                return docket
                        .groupName(API_GROUP)
                        .tags(new Tag("tag1", "test tag 1"),
                              new Tag("tag2", "test tag 2"))
                        ;

            }
        }

        @TestConfiguration
        public static class SwaggerConfigurerV1 implements SwaggerConfigurer, Ordered {

            @Override
            public int getOrder() {
                return Ordered.LOWEST_PRECEDENCE;
            }

            @Override
            public ApiInfoBuilder configureApiInfo(ApiInfoBuilder apiInfo) {
                return apiInfo.title("Deprecated API")
                        .version("1.0-TEST");
            }

            public Predicate<String> configureApiPathSelector(Predicate<String> apiPathSelector) {
                return apiPathSelector
                        .or(ant("/test/v1/**"));
            }

            @Override
            public Docket configure(Docket docket) {
                return docket
                        .groupName(API_GROUP)
                        .tags(new Tag("tag3", "test tag 3"),
                                new Tag("tag4", "test tag 4"))
                        ;
            }

        }
    }

}
