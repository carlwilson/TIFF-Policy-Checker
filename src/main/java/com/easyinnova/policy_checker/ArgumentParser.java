/**
 * <h1>Field.java</h1> <p> This program is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version; or,
 * at your choice, under the terms of the Mozilla Public License, v. 2.0. SPDX GPL-3.0+ or MPL-2.0+.
 * </p> <p> This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License and the Mozilla Public License for more details. </p>
 * <p> You should have received a copy of the GNU General Public License and the Mozilla Public
 * License along with this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>
 * and at <a href="http://mozilla.org/MPL/2.0">http://mozilla.org/MPL/2.0</a> . </p> <p> NB: for the
 *  statement, include Easy Innova SL or other company/Person contributing the code. </p> <p>
 * 2015 Easy Innova, SL </p>
 */

package com.easyinnova.policy_checker;

import com.easyinnova.policy_checker.model.Field;
import com.easyinnova.policy_checker.model.Rules;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Adria Llorens on 27/12/2016.
 */
public class ArgumentParser {

  private Rules rules;
  private String path;
  private boolean error;
  private Map<String, Field> validRules;

  public ArgumentParser() {
    path = "";
    rules = new Rules();
    validRules = new HashMap<>();
    List<Field> fields = PolicyChecker.getPolicyCheckerFields();
    for (Field field : fields) {
      validRules.put(field.getName(), field);
    }
  }

  public boolean parse(List<String> params) {
    int idx = 0;
    if (params.size() == 0){
      error = true;
      return false;
    }
    error = false;
    while (!error && idx < params.size()) {
      String arg = params.get(idx);
      // -r --rule
      if (arg.equals("-r") || arg.equals("--rule")) {
        if (idx + 4 < params.size()) {
          String type = params.get(++idx);
          String tag = params.get(++idx);
          String operator = params.get(++idx);
          String value = params.get(++idx);
          if (validateRule(tag, operator, value, type)) {
            rules.addRule(tag, parseRuleOperator(operator), value, type.equals("warning"));
          } else {
            printOutErr("Rule specification malformed. See help for details ('-h').");
          }
        } else {
          printOutErr("You must specify the rule after '--rule' option.");
        }
      }
      // -l --list
      else if (arg.equals("-l") || arg.equals("--list")) {
        displayTagsList();
        error = true;
        return true;
      }
      // -h --help
      else if (arg.equals("-h") || arg.equals("--help")) {
        error = true;
      }
      // Input path
      else {
        if (new File(arg).exists()) {
          if (path.isEmpty()) {
            path = arg;
          } else {
            printOutErr("Only one file path allowed.");
          }
        } else {
          printOutErr("Invalid param: " + arg);
        }
      }
      idx++;
    }
    return !error;
  }

  private void displayTagsList() {
    System.out.println("Allowed rules tags (type) [valid values]:");
    for (String tag : validRules.keySet()) {
      Field field = validRules.get(tag);
      if (field.getValues() != null) {
        System.out.println("  " + field.getName() + " [" + String.join(", ", field.getValues()) + "]");
      } else {
        System.out.println("  " + field.getName() + " (" + field.getType() + ")");
      }
    }
  }

  private boolean validateRule(String tag, String operator, String value, String type) {
    // Type
    if (!type.equals("error") && !type.equals("warning")) {
      return false;
    }
    // Tag Operator Value
    if (validRules.containsKey(tag)) {
      Field field = validRules.get(tag);
      String op = parseRuleOperator(operator);
      if (field.getOperators().contains(op)) {
        if (field.getValues() != null) {
          // Specific values
          return field.getValues().contains(value);
        } else {
          // Free values
          if (field.getType().equals("integer")) {
            return isNumeric(value);
          } else if (field.getType().equals("boolean")) {
            return isBoolean(value);
          } else {
            return true;
          }
        }
      }
    }
    return false;
  }

  private String parseRuleOperator(String op) {
    if (op.equals("GT") || op.equals("gt")) {
      return ">";
    } else if (op.equals("LT") || op.equals("lt")) {
      return "<";
    } else if (op.equals("EQ") || op.equals("eq")) {
      return "=";
    }
    return "";
  }

  private boolean isNumeric(String str) {
    try {
      Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  private boolean isBoolean(String str) {
    try {
      Boolean.parseBoolean(str);
    } catch (Exception e) {
      return false;
    }
    return false;
  }

  public Rules getRules() {
    return rules;
  }

  public String getPath() {
    return path;
  }

  public boolean isError() {
    return error;
  }

  private void printOutErr(String msg) {
    error = true;
    System.out.println(msg);
  }
}
