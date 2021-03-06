/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.caching.internal;

import com.google.common.collect.ForwardingObject;
import org.gradle.caching.BuildCacheEntryReader;
import org.gradle.caching.BuildCacheEntryWriter;
import org.gradle.caching.BuildCacheException;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.BuildCacheService;

import java.io.IOException;

public abstract class ForwardingBuildCacheService extends ForwardingObject implements BuildCacheService {

    @Override
    abstract protected BuildCacheService delegate();

    @Override
    public boolean load(BuildCacheKey key, BuildCacheEntryReader reader) throws BuildCacheException {
        return delegate().load(key, reader);
    }

    @Override
    public void store(BuildCacheKey key, BuildCacheEntryWriter writer) throws BuildCacheException {
        delegate().store(key, writer);
    }

    @Override
    public String getDescription() {
        return delegate().getDescription();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }
}
