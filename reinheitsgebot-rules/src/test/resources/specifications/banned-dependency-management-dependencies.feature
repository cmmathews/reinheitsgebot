Feature: Ban dependencies from within dependencyManagement

  Scenario Outline: Validate banned dependencies are flagged when defined in dependencyManagement
    Given the following dependencies are added to dependency management:
      | groupId | artifactId | version | scope   |
      | foo     | bar        | 1.0.0   | compile |
      | foo     | bom        | 1.0.0   | import  |
    When the dependency "<groupId>":"<artifactId>":"<version>" is marked as banned
    Then the dependency is flagged as banned

    Examples:
      | groupId | artifactId | version |
      | foo     | bar        | 1.0.0   |
      | foo     | bom        | 1.0.0   |

  Scenario Outline: Validate dependencies are not flagged when defined in dependencyManagement and not banned
    Given the following dependencies are added to dependency management:
      | groupId | artifactId | version | scope   |
      | foo     | bar        | 1.0.0   | compile |
      | foo     | bom        | 1.0.0   | import  |
    When the dependency "<groupId>":"<artifactId>":"<version>" is NOT marked as banned
    Then the dependency is NOT flagged as banned

    Examples:
      | groupId | artifactId | version |
      | foo     | bar        | 1.0.1   |
      | foo     | blah       | 1.0.0   |