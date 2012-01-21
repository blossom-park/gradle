/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve

import org.apache.ivy.core.module.descriptor.Artifact
import org.apache.ivy.core.module.descriptor.Configuration
import org.apache.ivy.core.module.descriptor.DependencyDescriptor
import org.apache.ivy.core.module.descriptor.ModuleDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.plugins.version.VersionMatcher
import org.gradle.api.internal.artifacts.ivyservice.DependencyToModuleResolver
import org.gradle.api.internal.artifacts.ivyservice.ModuleVersionIdResolveResult
import org.gradle.api.internal.artifacts.ivyservice.ModuleVersionNotFoundException
import org.gradle.api.internal.artifacts.ivyservice.ModuleVersionResolveException
import spock.lang.Specification

class LazyDependencyToModuleResolverTest extends Specification {
    final DependencyToModuleResolver target = Mock()
    final VersionMatcher matcher = Mock()
    final ModuleVersionIdResolveResult resolvedModule = Mock()
    final LazyDependencyToModuleResolver resolver = new LazyDependencyToModuleResolver(target, matcher)

    def "wraps failure to resolve module"() {
        def dependency = dependency()
        def failure = new RuntimeException("broken")

        when:
        resolver.resolve(dependency).descriptor

        then:
        ModuleVersionResolveException e = thrown()
        e.message == "Could not resolve group:group, module:module, version:1.0."
        e.cause == failure

        and:
        1 * target.resolve(dependency) >> { throw failure }
    }

    def "wraps module not found"() {
        def dependency = dependency()

        when:
        resolver.resolve(dependency).descriptor

        then:
        ModuleVersionNotFoundException e = thrown()
        e.message == "Could not find group:group, module:module, version:1.0."

        and:
        1 * target.resolve(dependency) >> null
    }

    def "wraps module not found for missing dynamic version"() {
        def dependency = dependency()

        given:
        matcher.isDynamic(_) >> true

        when:
        resolver.resolve(dependency).id

        then:
        ModuleVersionResolveException e = thrown()
        e.message == "Could not find any version that matches group:group, module:module, version:1.0."

        and:
        1 * target.resolve(dependency) >> null
    }

    def "wraps failure to resolve artifact"() {
        def dependency = dependency()
        def artifact = artifact()
        def failure = new RuntimeException("broken")

        when:
        resolver.resolve(dependency).getArtifact(artifact)

        then:
        ArtifactResolveException e = thrown()
        e.message == "Could not resolve artifact group:group, module:module, version:1.0, name:artifact."
        e.cause == failure

        and:
        1 * target.resolve(dependency) >> resolvedModule
        _ * resolvedModule.descriptor >> module()
        _ * resolvedModule.getArtifact(artifact) >> { throw failure }
    }

    def "wraps artifact not found failure"() {
        def dependency = dependency()
        def artifact = artifact()

        when:
        resolver.resolve(dependency).getArtifact(artifact)

        then:
        ArtifactNotFoundException e = thrown()
        e.message == "Artifact group:group, module:module, version:1.0, name:artifact not found."

        and:
        1 * target.resolve(dependency) >> resolvedModule
        _ * resolvedModule.descriptor >> module()
        _ * resolvedModule.getArtifact(artifact) >> null
    }

    def module() {
        ModuleDescriptor descriptor = Mock()
        ModuleRevisionId id = ModuleRevisionId.newInstance("group", "module", "1.0")
        _ * descriptor.moduleRevisionId >> id
        _ * descriptor.configurations >> ([] as Configuration[])
        return descriptor
    }

    def dependency() {
        DependencyDescriptor descriptor = Mock()
        ModuleRevisionId id = ModuleRevisionId.newInstance("group", "module", "1.0")
        _ * descriptor.dependencyRevisionId >> id
        return descriptor
    }

    def artifact() {
        Artifact artifact = Mock()
        ModuleRevisionId id = ModuleRevisionId.newInstance("group", "module", "1.0")
        _ * artifact.moduleRevisionId >> id
        _ * artifact.name >> 'artifact'
        return artifact
    }
}
