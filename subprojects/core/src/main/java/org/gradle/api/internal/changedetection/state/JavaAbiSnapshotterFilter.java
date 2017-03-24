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

package org.gradle.api.internal.changedetection.state;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.hash.FileHasher;
import org.gradle.api.internal.tasks.compile.ApiClassExtractor;
import org.gradle.util.DeprecationLogger;
import org.gradle.util.internal.Java9ClassReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

public class JavaAbiSnapshotterFilter implements SnapshotterFilter {
    private final FileHasher fallback;
    private final ApiClassExtractor extractor;

    public JavaAbiSnapshotterFilter(FileHasher fallback) {
        this.fallback = fallback;
        this.extractor = new ApiClassExtractor(Collections.<String>emptySet());
    }

    @Override
    public Iterable<FileDetails> filter(Iterable<SnapshottableFileDetails> details) {
        ImmutableList.Builder<FileDetails> result = ImmutableList.builder();
        for (SnapshottableFileDetails detail : details) {
            HashCode signature = hashFileDetails(detail);
            if (signature != null) {
                result.add(DefaultFileDetails.copyOf(detail).withContentHash(signature));
            }
        }
        return result.build();
    }

    private HashCode hashFileDetails(SnapshottableFileDetails detail) {
        InputStream inputStream = null;
        byte[] classBytes = null;
        HashCode signature;
        try {
            inputStream = detail.open();
            classBytes = ByteStreams.toByteArray(inputStream);
            signature = hashClassBytes(classBytes);
        } catch (Exception e) {
            if (classBytes == null) {
                throw new UncheckedIOException("Could not read class file [" + detail.getName() + "] file ", e);
            }
            signature = fallback.hash(new ByteArrayInputStream(classBytes));
            DeprecationLogger.nagUserWith("Malformed class file [" + detail.getName() + "] found on compile classpath, which means that this class will cause a compile error if referenced in a source file. Gradle 5.0 will no longer allow malformed classes on compile classpath.");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return signature;
    }

    private HashCode hashClassBytes(byte[] classBytes) throws IOException {
        // Use the ABI as the hash
        Java9ClassReader reader = new Java9ClassReader(classBytes);
        if (extractor.shouldExtractApiClassFrom(reader)) {
            byte[] signature = extractor.extractApiClassFrom(reader);
            if (signature != null) {
                return Hashing.md5().newHasher().putBytes(signature).hash();
            }
        }
        return null;
    }
}