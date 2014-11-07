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
package org.apache.http.HC4.client.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.apache.http.HC4.Header;
import org.apache.http.HC4.HeaderElement;
import org.apache.http.HC4.HttpEntity;
import org.apache.http.HC4.HttpException;
import org.apache.http.HC4.HttpResponse;
import org.apache.http.HC4.HttpResponseInterceptor;
import org.apache.http.HC4.annotation.Immutable;
import org.apache.http.HC4.client.config.RequestConfig;
import org.apache.http.HC4.client.entity.DecompressingEntity;
import org.apache.http.HC4.client.entity.DeflateInputStream;
import org.apache.http.HC4.client.entity.InputStreamFactory;
import org.apache.http.HC4.config.Lookup;
import org.apache.http.HC4.config.RegistryBuilder;
import org.apache.http.HC4.protocol.HttpContext;

/**
 * {@link HttpResponseInterceptor} responsible for processing Content-Encoding
 * responses.
 * <p>
 * Instances of this class are stateless and immutable, therefore threadsafe.
 *
 * @since 4.1
 *
 */
@Immutable
public class ResponseContentEncoding implements HttpResponseInterceptor {

    public static final String UNCOMPRESSED = "http.client.response.uncompressed";

    private final static InputStreamFactory GZIP = new InputStreamFactory() {

        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new GZIPInputStream(instream);
        }
    };

    private final static InputStreamFactory DEFLATE = new InputStreamFactory() {

        @Override
        public InputStream create(final InputStream instream) throws IOException {
            return new DeflateInputStream(instream);
        }

    };

    private final Lookup<InputStreamFactory> decoderRegistry;

    /**
     * @since 4.4
     */
    public ResponseContentEncoding(final Lookup<InputStreamFactory> decoderRegistry) {
        this.decoderRegistry = decoderRegistry != null ? decoderRegistry :
            RegistryBuilder.<InputStreamFactory>create()
                    .register("gzip", GZIP)
                    .register("x-gzip", GZIP)
                    .register("deflate", DEFLATE)
                    .build();
    }

    /**
     * Handles {@code gzip} and {@code deflate} compressed entities by using the following
     * decoders:
     * <ul>
     * <li>gzip - see {@link GZIPInputStream}</li>
     * <li>deflate - see {@link DeflateInputStream}</li>
     * </ul>
     */
    public ResponseContentEncoding() {
        this(null);
    }

    @Override
    public void process(
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        final HttpEntity entity = response.getEntity();

        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final RequestConfig requestConfig = clientContext.getRequestConfig();
        // entity can be null in case of 304 Not Modified, 204 No Content or similar
        // check for zero length entity.
        if (requestConfig.isDecompressionEnabled() && entity != null && entity.getContentLength() != 0) {
            final Header ceheader = entity.getContentEncoding();
            if (ceheader != null) {
                final HeaderElement[] codecs = ceheader.getElements();
                for (final HeaderElement codec : codecs) {
                    final String codecname = codec.getName().toLowerCase(Locale.ROOT);
                    final InputStreamFactory decoderFactory = decoderRegistry.lookup(codecname);
                    if (decoderFactory != null) {
                        response.setEntity(new DecompressingEntity(response.getEntity(), decoderFactory));
                        response.removeHeaders("Content-Length");
                        response.removeHeaders("Content-Encoding");
                        response.removeHeaders("Content-MD5");
                    } else {
                        if (!"identity".equals(codecname)) {
                            throw new HttpException("Unsupported Content-Coding: " + codec.getName());
                        }
                    }
                }
            }
        }
    }

}
