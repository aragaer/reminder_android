# vim: tabstop=4 expandtab shiftwidth=2 softtabstop=2
Feature: create reminder
  As a user
  I want to create a reminder
  So I could recall something later

  Scenario: Create a new reminder from app
    Given My app is running
     When I press the create button
     Then drawing window is opened
     When I draw a symbol
      And press save button
     Then drawing window is closed
      And reminder list now contains the new symbol

  Scenario: create from notification
    Given the status bar is expanded
      And the reminder notification is shown
     When I press the create button
     Then status bar is closed
      And drawing window is opened
     When I draw a symbol
      And press save button
     Then drawing window is closed

  Scenario: create with comment:
    Given the drawing window is opened
     When I draw a symbol
      And press the Next button
     Then drawing window is closed
      And reminder view window is opened
     When I type comment
      And press the back button
     Then reminder view window is closed
      And there is a message saying that reminder is saved

