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

package org.gradle.api.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Nullable;
import org.gradle.api.Plugin;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.progress.BuildOperationDetails;
import org.gradle.internal.progress.BuildOperationExecutor;

/**
 * A decorating {@link PluginApplicator} implementation that delegates to a given
 * decorated implementation, but wraps the apply() execution in a
 * {@link org.gradle.internal.operations.BuildOperation}.
 */
public class BuildOperationPluginApplicator implements PluginApplicator {

    private PluginApplicator decorated;
    private BuildOperationExecutor buildOperationExecutor;

    public BuildOperationPluginApplicator(PluginApplicator decorated, BuildOperationExecutor buildOperationExecutor) {
        this.decorated = decorated;
        this.buildOperationExecutor = buildOperationExecutor;
    }

    public void applyImperative(@Nullable final String pluginId, final Plugin<?> plugin, @Nullable final String version) {
        buildOperationExecutor.run(toBuildOperationDetails(pluginId, plugin.getClass(), version, "imperative"), new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                decorated.applyImperative(pluginId, plugin, version);
            }
        });
    }

    public void applyRules(@Nullable final String pluginId, final Class<?> clazz, @Nullable final String version) {
        buildOperationExecutor.run(toBuildOperationDetails(pluginId, clazz, version, "rules"), new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                decorated.applyRules(pluginId, clazz, version);
            }
        });
    }

    public void applyImperativeRulesHybrid(@Nullable final String pluginId, final Plugin<?> plugin, @Nullable final String version) {
        buildOperationExecutor.run(toBuildOperationDetails(pluginId, plugin.getClass(), version, "hybrid"), new Action<BuildOperationContext>() {
            @Override
            public void execute(BuildOperationContext buildOperationContext) {
                decorated.applyImperativeRulesHybrid(pluginId, plugin, version);
            }
        });
    }

    private BuildOperationDetails toBuildOperationDetails(@Nullable String pluginId, Class<?> pluginClass, @Nullable String pluginVersion, String pluginKind) {
        String identifier = pluginId != null ? pluginId : pluginClass.getName();
        String versionString = pluginVersion == null ? "" : " (" + pluginVersion + ")";
        ApplyPluginOperationDescriptor operationDescriptor = new ApplyPluginOperationDescriptor(identifier, pluginVersion, pluginKind);
        return BuildOperationDetails.displayName("Apply plugin '" + identifier + "'" + versionString)
            .name(identifier).operationDescriptor(operationDescriptor).build();
    }

    public static class ApplyPluginOperationDescriptor {
        private String pluginId;
        private String pluginVersion;
        private String pluginKind;

        public ApplyPluginOperationDescriptor(String pluginId, String pluginVersion, String pluginKind) {
            this.pluginId = pluginId;
            this.pluginVersion = pluginVersion;
            this.pluginKind = pluginKind;
        }

        public String getPluginId() {
            return pluginId;
        }

        public String getPluginVersion() {
            return pluginVersion;
        }

        public String getPluginKind() {
            return pluginKind;
        }
    }

}
