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
 * @fileoverview Generating Verilog for loop blocks.
 * @author q.neutron@gmail.com (Quynh Neutron)
 */
'use strict';

goog.provide('Blockly.Verilog.loops');

goog.require('Blockly.Verilog');


Blockly.Verilog['forever_loop'] = function(block) {
    var statements_body = Blockly.Verilog.statementToCode(block, 'body');
    // TODO: Assemble Verilog into code variable.
    var code = 'forever begin\n' + statements_body + 'end\n';
    return code;
};

Blockly.Verilog['repeat_loop'] = function(block) {
    var value_number = Blockly.Verilog.valueToCode(block, 'number', Blockly.Verilog.ORDER_ATOMIC);
    var statements_code = Blockly.Verilog.statementToCode(block, 'code');
    // TODO: Assemble Verilog into code variable.
    var code = 'repeat ('+ value_number + ') begin\n' + statements_code + 'end\n';
    return code;
};

Blockly.Verilog['while_loop'] = function(block) {
    var value_condition = Blockly.Verilog.valueToCode(block, 'condition', Blockly.Verilog.ORDER_ATOMIC);
    var statements_code = Blockly.Verilog.statementToCode(block, 'code');
    // TODO: Assemble Verilog into code variable.
    var code = 'while ( ' + value_condition + ') begin\n' + statements_code + 'end\n';
    return code;
  };


