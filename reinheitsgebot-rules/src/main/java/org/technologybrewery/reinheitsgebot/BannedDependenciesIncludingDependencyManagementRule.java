package org.technologybrewery.reinheitsgebot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rules.dependency.ReinheitsgebotBannedDependencyBase;
import org.apache.maven.enforcer.rules.dependency.ReinheitsgebotResolverUtil;
import org.apache.maven.enforcer.rules.utils.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;

/**
 * Extends the standard Maven Enforcer Banned Dependency rule to include support for searching for banned dependencies
 * within Dependency Management as well as Dependencies.
 * <p>
 * This is useful when you ban a dependency that may be in a parent pom's dependency management declaration, but is not
 * actually used as a dependency until further in the multi-module build.
 * <p>
 * A common examples is BOM declaration in a parent pom. When this occurs, your banned dependencies in the parent
 * module are missed and become GAV errors in the child, limiting the usefulness of the banned dependency check for
 * that scenario.
 */
public class BannedDependenciesIncludingDependencyManagementRule extends ReinheitsgebotBannedDependencyBase {
    
    BannedDependenciesIncludingDependencyManagementRule(MavenSession session, ReinheitsgebotResolverUtil resolverUtil) {
        super(session, resolverUtil);
        //TODO Auto-generated constructor stub
    }
    
    /**
     * Dependency management dependencies pulled off the Maven Project.
     */
    protected List<Dependency> dependencyManagementDependencies;

    private boolean shouldIfail = false;

    private List<String> listParameters;

    @Inject
    private MavenProject project;

    @Inject
    private MavenSession session;

    @Inject
    private RuntimeInformation runtimeInformation;

    /**
     * Extended to grab dependencies management section information.
     * <p>
     * {@inheritDoc}
     */

    /*
    @Inject
    BannedDependenciesIncludingDependencyManagementRule(MavenSession session, ResolverUtil resolverUtil) {
        super(session, resolverUtil);
    }
    */
    //@Override
    public boolean isCacheable() {
        boolean responseCacheable = true;
        return responseCacheable;
    }

    //@Override
    public boolean isResultValid(EnforcerRule cachedRule){
        return true;
    }

    //@Override
    protected boolean validate(Artifact artifact) {
        return !ArtifactUtils.matchDependencyArtifact(artifact, getExcludes())
                || ArtifactUtils.matchDependencyArtifact(artifact, getIncludes());
    }

    //@Override
    protected String getErrorMessage() {
        return "banned via the exclude/include list";
    }

    @Override
    public void execute() throws EnforcerRuleException {
        MavenProject project;
        /*
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
        */

        //getLog().info("Retrieved Target Folder: " + project.getBuild().getDirectory());
        //getLog().info("Retrieved ArtifactId: " + project.getArtifactId());
        //getLog().info("Retrieved Project: " + project);
        getLog().info("Retrieved Maven version: " + runtimeInformation.getMavenVersion());
        getLog().info("Retrieved Session: " + session);
        getLog().warnOrError("Parameter shouldIfail: " + shouldIfail);
        getLog().info(() -> "Parameter listParameters: " + listParameters);

        if (this.shouldIfail) {
            throw new EnforcerRuleException("Failing because my param said so.");
        }

        super.execute();

    }

    /**
     * Checks both the dependencies and dependencyManagement dependencies for banned items.
     *
     * @param theDependencies the dependencies to check for banned items
     * @param log             the log
     * @return any banned dependencies found
     * @throws EnforcerRuleException any exception encountered
     */
    
    // TODO this is no longer overriding super function
    protected Set<Artifact> checkDependencies(Set<Artifact> theDependencies, Log log)
            throws EnforcerRuleException {

        // check standard dependencies for banned artifacts:
       //Set<Artifact> bannedDependencies = checkDependencies(theDependencies, log);

        // check dependencies from dependency management for banned artifacts:
        Set<Artifact> bannedDependenciesFromDependencyManagement = checkForDependencyManagementBannedDependencies(log);

        Set<Artifact> combinedBannedDependencies = new TreeSet<>();
        if (theDependencies != null) {
            combinedBannedDependencies.addAll(theDependencies);
        }
        if (bannedDependenciesFromDependencyManagement != null) {
            combinedBannedDependencies.addAll(bannedDependenciesFromDependencyManagement);
        }

        return combinedBannedDependencies;

    }

    private Set<Artifact> checkForDependencyManagementBannedDependencies(Log log) throws EnforcerRuleException {
        Set<Artifact> dependenciesToCheck = new HashSet<>();
        for (Dependency dependency : dependencyManagementDependencies) {
            String groupId = dependency.getGroupId();
            String artifactId = dependency.getArtifactId();
            String version = dependency.getVersion();
            String scope = dependency.getScope();
            String type = dependency.getType();
            String classifier = dependency.getClassifier();
            ArtifactHandler artifactHandler = new FromDependencyManagementArtifactHandler();
            try {
                Artifact artifact = new DefaultArtifact(groupId, artifactId, version, scope, type, classifier, artifactHandler);
                dependenciesToCheck.add(artifact);

            } catch (Exception e) {
                throw new EnforcerRuleException("Could not resolve artifact " + groupId + ":" + artifactId + ":" + version);
            }
        }

        return checkDependencies(dependenciesToCheck, log);
    }

    //@Override
    protected CharSequence getErrorMessage(Artifact artifact) {
        CharSequence errorMessage;
        ArtifactHandler artifactHandler = artifact.getArtifactHandler();
        if (artifactHandler instanceof FromDependencyManagementArtifactHandler) {
            errorMessage = "Found Banned Dependency (via dependencyManagement): " + artifact.getId() + System.lineSeparator();

        } else {
            errorMessage = "Found Banned Dependency: " + artifact.getId() + System.lineSeparator();

        }
        return errorMessage;
    }

    @Override
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
        return Boolean.toString(shouldIfail);
    }
 
    @Override
    public String toString() {
        return String.format("BannedDependenciesIncludingDependencyManagementRule[shouldIfail=%b]", shouldIfail);
    }

}
