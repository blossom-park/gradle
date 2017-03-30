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

package org.gradle.api.internal.file;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.progress.BuildOperationExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A decorating {@link FileCollection} implementation that delegates to a given
 * decorated implementation, but wraps the {@link #getFiles()} execution in a
 * {@link org.gradle.internal.operations.BuildOperation}.
 */
public class BuildOperationFileCollection implements FileCollection {

    private FileCollection decorated;
    private String fileCollectionContext;
    private BuildOperationExecutor buildOperationExecutor;

    public BuildOperationFileCollection(FileCollection decorated, String fileCollectionContext, BuildOperationExecutor buildOperationExecutor) {
        this.decorated = decorated;
        this.fileCollectionContext = fileCollectionContext;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    @Override
    public File getSingleFile() throws IllegalStateException {
        return decorated.getSingleFile();
    }

    @Override
    public Set<File> getFiles() {
        final List<Set<File>> result = new ArrayList<Set<File>>(1);
        buildOperationExecutor.run(fileCollectionContext, new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                result.add(decorated.getFiles());
            }
        });
        return result.get(0);
    }

    @Override
    public boolean contains(File file) {
        return decorated.contains(file);
    }

    @Override
    public String getAsPath() {
        return decorated.getAsPath();
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return decorated.plus(collection);
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return decorated.minus(collection);
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        return decorated.filter(filterClosure);
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        return decorated.filter(filterSpec);
    }

    @Override
    public Object asType(Class<?> type) throws UnsupportedOperationException {
        return decorated.asType(type);
    }

    @Override
    public FileCollection add(FileCollection collection) throws UnsupportedOperationException {
        return decorated.add(collection);
    }

    @Override
    public boolean isEmpty() {
        return decorated.isEmpty();
    }

    @Override
    public FileCollection stopExecutionIfEmpty() throws StopExecutionException {
        return decorated.stopExecutionIfEmpty();
    }

    @Override
    public FileTree getAsFileTree() {
        return decorated.getAsFileTree();
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, AntType type) {
        decorated.addToAntBuilder(builder, nodeName, type);
    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        return decorated.addToAntBuilder(builder, nodeName);
    }

    @Override
    public Iterator<File> iterator() {
        return decorated.iterator();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return decorated.getBuildDependencies();
    }
}
