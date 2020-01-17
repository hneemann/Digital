/**
*@license
*Visual Blocks Programming
*
* Copyright 2019 Google Inc.
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
 * @fileoverview Helper functions for generating Verilog for blocks.
 * @author daarond@gmail.com (Daaron Dwyer)
 */
'use strict'

goog.provide('Blockly.Verilog')

goog.require('Blockly.Generator')


/**
 * Verilog code generator.
 * @type {!Blockly.Generator}
 */
 Blockly.Verilog = new Blockly.Generator('Verilog')


/**
 * List of illegal variable names.
 * This is not intended to be a security feature.  Blockly is 100% client-side,
 * so bypassing this list is trivial.  This is intended to prevent users from
 * accidentally clobbering a built-in object or function.
 * @private
 */
 Blockly.Verilog.addReservedWords(
     'Verilog,' 
     + 'always,and,assign,begin,buf,bufif0,bufif1,casex,case,casez,cmos,deassign,' + 
     'default,defparam,disable,edge,else,end,endcase,endmodule,endfunction,endprimitive,endspecify,' +
     'endtable,endtask,event,for,force,forever,for,function,highz0,highz1,if,ifnone,initial,inout,' +
     'input,integer,join,large,macromodule,medium,module,nand,negedge,nmos,nor,not,notif0,notif1,' +
     'or,output,parameter,pmos,posedge,primitive,pull0,pull1,pullup,pulldown,rcmos,real,realtime,' +
     'reg,release,repeat,rnmos,rpmos,rtan,rtranif0,rtranif1,scalared,small,specify,specparam,strong0' +
     'strong1,supply0,supply1,table,task,time,tran,tranif0,tranif1,tri,tri0,tri1,triand,trior,' +
     'trireg,vectored,wait,wand,weak0,weak1,while,wire,wor,xor,xnor'
 )



/**
* Order of operation ENUMs.
* http://php.net/manual/en/language.operators.precedence.php
*/
Blockly.Verilog.ORDER_ATOMIC = 0
Blockly.Verilog.ORDER_MEMBER = 1            //[]
Blockly.Verilog.ORDER_BRACKET = 1.1         //()
Blockly.Verilog.ORDER_LOGICNEG = 1.2        //!
Blockly.Verilog.ORDER_NEG = 1.2             //~
Blockly.Verilog.ORDER_AND = 1.2             //&
Blockly.Verilog.ORDER_OR = 1.2              //|
Blockly.Verilog.ORDER_NAND = 1.2            //~&
Blockly.Verilog.ORDER_NOR = 1.2             //~|
Blockly.Verilog.ORDER_XOR = 1.2             //~^ or ^~
Blockly.Verilog.ORDER_UNARY_PLUS = 2        //+
Blockly.Verilog.ORDER_UNARY_MINUS = 2       //-
Blockly.Verilog.ORDER_CONCAT = 3            //{}
Blockly.Verilog.ORDER_REPLICA = 4           //{{}}
Blockly.Verilog.ORDER_MULT = 5              //*
Blockly.Verilog.ORDER_DIV = 5               // /
Blockly.Verilog.ORDER_MOD = 5               //%
Blockly.Verilog.ORDER_BINARY_PLUS = 6       //+
Blockly.Verilog.ORDER_BINARY_MINUS = 6      //-
Blockly.Verilog.ORDER_SHIFT_LEFT = 7        //<<
Blockly.Verilog.ORDER_SHIFT_RIGHT = 7       //>>
Blockly.Verilog.ORDER_GREATER_THAN = 8      //>
Blockly.Verilog.ORDER_GREATER_THAN_EQ = 8   //>=
Blockly.Verilog.ORDER_LESS_THAN = 8         //<
Blockly.Verilog.ORDER_LESS_THAN_EQ = 8      //<=
Blockly.Verilog.ORDER_LOGIC_EQ = 9          //==
Blockly.Verilog.ORDER_LOGIC_INEQ = 9        //!=
Blockly.Verilog.ORDER_CASE_EQ = 10          //===
Blockly.Verilog.ORDER_CASE_INEQ = 10        //!==
Blockly.Verilog.ORDER_BITWISE_AND = 11      //&
Blockly.Verilog.ORDER_BITWISE_XOR = 12      //^
Blockly.Verilog.ORDER_BITWISE_XNOR = 12     //^~ or ~^
Blockly.Verilog.ORDER_BITWISE_OR = 13       //|
Blockly.Verilog.ORDER_LOGIC_AND = 14        //&&
Blockly.Verilog.ORDER_LOGIC_OR = 15         //||
Blockly.Verilog.ORDER_CONDITIONAL = 16      //?:
Blockly.Verilog.ORDER_NONE = 50     

