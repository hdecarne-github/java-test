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

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Exceptions;
import de.carne.util.logging.Log;

class HttpClientMock extends HttpClient {

	private static final Log LOG = new Log();

	HttpClientMock() {
		// prevent instantiation outside
	}

	@Override
	public Optional<CookieHandler> cookieHandler() {
		LOG.callee();

		return Optional.ofNullable(CookieHandler.getDefault());
	}

	@Override
	public Optional<Duration> connectTimeout() {
		LOG.callee();

		return Optional.empty();
	}

	@Override
	public Redirect followRedirects() {
		LOG.callee();

		return Redirect.NEVER;
	}

	@Override
	public Optional<ProxySelector> proxy() {
		LOG.callee();

		return Optional.empty();
	}

	@Override
	public SSLContext sslContext() {
		LOG.callee();

		SSLContext sslContext;

		try {
			sslContext = SSLContext.getDefault();
		} catch (NoSuchAlgorithmException e) {
			throw Exceptions.toRuntime(e);
		}
		return sslContext;
	}

	@Override
	public SSLParameters sslParameters() {
		LOG.callee();

		return sslContext().getDefaultSSLParameters();
	}

	@Override
	public Optional<Authenticator> authenticator() {
		LOG.callee();

		return Optional.empty();
	}

	@Override
	public Version version() {
		LOG.callee();

		return Version.HTTP_2;
	}

	@Override
	public Optional<Executor> executor() {
		LOG.callee();

		return Optional.empty();
	}

	@Override
	public <T> HttpResponse<T> send(@Nullable HttpRequest request, @Nullable BodyHandler<T> responseBodyHandler)
			throws IOException, InterruptedException {
		LOG.callee();

		Objects.requireNonNull(request);

		throw unhandledRequest(request);
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(@Nullable HttpRequest request,
			@Nullable BodyHandler<T> responseBodyHandler) {
		LOG.callee();

		return sendAsync(request, responseBodyHandler, null);
	}

	@Override
	public <T> CompletableFuture<HttpResponse<T>> sendAsync(@Nullable HttpRequest request,
			@Nullable BodyHandler<T> responseBodyHandler, @Nullable PushPromiseHandler<T> pushPromiseHandler) {
		LOG.callee();

		Objects.requireNonNull(request);

		throw unhandledRequest(request);
	}

	private IllegalArgumentException unhandledRequest(HttpRequest request) {
		return new IllegalArgumentException("Unhandled request: " + request.uri().toASCIIString());
	}

}
