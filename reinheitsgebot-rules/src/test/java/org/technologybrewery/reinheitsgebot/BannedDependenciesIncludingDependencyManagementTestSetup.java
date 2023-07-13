package org.technologybrewery.reinheitsgebot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.enforcer.rules.EnforcerTestUtils;
import org.apache.maven.enforcer.rules.dependency.ReinheitsgebotResolverUtil;
import org.apache.maven.enforcer.rules.utils.MockEnforcerExpressionEvaluator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * This is based on the Maven Enforcer Rules {@link org.apache.maven.plugins.enforcer.BannedDependenciesTestSetup} class,
 * but was copied vice extended due to the need to override private methods.
 * <p>
 * It sets up the needed mock Maven project to test dependency management banning.
 */
@ExtendWith(MockitoExtension.class)
public class BannedDependenciesIncludingDependencyManagementTestSetup {

    @Mock
    private MavenProject project;

    @Mock
    private MavenSession session;

    @Mock
    private ReinheitsgebotResolverUtil resolverUtil;

    @InjectMocks
    private BannedDependenciesIncludingDependencyManagementRule rule;

    @Test
    void BannedDependenciesIncludingDependencyManagementTestSetup() throws IOException {
        this.excludes = new ArrayList<>();
        this.includes = new ArrayList<>();

        ArtifactStubFactory factory = new ArtifactStubFactory();
        
        when(session.getCurrentProject()).thenReturn(project);
        when(project.getDependencyArtifacts()).thenReturn(factory.getScopedArtifacts());
    
        project.setArtifacts(factory.getMixedArtifacts());

        // Getting the reference for this extension:
        project.setDependencyArtifacts(factory.getScopedArtifacts());
        this.dependencyManagement = project.getDependencyManagement();

        this.helper = EnforcerTestUtils.getHelper(project);
        ExpressionEvaluator eval;
        MojoExecution mockExecution = mock( MojoExecution.class );
        session.setCurrentProject( project );
        eval = new PluginParameterExpressionEvaluator( session, mockExecution );
        
        this.helper = DefaultEnforcementRuleHelper( session, eval, new SystemStreamLog(), null );
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

    private List<String> includes;

    private EnforcerRuleHelper helper;

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
        rule.execute();
    }

    public void addIncludeExcludeAndRunRule(String incAdd, String excAdd)
            throws EnforcerRuleException {
        excludes.add(excAdd);
        includes.add(incAdd);
        rule.execute();
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    private BannedDependenciesIncludingDependencyManagementRule newBannedDependenciesIncludingDependencyManagementRule() {
        return new BannedDependenciesIncludingDependencyManagementRule(session, resolverUtil) {
            protected Set<Artifact> getDependenciesToCheck(ProjectBuildingRequest buildingRequest) {
                MavenProject project = buildingRequest.getProject();

                // the integration with dependencyGraphTree is verified with the integration tests
                // for unit-testing
                return isSearchTransitive() ? project.getArtifacts() : project.getDependencyArtifacts();
            }
        };
    }

}
