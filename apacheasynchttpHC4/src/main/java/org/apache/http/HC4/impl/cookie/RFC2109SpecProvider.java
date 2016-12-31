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

package org.apache.http.HC4.impl.cookie;

import org.apache.http.HC4.annotation.Immutable;
import org.apache.http.HC4.conn.util.PublicSuffixMatcher;
import org.apache.http.HC4.cookie.CookieSpecProvider;
import org.apache.http.HC4.cookie.CookieSpec;
import org.apache.http.HC4.impl.cookie.BasicCommentHandler;
import org.apache.http.HC4.impl.cookie.BasicMaxAgeHandler;
import org.apache.http.HC4.impl.cookie.BasicPathHandler;
import org.apache.http.HC4.impl.cookie.BasicSecureHandler;
import org.apache.http.HC4.impl.cookie.RFC2109DomainHandler;
import org.apache.http.HC4.impl.cookie.RFC2109Spec;
import org.apache.http.HC4.impl.cookie.RFC2109VersionHandler;
import org.apache.http.HC4.protocol.HttpContext;

/**
 * {@link org.apache.http.HC4.cookie.CookieSpecProvider} implementation that provides an instance of
 * {@link RFC2109Spec}. The instance returned by this factory
 * can be shared by multiple threads.
 *
 * @since 4.4
 */
@Immutable
public class RFC2109SpecProvider implements CookieSpecProvider {

    private final PublicSuffixMatcher publicSuffixMatcher;
    private final boolean oneHeader;

    private volatile CookieSpec cookieSpec;

    public RFC2109SpecProvider(final PublicSuffixMatcher publicSuffixMatcher, final boolean oneHeader) {
        super();
        this.oneHeader = oneHeader;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC2109SpecProvider(final PublicSuffixMatcher publicSuffixMatcher) {
        this(publicSuffixMatcher, false);
    }

    public RFC2109SpecProvider() {
        this(null, false);
    }

    @Override
    public CookieSpec create(final HttpContext context) {
        if (cookieSpec == null) {
            synchronized (this) {
                if (cookieSpec == null) {
                    this.cookieSpec = new RFC2109Spec(this.oneHeader,
                            new RFC2109VersionHandler(),
                            new BasicPathHandler(),
                            PublicSuffixDomainFilter.decorate(
                                    new RFC2109DomainHandler(), this.publicSuffixMatcher),
                            new BasicMaxAgeHandler(),
                            new BasicSecureHandler(),
                            new BasicCommentHandler());
                }
            }
        }
        return this.cookieSpec;
    }

}
