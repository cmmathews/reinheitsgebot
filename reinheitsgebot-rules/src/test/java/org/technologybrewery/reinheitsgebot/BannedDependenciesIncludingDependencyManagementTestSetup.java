package org.technologybrewery.reinheitsgebot;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.enforcer.rules.EnforcerTestUtils;
//import org.apache.maven.enforcer.rule.api.EnforcerTestUtils;
//import org.apache.maven.plugins.enforcer.MockProject;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is based on the Maven Enforcer Rules {@link org.apache.maven.plugins.enforcer.BannedDependenciesTestSetup} class,
 * but was copied vice extended due to the need to override private methods.
 * <p>
 * It sets up the needed mock Maven project to test dependency management banning.
 */
public class BannedDependenciesIncludingDependencyManagementTestSetup {

    public BannedDependenciesIncludingDependencyManagementTestSetup() throws IOException {
        this.excludes = new ArrayList<>();
        this.includes = new ArrayList<>();

        ArtifactStubFactory factory = new ArtifactStubFactory();

        MockProject project = new MockProject();
        project.setArtifacts(factory.getMixedArtifacts());

        // Getting the reference for this extension:
        project.setDependencyArtifacts(factory.getScopedArtifacts());
        this.dependencyManagement = project.getDependencyManagement();

        this.helper = EnforcerTestUtils.getHelper(project);

        this.rule = newBannedDependenciesIncludingDependencyManagementRule();
        this.rule.setMessage(null);

        this.rule.setExcludes(this.excludes);
        this.rule.setIncludes(this.includes);
    }

    /**
     * Added for this extension.
     */
    protected DependencyManagement dependencyManagement;

    private List<String> excludes;

    private final List<String> includes;

    private final BannedDependenciesIncludingDependencyManagementRule rule;

    private final EnforcerRuleHelper helper;

    public void setSearchTransitive(boolean searchTransitive) {
        rule.setSearchTransitive(searchTransitive);
    }

    /**
     * Adds the passed {@link Dependency} instances into the mock Maven project.
     *
     * @param dependencies dependencies to add
     */
    public void addDependencyManagementDependencies(List<Dependency> dependencies) {
        dependencyManagement.setDependencies(dependencies);
    }

    public void addExcludeAndRunRule(String toAdd)
            throws EnforcerRuleException {
        excludes.add(toAdd);
        rule.execute(helper);
    }

    public void addIncludeExcludeAndRunRule(String incAdd, String excAdd)
            throws EnforcerRuleException {
        excludes.add(excAdd);
        includes.add(incAdd);
        rule.execute(helper);
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    private BannedDependenciesIncludingDependencyManagementRule newBannedDependenciesIncludingDependencyManagementRule() {
        return new BannedDependenciesIncludingDependencyManagementRule() {
            @Override
            protected Set<Artifact> getDependenciesToCheck(ProjectBuildingRequest buildingRequest) {
                MavenProject project = buildingRequest.getProject();

                // the integration with dependencyGraphTree is verified with the integration tests
                // for unit-testing
                return isSearchTransitive() ? project.getArtifacts() : project.getDependencyArtifacts();
            }
        };
    }

}
