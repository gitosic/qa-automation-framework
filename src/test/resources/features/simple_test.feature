Feature: Example API Tests
  Background:
    Given I setup the base URL

  @shouldPASS
  Scenario: Test API endpoint
    When I send a GET request to "/api/test"
    Then the response status should be 404

  @shouldPASS
  Scenario: Test API endpoint2
    When I send a GET request to "/api/test"
    Then the response status should be 404

  @should1
  Scenario: Test API
    When I send a GET request to "/example/api/v1/all-variants"
    Then the response status should be 200
    And I save "lifeSpan.effectiveTime" from the response
    And I request user by saved id
