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

package org.gradle.internal.configuration;

import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Nullable;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.progress.BuildOperationDetails;
import org.gradle.internal.progress.BuildOperationExecutor;
import org.gradle.internal.progress.HasBuildOperationContext;

/**
 * A decorating {@link DomainObjectConfigurator} implementation that delegates to a given
 * decorated implementation, but wraps the configure() execution in a
 * {@link org.gradle.internal.operations.BuildOperation}. The wrapping is only performed
 * if the domain object under configuration is implementing {@link HasBuildOperationContext}.
 */
public class BuildOperationDomainObjectConfigurator implements DomainObjectConfigurator {

    private DomainObjectConfigurator decorated;
    private BuildOperationExecutor buildOperationExecutor;

    public BuildOperationDomainObjectConfigurator(DomainObjectConfigurator decorated, BuildOperationExecutor buildOperationExecutor) {
        this.decorated = decorated;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    @Override
    public <T> T configure(final T object, final Closure configureClosure, @Nullable final String callerContextInformation) {
        if (object instanceof HasBuildOperationContext) {
            buildOperationExecutor.run(toDisplayName((HasBuildOperationContext) object, callerContextInformation), new Action<BuildOperationContext>() {
                @Override
                public void execute(BuildOperationContext buildOperationContext) {
                    decorated.configure(object, configureClosure, callerContextInformation);
                }
            });
        } else {
            decorated.configure(object, configureClosure, callerContextInformation);
        }
        return object;
    }

    @Override
    public <T> T configure(final T object, final Action<? super T> configureAction, @Nullable final String callerContextInformation) {
        if (object instanceof HasBuildOperationContext) {
            buildOperationExecutor.run(toDisplayName((HasBuildOperationContext) object, callerContextInformation), new Action<BuildOperationContext>() {
                @Override
                public void execute(BuildOperationContext buildOperationContext) {
                    decorated.configure(object, configureAction, callerContextInformation);
                }
            });
        } else {
            decorated.configure(object, configureAction, callerContextInformation);
        }
        return object;
    }

    private BuildOperationDetails toDisplayName(HasBuildOperationContext objectWithIdentityPath, String callerContextInformation) {
        String nameWithContext = objectWithIdentityPath.getBuildOperationDisplayName();
        if (callerContextInformation != null) {
            nameWithContext += " (" + callerContextInformation + ")";
        }
        return BuildOperationDetails.displayName("Configure " + nameWithContext).name(StringUtils.capitalize(nameWithContext)).build();
    }
}
