/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.security.oauth2.provider;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Dave Syer
 * 
 */
public class TestOAuth2Authentication {
	
	/**
	 * tests that a client authentication with scopes is converted to authorities
	 */
	@Test
	public void testScopesPresentAndConvertedToAuthorities() throws Exception {
		Authentication clientAuthentication = new AuthorizedClientAuthenticationToken("foo", null, Collections.singleton("read"), null);
		Authentication userAuthentication = null;
		OAuth2Authentication<Authentication, Authentication> target = new OAuth2Authentication<Authentication, Authentication>(clientAuthentication, userAuthentication);
		assertEquals("[SCOPE_READ]", target.getAuthorities().toString());
	}

	/**
	 * tests that a client authentication without scopes is used if it has authorities
	 */
	@Test
	public void testScopesNotPresentAndUseAuthorities() throws Exception {
		Authentication clientAuthentication = new AuthorizedClientAuthenticationToken("foo", null, null, Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority("ROLE_CLIENT")));
		Authentication userAuthentication = null;
		OAuth2Authentication<Authentication, Authentication> target = new OAuth2Authentication<Authentication, Authentication>(clientAuthentication, userAuthentication);
		assertEquals("[ROLE_CLIENT]", target.getAuthorities().toString());
	}

	/**
	 * tests that a client authentication without scopes or authorities is ignored and authorities taken from user
	 */
	@Test
	public void testUseAuthoritiesFromUserIfNoClient() throws Exception {
		Authentication clientAuthentication = new AuthorizedClientAuthenticationToken("foo", null, null, null);
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("user", "passwd", Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority("ROLE_USER")));
		OAuth2Authentication<Authentication, Authentication> target = new OAuth2Authentication<Authentication, Authentication>(clientAuthentication, userAuthentication);
		assertEquals("[ROLE_USER]", target.getAuthorities().toString());
	}

	/**
	 * tests that a client authentication without scopes or authorities is ignored and authorities taken from user
	 */
	@Test
	public void testUseAuthoritiesFromClientIfPresent() throws Exception {
		Authentication clientAuthentication = new AuthorizedClientAuthenticationToken("foo", null, null, Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority("ROLE_CLIENT")));
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken("user", "passwd", Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority("ROLE_USER")));
		OAuth2Authentication<Authentication, Authentication> target = new OAuth2Authentication<Authentication, Authentication>(clientAuthentication, userAuthentication);
		assertEquals("[ROLE_CLIENT]", target.getAuthorities().toString());
	}

}
