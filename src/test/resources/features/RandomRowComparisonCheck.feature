Feature: Compare random rows

  Scenario Outline: Tables random rows
    Given I compare random rows <tableName>

    Examples:
      | tableName    |
      | Test        |
