package org.bitbucket.cpointe.reinheitsgebot;

import io.cucumber.java.DefaultDataTableEntryTransformer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.junit.Assert;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class BannedDependencyManagementDependenciesSteps {

    private BannedDependenciesIncludingDependencyManagementTestSetup setup;

    private EnforcerRuleException encounteredException;

    @Given("the following dependencies are added to dependency management:")
    public void the_following_dependencies_are_added_to_dependency_management(List<Dependency> dependencies)
            throws Exception {

        setup = new BannedDependenciesIncludingDependencyManagementTestSetup();
        setup.setSearchTransitive(false);
        setup.addDependencyManagementDependencies(dependencies);
    }

    @When("the dependency {string}:{string}:{string} is marked as banned")
    public void the_dependency_is_marked_as_banned(String groupId, String artifactId, String version) {
        try {
            this.setup.addExcludeAndRunRule(groupId + ":" + artifactId + ":" + version);
        } catch (EnforcerRuleException e) {
            encounteredException = e;
        }
    }

    @When("the dependency {string}:{string}:{string} is NOT marked as banned")
    public void the_dependency_is_not_marked_as_banned(String groupId, String artifactId, String version) throws Exception {
        this.setup.addIncludeExcludeAndRunRule (groupId + ":" + artifactId + ":" + version, "a:b:100");
    }

    @Then("the dependency is flagged as banned")
    public void the_dependency_is_flagged_as_banned() {
        Assert.assertNotNull("Expected a banned dependency to have been caught!", encounteredException);
    }

    @Then("the dependency is NOT flagged as banned")
    public void the_dependency_is_not_flagged_as_banned() {
        Assert.assertNull("Did NOT expect a banned dependency to have been caught!", encounteredException);
    }

    @DefaultDataTableEntryTransformer
    public Object defaultDataTableEntry(Map<String, String> fromValue, Type toValueType) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(fromValue.get("groupId"));
        dependency.setArtifactId(fromValue.get("artifactId"));
        dependency.setVersion(fromValue.get("version"));
        dependency.setScope(fromValue.get("scope"));

        return dependency;
    }

}
