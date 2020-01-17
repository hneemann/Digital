/**
 * @license
 * Visual Blocks Language
 *
 * Copyright 2015 Google Inc.
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
 * @fileoverview Generating Verilog for variable blocks.
 * @author daarond@gmail.com (Daaron Dwyer)
 */
'use strict';

goog.provide('Blockly.Verilog.variables');

goog.require('Blockly.Verilog');


Blockly.Verilog['variables_get'] = function(block) {
    // Variable getter.
    var code = Blockly.Verilog.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.Variables.NAME_TYPE);
    return [code, Blockly.Verilog.ORDER_ATOMIC];
  };


  Blockly.Verilog['variables_set'] = function(block) {
    // Variable setter.
    var argument0 = Blockly.Verilog.valueToCode(block, 'VALUE',
        Blockly.Verilog.ORDER_NONE) || '0';
    var varName = Blockly.Verilog.variableDB_.getName(block.getFieldValue('VAR'),
        Blockly.Variables.NAME_TYPE);
    return varName + ' = ' + argument0 +';' + '\n';
  };

  
