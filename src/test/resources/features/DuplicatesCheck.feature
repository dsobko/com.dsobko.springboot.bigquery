Feature: Check duplicated rows in table

  Scenario Outline: Tables duplicates
    Given I check that there are no duplicated rows <tableName>

    Examples:
      | tableName    |
      | Test        |
