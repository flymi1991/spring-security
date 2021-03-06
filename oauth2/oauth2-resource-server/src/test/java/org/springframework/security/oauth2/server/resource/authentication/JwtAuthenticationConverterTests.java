/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.oauth2.server.resource.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Tests for {@link JwtAuthenticationConverter}
 *
 * @author Josh Cummings
 */
public class JwtAuthenticationConverterTests {
	JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

	@Test
	public void convertWhenDefaultGrantedAuthoritiesConverterSet() {
		Jwt jwt = this.jwt(Collections.singletonMap("scope", "message:read message:write"));

		AbstractAuthenticationToken authentication = this.jwtAuthenticationConverter.convert(jwt);
		Collection<GrantedAuthority> authorities = authentication.getAuthorities();

		assertThat(authorities).containsExactly(
				new SimpleGrantedAuthority("SCOPE_message:read"),
				new SimpleGrantedAuthority("SCOPE_message:write"));
	}

	@Test
	public void whenSettingNullGrantedAuthoritiesConverter() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(null))
				.withMessage("jwtGrantedAuthoritiesConverter cannot be null");
	}

	@Test
	public void convertWithOverriddenGrantedAuthoritiesConverter() {
		Jwt jwt = this.jwt(Collections.singletonMap("scope", "message:read message:write"));

		Converter<Jwt, Collection<GrantedAuthority>> grantedAuthoritiesConverter =
				token -> Arrays.asList(new SimpleGrantedAuthority("blah"));

		this.jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

		AbstractAuthenticationToken authentication = this.jwtAuthenticationConverter.convert(jwt);
		Collection<GrantedAuthority> authorities = authentication.getAuthorities();

		assertThat(authorities).containsExactly(
				new SimpleGrantedAuthority("blah"));
	}

	private Jwt jwt(Map<String, Object> claims) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", JwsAlgorithms.RS256);

		return new Jwt("token", Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
	}
}
