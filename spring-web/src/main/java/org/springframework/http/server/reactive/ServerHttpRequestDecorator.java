/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.http.server.reactive;

import java.net.InetSocketAddress;
import java.net.URI;

import reactor.core.publisher.Flux;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * Wraps another {@link ServerHttpRequest} and delegates all methods to it.
 * Subclasses can override specific methods selectively.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public class ServerHttpRequestDecorator implements ServerHttpRequest {

	private final ServerHttpRequest delegate;


	public ServerHttpRequestDecorator(ServerHttpRequest delegate) {
		Assert.notNull(delegate, "Delegate is required");
		this.delegate = delegate;
	}


	public ServerHttpRequest getDelegate() {
		return this.delegate;
	}


	// ServerHttpRequest delegation methods...

	@Override
	public String getId() {
		return getDelegate().getId();
	}

	@Override
	public HttpMethod getMethod() {
		return getDelegate().getMethod();
	}

	@Override
	public URI getURI() {
		return getDelegate().getURI();
	}

	@Override
	public RequestPath getPath() {
		return getDelegate().getPath();
	}

	@Override
	public MultiValueMap<String, String> getQueryParams() {
		return getDelegate().getQueryParams();
	}

	@Override
	public HttpHeaders getHeaders() {
		return getDelegate().getHeaders();
	}

	@Override
	public MultiValueMap<String, HttpCookie> getCookies() {
		return getDelegate().getCookies();
	}

	@Override
	@Nullable
	public InetSocketAddress getLocalAddress() {
		return getDelegate().getLocalAddress();
	}

	@Override
	@Nullable
	public InetSocketAddress getRemoteAddress() {
		return getDelegate().getRemoteAddress();
	}

	@Override
	@Nullable
	public SslInfo getSslInfo() {
		return getDelegate().getSslInfo();
	}

	@Override
	public Flux<DataBuffer> getBody() {
		return getDelegate().getBody();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [delegate=" + getDelegate() + "]";
	}


	/**
	 * Return the native request of the underlying server API, if possible,
	 * also unwrapping {@link ServerHttpRequestDecorator} if necessary.
	 * @param request the request to check
	 * @param <T> the expected native request type
	 * @throws IllegalArgumentException if the native request can't be obtained
	 * @since 5.3.3
	 */
	public static <T> T getNativeRequest(ServerHttpRequest request) {
		if (request instanceof AbstractServerHttpRequest abstractServerHttpRequest) {
			return abstractServerHttpRequest.getNativeRequest();
		}
		else if (request instanceof ServerHttpRequestDecorator serverHttpRequestDecorator) {
			return getNativeRequest(serverHttpRequestDecorator.getDelegate());
		}
		else {
			throw new IllegalArgumentException("Cannot find native request in " + request.getClass().getName());
		}
	}

}
