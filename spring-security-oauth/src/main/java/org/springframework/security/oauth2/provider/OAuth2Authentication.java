package org.springframework.security.oauth2.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * An OAuth 2 authentication token can contain multiple authentications: one for the client and one for the user. Since
 * some OAuth profiles don't require user authentication, the user authentication may be null.
 * 
 * @author Ryan Heaton
 */
public class OAuth2Authentication<C extends Authentication, U extends Authentication> extends
		AbstractAuthenticationToken {

	private static final long serialVersionUID = -4809832298438307309L;

	private static final String SCOPE_PREFIX = "SCOPE_";

	private final C clientAuthentication;
	private final U userAuthentication;

	/**
	 * Construct an OAuth 2 authentication. Since some OAuth profiles don't require user authentication, the user
	 * authentication may be null.
	 * 
	 * @param clientAuthentication The client authentication (may NOT be null).
	 * @param userAuthentication The user authentication (possibly null).
	 */
	public OAuth2Authentication(C clientAuthentication, U userAuthentication) {
		super(extractAuthorities(clientAuthentication, userAuthentication));
		this.clientAuthentication = clientAuthentication;
		this.userAuthentication = userAuthentication;
	}

	/**
	 * Extract some authorities from a combination of the contents of the input. The client always takes precedence
	 * because this is where the knowledge of scoped access is encapsulated. The algorithm is as follows:
	 * 
	 * <ol>
	 * <li>If the client is a {@link ClientAuthenticationToken} and it contains scope information then that is converted
	 * to authorities using a prefix "SCOPE_" and appending the name (uppercased) of the claimed scope. For instance a
	 * client authorized to access scopes <code>[read,write]</code> will have authorities
	 * <code>[SCOPE_READ,SCOPE_WRITE]</code>.</li>
	 * <li>A client can have no claimed scopes but still have some granted authorities, which will have been supplied as
	 * defaults by the authorization service. If those are present they are used.</li>
	 * <li>If the client has no intrinsic source of authorities the user authorities are used instead (or null if there
	 * are none or the user token itself is null).</li>
	 * </ol>
	 * 
	 * Note that the third option above is a fallback, and only makes sense in restricted circumstances, when the
	 * resource service understands the same authorities as the authorization service (as would be the case if they were
	 * the same application).
	 * 
	 * @param clientAuthentication the client authentication token
	 * @param userAuthentication the user authentication token
	 * @return a collection of authorities
	 */
	private static Collection<? extends GrantedAuthority> extractAuthorities(Authentication clientAuthentication,
			Authentication userAuthentication) {
		if (clientAuthentication instanceof ClientAuthenticationToken) {
			ClientAuthenticationToken clientAuthenticationToken = (ClientAuthenticationToken) clientAuthentication;
			Set<String> scopes = clientAuthenticationToken.getScope();
			if (scopes != null && !scopes.isEmpty()) {
				Collection<GrantedAuthority> result = new HashSet<GrantedAuthority>();
				for (String scope : scopes) {
					result.add(new SimpleGrantedAuthority(SCOPE_PREFIX + scope.toUpperCase()));
				}
				return result;
			}
		}
		Collection<? extends GrantedAuthority> authorities = clientAuthentication.getAuthorities();
		if (authorities != null && !authorities.isEmpty()) {
			return authorities;
		}
		return userAuthentication == null ? null : userAuthentication.getAuthorities();
	}

	public Object getCredentials() {
		return this.userAuthentication == null ? this.clientAuthentication.getCredentials() : this.userAuthentication
				.getCredentials();
	}

	public Object getPrincipal() {
		return this.userAuthentication == null ? this.clientAuthentication.getPrincipal() : this.userAuthentication
				.getPrincipal();
	}

	/**
	 * The client authentication.
	 * 
	 * @return The client authentication.
	 */
	public C getClientAuthentication() {
		return clientAuthentication;
	}

	/**
	 * The user authentication.
	 * 
	 * @return The user authentication.
	 */
	public U getUserAuthentication() {
		return userAuthentication;
	}

	@Override
	public boolean isAuthenticated() {
		return this.clientAuthentication.isAuthenticated()
				&& (this.userAuthentication == null || this.userAuthentication.isAuthenticated());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OAuth2Authentication)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		OAuth2Authentication that = (OAuth2Authentication) o;

		if (!clientAuthentication.equals(that.clientAuthentication)) {
			return false;
		}
		if (userAuthentication != null ? !userAuthentication.equals(that.userAuthentication)
				: that.userAuthentication != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + clientAuthentication.hashCode();
		result = 31 * result + (userAuthentication != null ? userAuthentication.hashCode() : 0);
		return result;
	}
}
