/**
 * @license
 * Visual Blocks Language
 *
 * Copyright 2012 Google Inc.
 * https://developers.google.com/blockly/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Generating Verilog for logic blocks.
 * @author q.neutron@gmail.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.Verilog.logic');

goog.require('Blockly.Verilog');


Blockly.Verilog['controls_if'] = function(block) {
    //if/else/
    var n = 0;
    var code = '', branchCode, conditionCode;
    do {
        conditionCode = Blockly.Verilog.valueToCode(block, 'IF' + n,
            Blockly.Verilog.ORDER_NONE) || '0';
        branchCode = Blockly.Verilog.statementToCode(block, 'DO' + n);
        code += (n > 0 ? ' else ' : '') +
            'if (' + conditionCode + ') ' + '\tbegin\n' + branchCode+ ' end ';
        ++n;
    } while (block.getInput('IF' + n));

  if (block.getInput('ELSE')) {
    branchCode = Blockly.Verilog.statementToCode(block, 'ELSE');
    code += ' else ' + '\tbegin\n ' + branchCode + ' end ';
  }
  return code + '\n';
};


Blockly.Verilog['controls_ifelse'] = Blockly.Verilog['controls_if'];


Blockly.Verilog['logic_compare'] = function(block) {
    // Comparison operator.
    var OPERATORS = {
      'EQ': '==',
      'NEQ': '!=',
      'LT': '<',
      'LTE': '<=',
      'GT': '>',
      'GTE': '>='
    };
    var operator = OPERATORS[block.getFieldValue('OP')];
    var order = Blockly.Verilog.ORDER_ATOMIC;
    var argument0 = Blockly.Verilog.valueToCode(block, 'A', order) || '0';
    var argument1 = Blockly.Verilog.valueToCode(block, 'B', order) || '0';
    var code = argument0 + ' ' + operator + ' ' + argument1;
    return [code, order];
  };


  Blockly.Verilog['logic_operation'] = function(block) {
    // Operations 'and', 'or'.
    var operator = (block.getFieldValue('OP') == 'AND') ? '&' : '|';
    var order = (operator == 'and') ? Blockly.Verilog.ORDER_LOGICAL_AND :
        Blockly.Verilog.ORDER_LOGIC_OR;
    var argument0 = Blockly.Verilog.valueToCode(block, 'A', order);
    var argument1 = Blockly.Verilog.valueToCode(block, 'B', order);
    if (!argument0 && !argument1) {
      // If there are no arguments, then the return value is false.
      argument0 = '0';
      argument1 = '0';
    } else {
      // Single missing arguments have no effect on the return value.
      var defaultArgument = (operator == 'and') ? '1' : '0';
      if (!argument0) {
        argument0 = defaultArgument;
      }
      if (!argument1) {
        argument1 = defaultArgument;
      }
    }
    var code = argument0 + ' ' + operator + ' ' + argument1;
    return [code, order];
  };


  Blockly.Verilog['logic_negate'] = function(block) {
    // Negation.
    var argument0 = Blockly.Verilog.valueToCode(block, 'BOOL',
        Blockly.Verilog.ORDER_NEG) || '1';
    var code = '!' + argument0;
    return [code, Blockly.Verilog.ORDER_NEG];
  };


  Blockly.Verilog['logic_boolean'] = function(block) {
    // Boolean values true and false.
    var code = (block.getFieldValue('BOOL') == 'TRUE') ? '1' : '0';
    return [code, Blockly.Verilog.ORDER_ATOMIC];
  };


  Blockly.Verilog['logic_null'] = function(block) {
    // Null data type.
    return ['X', Blockly.Verilog.ORDER_ATOMIC];
  };
  
  Blockly.Verilog['logic_ternary'] = function(block) {
    // Ternary operator.
    var value_if = Blockly.Verilog.valueToCode(block, 'IF',
        Blockly.Verilog.ORDER_CONDITIONAL) || '0';
    var value_then = Blockly.Verilog.valueToCode(block, 'THEN',
        Blockly.Verilog.ORDER_CONDITIONAL) || 'X';
    var value_else = Blockly.Verilog.valueToCode(block, 'ELSE',
        Blockly.Verilog.ORDER_CONDITIONAL) || 'X';
    var code = '('+ value_if + ')' + ' ? ' + value_then + ' : ' + value_else;
    return [code, Blockly.Verilog.ORDER_ATOMIC];
  };
  