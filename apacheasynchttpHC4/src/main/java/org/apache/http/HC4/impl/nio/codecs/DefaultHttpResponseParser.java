/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.HC4.impl.nio.codecs;

import org.apache.http.HC4.HttpException;
import org.apache.http.HC4.HttpResponse;
import org.apache.http.HC4.HttpResponseFactory;
import org.apache.http.HC4.ParseException;
import org.apache.http.HC4.StatusLine;
import org.apache.http.HC4.annotation.NotThreadSafe;
import org.apache.http.HC4.config.MessageConstraints;
import org.apache.http.HC4.nio.reactor.SessionInputBuffer;
import org.apache.http.HC4.util.Args;
import org.apache.http.HC4.impl.DefaultHttpResponseFactory;
import org.apache.http.HC4.message.LineParser;
import org.apache.http.HC4.message.ParserCursor;
import org.apache.http.HC4.params.HttpParams;
import org.apache.http.HC4.util.CharArrayBuffer;

/**
 * Default {@link org.apache.http.HC4.nio.NHttpMessageParser} implementation
 * for {@link HttpResponse}s.
 *
 * @since 4.1
 */
@SuppressWarnings("deprecation")
@NotThreadSafe
public class DefaultHttpResponseParser extends AbstractMessageParser<HttpResponse> {

    private final HttpResponseFactory responseFactory;

    /**
     * @deprecated (4.3) use
     *   {@link DefaultHttpResponseParser#DefaultHttpResponseParser(
     *   SessionInputBuffer, LineParser, HttpResponseFactory, MessageConstraints)}
     */
    @Deprecated
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final LineParser parser,
            final HttpResponseFactory responseFactory,
            final HttpParams params) {
        super(buffer, parser, params);
        Args.notNull(responseFactory, "Response factory");
        this.responseFactory = responseFactory;
    }

    /**
     * Creates an instance of DefaultHttpResponseParser.
     *
     * @param buffer the session input buffer.
     * @param parser the line parser. If {@code null}
     *   {@link org.apache.http.message.BasicLineParser#INSTANCE} will be used.
     * @param responseFactory the response factory. If {@code null}
     *   {@link DefaultHttpResponseFactory#INSTANCE} will be used.
     * @param constraints Message constraints. If {@code null}
     *   {@link MessageConstraints#DEFAULT} will be used.
     *
     * @since 4.3
     */
    public DefaultHttpResponseParser(
            final SessionInputBuffer buffer,
            final LineParser parser,
            final HttpResponseFactory responseFactory,
            final MessageConstraints constraints) {
        super(buffer, parser, constraints);
        this.responseFactory = responseFactory != null ? responseFactory :
            DefaultHttpResponseFactory.INSTANCE;
    }

    /**
     * @since 4.3
     */
    public DefaultHttpResponseParser(final SessionInputBuffer buffer, final MessageConstraints constraints) {
        this(buffer, null, null, constraints);
    }

    /**
     * @since 4.3
     */
    public DefaultHttpResponseParser(final SessionInputBuffer buffer) {
        this(buffer, null);
    }

    @Override
    protected HttpResponse createMessage(final CharArrayBuffer buffer)
            throws HttpException, ParseException {
        final ParserCursor cursor = new ParserCursor(0, buffer.length());
        final StatusLine statusline = lineParser.parseStatusLine(buffer, cursor);
        return this.responseFactory.newHttpResponse(statusline, null);
    }

}
