package org.bitbucket.cpointe.reinheitsgsbot;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.enforcer.BannedDependencies;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extends the standard Maven Enforcer Banned Dependency rule to include support for searching for banned dependencies
 * within Dependency Management as well as Dependencies.
 * <p>
 * This is useful when you ban a dependency that may be in a parent pom's dependency management declaration, but is not
 * actually used as a dependency until further in the multi-module build.
 * <p>
 * A common examples is BOM declaration in a parent pom. When this occurs, you're banned dependencies in the parent
 * module are missed and become GAV errors in the child, limiting the usefulness of the banned dependency check for
 * that scenario.
 */
public class BannedDependeneciesIncludingDependencyManagementRule extends BannedDependencies {

    /**
     * Dependency management dependencies pulled off the Maven Project.
     */
    protected List<Dependency> dependencyManagementDependencies;

    /**
     * Extended to grab dependencies management section information.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        MavenProject project;
        try {
            project = (MavenProject) helper.evaluate("${project}");
        } catch (ExpressionEvaluationException eee) {
            throw new EnforcerRuleException("Unable to retrieve the MavenProject: ", eee);
        }

        DependencyManagement dependencyManagement = project.getDependencyManagement();
        if (dependencyManagement != null) {
            dependencyManagementDependencies = dependencyManagement.getDependencies();
        } else {
            dependencyManagementDependencies = Collections.emptyList();
        }

        super.execute(helper);

    }

    /**
     * Checks both the dependencies and dependencyManagement dependencies for banned items.
     *
     * @param theDependencies the dependencies to check for banned items
     * @param log             the log
     * @return any banned dependencies found
     * @throws EnforcerRuleException any exception encountered
     */
    @Override
    protected Set<Artifact> checkDependencies(Set<Artifact> theDependencies, Log log)
            throws EnforcerRuleException {

        Set<Artifact> dependenciesToCheck = new HashSet<>(theDependencies);

        // add in any dependencies from dependency management:
        for (Dependency dependency : dependencyManagementDependencies) {
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String version = dependency.getVersion();
            String scope = dependency.getScope();
            String type = dependency.getType();
            String classifier = dependency.getClassifier();
            ArtifactHandler artifactHandler = new DefaultArtifactHandler();
            Artifact artifact = new DefaultArtifact(groupId, artifactId, version, scope, type, classifier, artifactHandler);

            dependenciesToCheck.add(artifact);
        }

        return super.checkDependencies(dependenciesToCheck, log);

    }

}
