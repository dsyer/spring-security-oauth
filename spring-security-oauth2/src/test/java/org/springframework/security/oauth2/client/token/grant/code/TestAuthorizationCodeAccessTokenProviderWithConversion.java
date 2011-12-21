/*
 * Copyright 2006-2011 the original author or authors.
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
package org.springframework.security.oauth2.client.token.grant.code;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * @author Dave Syer
 * 
 */
public class TestAuthorizationCodeAccessTokenProviderWithConversion {

	private static class StubClientHttpRequest implements ClientHttpRequest {

		private static final HttpHeaders DEFAULT_RESPONSE_HEADERS = new HttpHeaders();

		private final HttpStatus responseStatus;

		private final HttpHeaders responseHeaders;

		private final String responseBody;

		{
			DEFAULT_RESPONSE_HEADERS.setContentType(MediaType.APPLICATION_JSON);
		}

		public StubClientHttpRequest(String responseBody) {
			this(HttpStatus.OK, DEFAULT_RESPONSE_HEADERS, responseBody);
		}

		public StubClientHttpRequest(HttpHeaders responseHeaders, String responseBody) {
			this(HttpStatus.OK, responseHeaders, responseBody);
		}

		public StubClientHttpRequest(HttpStatus responseStatus, HttpHeaders responseHeaders, String responseBody) {
			this.responseStatus = responseStatus;
			this.responseHeaders = responseHeaders;
			this.responseBody = responseBody;
		}

		public OutputStream getBody() throws IOException {
			return new ByteArrayOutputStream();
		}

		public HttpHeaders getHeaders() {
			return new HttpHeaders();
		}

		public URI getURI() {
			try {
				return new URI("http://foo.com");
			}
			catch (URISyntaxException e) {
				throw new IllegalStateException(e);
			}
		}

		public HttpMethod getMethod() {
			return HttpMethod.POST;
		}

		public ClientHttpResponse execute() throws IOException {
			return new ClientHttpResponse() {

				public HttpHeaders getHeaders() {
					return responseHeaders;
				}

				public InputStream getBody() throws IOException {
					return new ByteArrayInputStream(responseBody.getBytes("UTF-8"));
				}

				public String getStatusText() throws IOException {
					return responseStatus.getReasonPhrase();
				}

				public HttpStatus getStatusCode() throws IOException {
					return responseStatus;
				}

				public void close() {
				}
			};
		}
	}

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private RestTemplate restTemplate = new RestTemplate();

	private AuthorizationCodeAccessTokenProvider provider = new AuthorizationCodeAccessTokenProvider() {
		protected RestOperations getRestTemplate() {
			return restTemplate;
		};
	};

	private AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();

	@Test
	public void testGetAccessTokenFromJson() throws Exception {
		final OAuth2AccessToken token = new OAuth2AccessToken("FOO");
		restTemplate.setRequestFactory(new ClientHttpRequestFactory() {
			public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
				return new StubClientHttpRequest(new ObjectMapper().writeValueAsString(token));
			}
		});
		AccessTokenRequest request = new AccessTokenRequest();
		request.setAuthorizationCode("foo");
		resource.setAccessTokenUri("http://localhost/oauth/token");
		assertEquals(token, provider.obtainAccessToken(resource, request));
	}

	@Test
	public void testGetAccessTokenFromForm() throws Exception {
		final OAuth2AccessToken token = new OAuth2AccessToken("FOO");
		final HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		restTemplate.setRequestFactory(new ClientHttpRequestFactory() {
			public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
				return new StubClientHttpRequest(responseHeaders, "access_token=FOO");
			}
		});
		AccessTokenRequest request = new AccessTokenRequest();
		request.setAuthorizationCode("foo");
		resource.setAccessTokenUri("http://localhost/oauth/token");
		assertEquals(token, provider.obtainAccessToken(resource, request));
	}

}
