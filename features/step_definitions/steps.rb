# vim: tabstop=4 expandtab shiftwidth=2 softtabstop=2

When /^I press the "([^\"]*)" actionbar item$/ do | buttonID |
  touch(query("ActionMenuItemView id:'#{buttonID}'"))
end

When /^I press the create button$/ do
  step 'I press the "add_new" actionbar item'
end

Then /^drawing window is opened$/ do
  wait_for_elements_exist(query("DrawView"))
end

When /^I draw a symbol$/ do
  draw_rect = query("DrawView")[0]["rect"]
  screen_rect = query("DecorView")[0]["rect"]

  draw_diff = draw_rect["width"] * 2 / 5

  left = draw_rect["center_x"] - draw_diff
  right = draw_rect["center_x"] + draw_diff
  top = draw_rect["center_y"] - draw_diff
  bottom = draw_rect["center_y"] + draw_diff

  left = left * 100 / screen_rect["width"]
  right = right * 100 / screen_rect["width"]
  top = top * 100 / screen_rect["height"]
  bottom = bottom * 100 /screen_rect["height"]

  performAction('drag', left, right, top, bottom, 10)
  performAction('drag', left, right, bottom, top, 10)
end

When /^I? ?press save button$/ do
  step 'I press the "no_extra" actionbar item'
end

Then /^drawing window is closed$/ do
  wait_for_elements_not_exist(query("DrawView"))
end

Then(/^reminder list now contains the new symbol$/) do
  pending # express the regexp above with the code you wish you had
end

Given /^the status bar is expanded$/ do
  performAction('drag', 50, 50, 0, 100, 10)
end

Given /^the reminder notification is shown$/ do
  step 'I wait up to 5 seconds for "Reminder saved" to appear'
end

Then(/^status bar is closed$/) do
  pending # express the regexp above with the code you wish you had
end

Given /^the drawing window is opened$/ do
  steps %Q{
    Given my app is running
     When I press the create button
     Then drawing window is opened
  }
end

When /^press the Next button$/ do
  step 'I press the "add_extra" actionbar item'
end

Then(/^reminder view window is opened$/) do
  pending # express the regexp above with the code you wish you had
end

When(/^I type comment$/) do
  pending # express the regexp above with the code you wish you had
end

When(/^press the back button$/) do
  pending # express the regexp above with the code you wish you had
end

Then(/^reminder view window is closed$/) do
  pending # express the regexp above with the code you wish you had
end

Then(/^there is a message saying that reminder is saved$/) do
  pending # express the regexp above with the code you wish you had
end

