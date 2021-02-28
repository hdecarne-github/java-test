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
package de.carne.test.test.mock.net.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import de.carne.test.mock.net.http.HttpClientMockInstance;

/**
 * Test {@linkplain HttpClientMockInstance} class.
 */
class TestHttpClientMockInstance {

	private static final URI TEST_URI1 = URI.create("https://domain.tld/test1");
	private static final URI TEST_URI2 = URI.create("https://domain.tld/test2");
	private static final URI TEST_URI3 = URI.create("https://domain.tld/test3");

	private static final HttpClientMockInstance MOCK_INSTANCE = new HttpClientMockInstance();

	@BeforeAll
	@SuppressWarnings("java:S6073")
	static void setupMock() throws Exception {
		HttpClient httpClient = MOCK_INSTANCE.get();

		@SuppressWarnings("unchecked") HttpResponse<String> response1 = Mockito.mock(HttpResponse.class);

		Mockito.when(response1.body()).thenReturn(TEST_URI1.toASCIIString());
		Mockito.doReturn(response1).when(httpClient).send(HttpClientMockInstance.requestUriEq(TEST_URI1),
				ArgumentMatchers.any());

		@SuppressWarnings("unchecked") HttpResponse<String> response2 = Mockito.mock(HttpResponse.class);

		Mockito.when(response2.body()).thenReturn(TEST_URI2.toASCIIString());
		Mockito.doReturn(response2).when(httpClient)
				.send(HttpClientMockInstance.requestUriEq(TEST_URI2.toASCIIString()), ArgumentMatchers.any());

		@SuppressWarnings("unchecked") HttpResponse<String> response3 = Mockito.mock(HttpResponse.class);

		Mockito.when(response3.body()).thenReturn(TEST_URI3.toASCIIString());
		Mockito.doReturn(response3).when(httpClient).send(
				HttpClientMockInstance.requestUriMatches(Pattern.compile("^https\\://.*/test3$")),
				ArgumentMatchers.any());
	}

	@AfterAll
	static void releaseMock() {
		MOCK_INSTANCE.close();
	}

	@Test
	void testUriMatch() throws Exception {
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request1 = HttpRequest.newBuilder(TEST_URI1).build();
		HttpResponse<String> response1 = httpClient.send(request1, BodyHandlers.ofString());

		Assertions.assertEquals(TEST_URI1.toASCIIString(), response1.body());

		HttpRequest request2 = HttpRequest.newBuilder(TEST_URI2).build();
		HttpResponse<String> response2 = httpClient.send(request2, BodyHandlers.ofString());

		Assertions.assertEquals(TEST_URI2.toASCIIString(), response2.body());

		HttpRequest request3 = HttpRequest.newBuilder(TEST_URI3).build();
		HttpResponse<String> response3 = httpClient.send(request3, BodyHandlers.ofString());

		Assertions.assertEquals(TEST_URI3.toASCIIString(), response3.body());
	}

}
