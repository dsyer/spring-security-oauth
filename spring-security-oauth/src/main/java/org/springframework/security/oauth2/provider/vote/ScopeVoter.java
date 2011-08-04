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

package org.springframework.security.oauth2.provider.vote;

import java.util.Collection;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Dave Syer
 *
 */
public class ScopeVoter implements AccessDecisionVoter<Object> {

    private String scopePrefix = "SCOPE_";

    public String getScopePrefix() {
        return scopePrefix;
    }

    /**
     * Allows the default role prefix of <code>SCOPE_</code> to be overridden.
     * May be set to an empty value, although this is usually not desirable.
     *
     * @param scopePrefix the new prefix
     */
    public void setScopePrefix(String scopePrefix) {
        this.scopePrefix = scopePrefix;
    }

	public boolean supports(ConfigAttribute attribute) {
        if ((attribute.getAttribute() != null) && attribute.getAttribute().startsWith(getScopePrefix())) {
            return true;
        }
        else {
            return false;
        }
	}

	public boolean supports(Class<?> clazz) {
		return true;
	}

	public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

		int result = ACCESS_ABSTAIN;
        Collection<? extends GrantedAuthority> authorities = extractAuthorities(authentication);

        for (ConfigAttribute attribute : attributes) {
            if (this.supports(attribute)) {
                result = ACCESS_DENIED;

                // Attempt to find a matching granted authority
                for (GrantedAuthority authority : authorities) {
                    if (attribute.getAttribute().equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        return result;

	}

	protected Collection<? extends GrantedAuthority> extractAuthorities(Authentication authentication) {
       return authentication.getAuthorities();
    }

}
