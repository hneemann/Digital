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
 * @fileoverview Generating Verilog for math blocks.
 * @author q.neutron@gmail.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.Verilog.math');

goog.require('Blockly.Verilog');

Blockly.Verilog['math_change'] = function(block) {
    // Add to a variable in place.
    var argument0 = Blockly.Verilog.valueToCode(block, 'DELTA',
        Blockly.Verilog.ORDER_UNARY_PLUS) || '0';
    var varName = Blockly.Verilog.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.Variables.NAME_TYPE);
    console.log(varName, argument0)
    return varName + ' = ' + varName + ' + ' +argument0 + ';\n';
};

Blockly.Verilog['math_arithmetic'] = function(block) {
    // Basic arithmetic operators, and power.
    var OPERATORS = {
      'ADD': [' + ', Blockly.Verilog.ORDER_UNARY_PLUS],
      'MINUS': [' - ', Blockly.Verilog.ORDER_UNARY_MINUS],
      'MULTIPLY': [' * ', Blockly.Verilog.ORDER_MULT],
      'DIVIDE': [' / ', Blockly.Verilog.ORDER_DIV],
      'REMAINDER': [' % ', Blockly.Verilog.ORDER_MOD]
    };
    var tuple = OPERATORS[block.getFieldValue('OP')];
    var operator = tuple[0];
    var order = tuple[1];
    var argument0 = Blockly.Verilog.valueToCode(block, 'A', order) || '0';
    var argument1 = Blockly.Verilog.valueToCode(block, 'B', order) || '0';
    var code = argument0 + operator + argument1;
    return [code, order];
  };