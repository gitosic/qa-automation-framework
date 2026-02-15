Feature: Example API Tests
  Background:
    Given I setup the base URL

  @shouldPASS
  Scenario: Test API endpoint
    When I send a GET request to "/api/test"
    Then the response status should be 200

  @shouldPASS
  Scenario: Test API endpoint2
    When I send a GET request to "/api/test"
    Then the response status should be 200