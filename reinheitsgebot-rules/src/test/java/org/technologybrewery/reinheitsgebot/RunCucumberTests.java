package org.technologybrewery.reinheitsgebot;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/specifications",
        plugin = {"json:target/cucumber-html-reports/cucumber.json"},
        tags = "not @manual")
public class RunCucumberTests {
}