Blockly.Verilog.INFINITE_LOOP_TRAP = null


/**
 * Initialise the database of variable names.
 * @param {!Blockly.Workspace} workspace Workspace to generate code from.
 */
Blockly.Verilog.init = function(workspace){
  // Create a dictionary of definitions to be printed before the code.
  Blockly.Verilog.definitions_ = Object.create(null);
  // Create a dictionary mapping desired function names in definitions_
  // to actual function names (to avoid collisions with user functions).
  Blockly.Verilog.functionNames_ = Object.create(null);

  if(!Blockly.Verilog.variableDB_){
    Blockly.Verilog.variableDB_ = 
      new Blockly.Names(Blockly.Verilog.RESERVED_WORDS_)
  } else {
    Blockly.Verilog.variableDB_.reset()
  }

  Blockly.Verilog.variableDB_.setVariableMap(workspace.getVariableMap())

  var defvars = []

  // Add developer variables (not created or named by the user).
  var devVarList = Blockly.Variables.allDeveloperVariables(workspace);
  for (var i = 0; i < devVarList.length; i++) {
    defvars.push(Blockly.Verilog.variableDB_.getName(devVarList[i],
        Blockly.Names.DEVELOPER_VARIABLE_TYPE));
  }


  // Add user variables, but only ones that are being used.
  var variables = Blockly.Variables.allUsedVarModels(workspace);
  for (var i = 0; i < variables.length; i++) {
    defvars.push(Blockly.Verilog.variableDB_.getName(variables[i].getId(),
        Blockly.Variables.NAME_TYPE));
  }
}


/**
 * Prepend the generated code with the variable definitions.
 * @param {string} code Generated code.
 * @return {string} Completed code.
 */
Blockly.Verilog.finish = function(code) {
  // Convert the definitions dictionary into a list.
  var definitions = [];
  for (var name in Blockly.Verilog.definitions_) {
    definitions.push(Blockly.Verilog.definitions_[name]);
  }
  // Clean up temporary data.
  delete Blockly.Verilog.definitions_;
  delete Blockly.Verilog.functionNames_;
  Blockly.Verilog.variableDB_.reset();
  return definitions.join('\n\n') + '\n\n\n' + code;
};


/**
 * Naked values are top-level blocks with outputs that aren't plugged into
 * anything.  A trailing semicolon is needed to make this legal.
 * @param {string} line Line of generated code.
 * @return {string} Legal line of code.
 */
Blockly.Verilog.scrubNakedValue = function(line) {
  return line + ';\n';
};



/**
 * Encode a string as a properly escaped Verilog string, complete with
 * quotes.
 * @param {string} string Text to encode.
 * @return {string} Verilog string.
 * @private
 */
Blockly.Verilog.quote_ = function(string) {
  string = string.replace(/\\/g, '\\\\')
                 .replace(/\n/g, '\\\n')
                 .replace(/'/g, '\\\'');
  return '\'' + string + '\'';
};




/**
 * Common tasks for generating Verilog from blocks.
 * Handles comments for the specified block and any connected value blocks.
 * Calls any statements following this block.
 * @param {!Blockly.Block} block The current block.
 * @param {string} code The Verilog code created for this block.
 * @param {boolean=} opt_thisOnly True to generate code for only this statement.
 * @return {string} Verilog code with comments and subsequent blocks added.
 * @private
 */
Blockly.Verilog.scrub_ = function(block, code, opt_thisOnly) {
  var commentCode = '';
  // Only collect comments for blocks that aren't inline.
  if (!block.outputConnection || !block.outputConnection.targetConnection) {
    // Collect comment for this block.
    var comment = block.getCommentText();
    comment = Blockly.utils.wrap(comment, Blockly.Verilog.COMMENT_WRAP - 3);
    if (comment) {
      commentCode += Blockly.Verilog.prefixLines(comment, '// ') + '\n';
    }
    // Collect comments for all value arguments.
    // Don't collect comments for nested statements.
    for (var i = 0; i < block.inputList.length; i++) {
      if (block.inputList[i].type == Blockly.INPUT_VALUE) {
        var childBlock = block.inputList[i].connection.targetBlock();
        if (childBlock) {
          var comment = Blockly.Verilog.allNestedComments(childBlock);
          if (comment) {
            commentCode += Blockly.Verilog.prefixLines(comment, '// ');
          }
        }
      }
    }
  }
  var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
  var nextCode = opt_thisOnly ? '' : Blockly.Verilog.blockToCode(nextBlock);
  return commentCode + code + nextCode;
};





