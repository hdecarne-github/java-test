/*
 * Copyright (c) 2018-2021 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.test.mock.net.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.regex.Pattern;

import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.carne.test.mock.ScopedMockInstance;

/**
 * Mocks the {@linkplain HttpClient#newHttpClient()} method to return a spyable {@linkplain HttpClient} instance.
 */
public class HttpClientMockInstance extends ScopedMockInstance<MockedStatic<HttpClient>, HttpClient> {

	/**
	 * Constructs a new {@linkplain HttpClientMockInstance} instance.
	 */
	public HttpClientMockInstance() {
		super(HttpClientMockInstance::initialize, Mockito.spy(new HttpClientMock()));
	}

	private static MockedStatic<HttpClient> initialize(HttpClient instance) {
		MockedStatic<HttpClient> mock = Mockito.mockStatic(HttpClient.class);

		mock.when(HttpClient::newHttpClient).thenReturn(instance);
		return mock;
	}

	/**
	 * Creates {@linkplain ArgumentMatcher} for the given {@linkplain URI}.
	 *
	 * @param uri the {@linkplain URI} to match.
	 * @return {@code null}.
	 */
	public static HttpRequest requestUriEq(URI uri) {
		return ArgumentMatchers.argThat(request -> uri.equals(request.uri()));
	}

	/**
	 * Creates {@linkplain ArgumentMatcher} for the given URI string.
	 *
	 * @param uri the URI string to match.
	 * @return {@code null}.
	 */
	public static HttpRequest requestUriEq(String uri) {
		return ArgumentMatchers.argThat(request -> uri.equals(request.uri().toASCIIString()));
	}

	/**
	 * Creates {@linkplain ArgumentMatcher} for the given URI pattern.
	 *
	 * @param pattern the {@linkplain Pattern} to match.
	 * @return {@code null}.
	 */
	public static HttpRequest requestUriMatches(Pattern pattern) {
		return ArgumentMatchers.argThat(request -> pattern.matcher(request.uri().toASCIIString()).matches());
	}

}
