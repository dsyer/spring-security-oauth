/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.security.oauth2.provider.vote;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @author Dave Syer
 * 
 */
public class TestScopeVoter {

	private ScopeVoter voter = new ScopeVoter();

	@Test
	public void testScopesPresentAndGranted() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("user", "passwd",
				Collections.singleton(new SimpleGrantedAuthority("SCOPE_READ")));
		assertEquals(
				AccessDecisionVoter.ACCESS_GRANTED,
				voter.vote(authentication, null,
						Collections.<ConfigAttribute> singleton(new SecurityConfig("SCOPE_READ"))));
	}

	@Test
	public void testScopesPresentAndDenied() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("user", "passwd",
				Collections.singleton(new SimpleGrantedAuthority("SCOPE_READ")));
		assertEquals(
				AccessDecisionVoter.ACCESS_DENIED,
				voter.vote(authentication, null,
						Collections.<ConfigAttribute> singleton(new SecurityConfig("SCOPE_WRITE"))));
	}

	@Test
	public void testNoScopesPresentAndRequired() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("user", "passwd",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
		assertEquals(
				AccessDecisionVoter.ACCESS_DENIED,
				voter.vote(authentication, null,
						Collections.<ConfigAttribute> singleton(new SecurityConfig("SCOPE_WRITE"))));
	}

	@Test
	public void testNoScopesPresentAndNotRequired() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken("user", "passwd",
				Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
		assertEquals(
				AccessDecisionVoter.ACCESS_ABSTAIN,
				voter.vote(authentication, null,
						Collections.<ConfigAttribute> singleton(new SecurityConfig("ROLE_GUEST"))));
	}

}
