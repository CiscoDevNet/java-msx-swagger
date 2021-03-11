/*
 * Copyright (c) 2021. Cisco Systems, Inc and its affiliates
 * All Rights reserved
 */

package com.cisco.msx.swagger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.*;

import com.cisco.msx.swagger.controller.AdditionalApiResourcesController;

/**
 * Everything for Swagger UI, such as manages the Spring MVC configuration
 * .....
 * Created by Livan Du on 2016-11-25.
 */
@Configuration
@ConditionalOnProperty(name="swagger.ui.enabled", matchIfMissing=true)
public class SwaggerUiConfiguration {
	
	@Bean
	public AdditionalApiResourcesController additionalApiResourcesController() {
		return new AdditionalApiResourcesController();
	}
	
	
	/* Configuration Classes */
	
	/**
	 * added internal resource view controller to serve swagger UI
	 * 
	 * @author Livan Du
	 * Created on Dec 1, 2016
	 */
	@Configuration
	public static class SwaggerUiMvcConfiguration implements WebMvcConfigurer {

		@Autowired
		protected SwaggerProperties swaggerProperties;
		
		/**
		 * add the view controllers for the swagger page
		 *
		 * @param registry the view controller registry
		 */
		@Override
		public void addViewControllers(ViewControllerRegistry registry) {
			registry
				.addViewController(swaggerProperties.getUi().getEndpoint())
				.setViewName(swaggerProperties.getUi().getView());
			registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		}
	}
}
