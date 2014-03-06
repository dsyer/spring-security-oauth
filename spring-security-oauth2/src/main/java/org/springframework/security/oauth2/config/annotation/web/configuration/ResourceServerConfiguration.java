/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.security.oauth2.config.annotation.web.configuration;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@Order(3)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private TokenStore tokenStore;

	private List<ResourceServerConfigurer> configurers = Collections.emptyList();

	private AccessDeniedHandler accessDeniedHandler = new OAuth2AccessDeniedHandler();

	/**
	 * @param configurers the configurers to set
	 */
	@Autowired(required = false)
	public void setConfigurers(List<ResourceServerConfigurer> configurers) {
		this.configurers = configurers;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		for (ResourceServerConfigurer configurer : configurers) {
			configurer.configure(http);
		}
		OAuth2ResourceServerConfigurer resources = new OAuth2ResourceServerConfigurer();
		http.apply(resources).and().exceptionHandling().accessDeniedHandler(accessDeniedHandler);
		for (ResourceServerConfigurer configurer : configurers) {
			configurer.configure(resources);
		}
		resources.tokenStore(tokenStore);
	}

}
