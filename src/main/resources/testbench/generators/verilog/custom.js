Blockly.Verilog['one'] = function(block) {
    // TODO: Assemble Verilog into code variable.
    var code = ''+1;
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_NONE];
  };


  Blockly.Verilog['zero'] = function(block) {
    // TODO: Assemble Verilog into code variable.
    var code = ''+0;
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_NONE];
  };


  Blockly.Verilog['high_impedence'] = function(block) {
    // TODO: Assemble Verilog into code variable.
    var code = 'Z';
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['dont_care'] = function(block) {
    // TODO: Assemble Verilog into code variable.
    var code = 'X';
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['always_blk'] = function(block) {
    var value_condition = Blockly.Verilog.valueToCode(block, 'condition', Blockly.Verilog.ORDER_ATOMIC);
    var statements_body = Blockly.Verilog.statementToCode(block, 'body');
    // TODO: Assemble Verilog into code variable.
    var code = 'always @ (' + value_condition + ')\n' + 'begin\n' + statements_body + '\n'+ 'end\n';
    return code;
  }

  Blockly.Verilog['decimal_binary'] = function(block) {
    var value_val = Blockly.Verilog.valueToCode(block, 'val', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var value = parseFloat(value_val);
    var stringV = value.toString(2).length;
    var code = stringV + "'b" + value.toString(2) +';\n';
    return code;
  };

  Blockly.Verilog['decimal_hexa'] = function(block) {
    var value_val = Blockly.Verilog.valueToCode(block, 'val', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var value = parseFloat(value_val);
    var stringV = value.toString(2).length;
    var code = stringV + "'h" + value.toString(16) +';\n';
    return code;
  };

  Blockly.Verilog['decimal_octa'] = function(block) {
    var value_val = Blockly.Verilog.valueToCode(block, 'val', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var value = parseFloat(value_val);
    var stringV = value.toString(2).length;
    var code = stringV + "'o" + value.toString(8) +';\n';
    return code;
  };

  Blockly.Verilog['math_number'] = function(block) {
    var code = parseFloat(block.getFieldValue('NUM'));
    var order = code >= 0 ? Blockly.Verilog.ORDER_ATOMIC :
      Blockly.Verilog.ORDER_NEG;
    return [code, order];
  };
  
  Blockly.Verilog['module_dec'] = function(block) {
    var text_modname = block.getFieldValue('modName');
    var text_varnames = block.getFieldValue('varNames');
    // TODO: Assemble Verilog into code variable.
    if(text_varnames === "varNames")
      var code = 'module ' + text_modname + '();\n';
    else 
      var code = 'module ' + text_modname + ' ('+ text_varnames+ ')' + ';\n';
    return code;
  };
  
  Blockly.Verilog['end_module'] = function(block) {
    // TODO: Assemble Verilog into code variable.
    var code = 'endmodule\n';
    return code;
  };

  Blockly.Verilog['pos_edge'] = function(block) {
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'posedge ' + value_name;
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['neg_edge'] = function(block) {
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'negedge ' + value_name;
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['input_block'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'input ' + text_name + ';\n';
    }else{
      code = 'input ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['output_block'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'output ' + text_name + ';\n';
    }else{
      code = 'output ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['wire_block'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'wire ' + text_name + ';\n';
    }else{
      code = 'wire ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['reg_block'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'reg ' + text_name + ';\n';
    }else{
      code = 'reg ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['inout_block'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'inout ' + text_name + ';\n';
    }else{
      code = 'inout ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['and_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'and ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };
  
  Blockly.Verilog['or_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'or ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };

  Blockly.Verilog['xor_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'xor ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };

  Blockly.Verilog['nand_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'nand ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };

  Blockly.Verilog['nor_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'nor ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };

  Blockly.Verilog['xnor_block'] = function(block) {
    var text_gname = block.getFieldValue('gName');
    var text_oname = block.getFieldValue('oName');
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'xnor ' + text_gname + ' (' + text_oname + ', ' + value_arg1 + ', ' + value_arg2
    + ' )' +';\n';
    return code;
  };

  Blockly.Verilog['not_gate'] = function(block) {
    var text_out = block.getFieldValue('out');
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = 'not ' + ' (' + text_out + ', ' + value_name + ' )'+ ';\n';
    return code;
  };

  Blockly.Verilog['assign_block'] = function(block) {
    var variable_var = Blockly.Verilog.variableDB_.getName(block.getFieldValue('var'), Blockly.Variables.NAME_TYPE);
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    if(value_name == '')
      var code = 'assign ' + variable_var + ' = X' + ';\n';
    else
      var code = 'assign ' + variable_var + ' = ' + value_name+ ';\n';
    return code;
  };

  Blockly.Verilog['module_test'] = function(block) {
    var text_modname = block.getFieldValue('modName');
    // TODO: Assemble Verilog into code variable.
    var code = 'module ' + text_modname + '();\n';
    return code;
  };

  Blockly.Verilog['input_simu'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'reg ' + text_name + ';\n';
    }else{
      code = 'reg ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['output_simu'] = function(block) {
    var text_name = block.getFieldValue('NAME');
    var value_size = Blockly.Verilog.valueToCode(block, 'size', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code;
    if(value_size == 0 || value_size == 1){
      code = 'wire ' + text_name + ';\n';
    }else{
      code = 'wire ' + '['+ (parseInt(value_size)-1)+':'+'0] '+ text_name + ';\n';
    }
    return code;
  };

  Blockly.Verilog['decimal_binary_return'] = function(block) {
    var valuex = Blockly.Verilog.valueToCode(block, 'number', Blockly.Verilog.ORDER_NONE);
    var value = parseFloat(valuex);
    if(isNaN(value))
      var code = "1'bx";
    else{
      var stringV = value.toString(2).length;
      var code = stringV + "'b" + value.toString(2);
    }
    return [code, Blockly.Verilog.ORDER_NONE];
  };


  Blockly.Verilog['decimal_hexa_return'] = function(block) {
    var valuex = Blockly.Verilog.valueToCode(block, 'number', Blockly.Verilog.ORDER_NONE);
    var value = parseFloat(valuex);
    if(isNaN(value))
      var code = "1'hx";
    else{
      var stringV = value.toString(2).length;
      var code = stringV + "'h" + value.toString(16);
    }
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['decimal_octal_return'] = function(block) {
    var valuex = Blockly.Verilog.valueToCode(block, 'number', Blockly.Verilog.ORDER_NONE);
    var value = parseFloat(valuex);
    if(isNaN(value))
      var code = "1'ox";
    else{
      var stringV = value.toString(2).length;
      var code = stringV + "'o" + value.toString(8);
    }
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['intial'] = function(block) {
    var statements_body = Blockly.Verilog.statementToCode(block, 'body');
    // TODO: Assemble Verilog into code variable.
    var code = 'initial begin\n' + statements_body + 'end\n';
    return code;
  };

  Blockly.Verilog['intial_par'] = function(block) {
    var statements_body = Blockly.Verilog.statementToCode(block, 'body');
    // TODO: Assemble Verilog into code variable.
    var code = 'initial fork\n' + statements_body + 'join\n';
    return code;
  };

  Blockly.Verilog['display_block'] = function(block) {
    var text_text = block.getFieldValue('text');
    // TODO: Assemble Verilog into code variable.
    var code = '$display (' +'"' + text_text + '")' + ';\n';
    return code;
  };

  Blockly.Verilog['time_block'] = function(block) {
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var statements_arg2 = Blockly.Verilog.statementToCode(block, 'arg2');
    // TODO: Assemble Verilog into code variable.
    if(value_arg1 == '')
      var code = '#1\n'+ statements_arg2 + '\n';
    else
      var code = '#' + value_arg1 + '\n' + statements_arg2 + '\n';
    return code;
  };

  Blockly.Verilog['monitor_block'] = function(block) {
    var text_names = block.getFieldValue('names');
    // TODO: Assemble Verilog into code variable.
    var code = '$monitor ( ' + text_names + ' );\n';
    return code;
  };

  Blockly.Verilog['finish_block'] = function(block) {
  // TODO: Assemble Verilog into code variable.
  var code = '$finish;\n';
  return code;
  };

  Blockly.Verilog['if_else_block'] = function(block) {
    var value_condition_if = Blockly.Verilog.valueToCode(block, 'condition_if', Blockly.Verilog.ORDER_ATOMIC);
    var statements_if_code = Blockly.Verilog.statementToCode(block, 'if_code');
    var value_condition_else = Blockly.Verilog.valueToCode(block, 'condition_else', Blockly.Verilog.ORDER_ATOMIC);
    var statements_else_code = Blockly.Verilog.statementToCode(block, 'else_code');
    // TODO: Assemble Verilog into code variable.
    var code = 'if ('+ value_condition_if + ') ' + 'begin \n' + statements_if_code +
    'end\n' + 'else if (' + value_condition_else + ') ' + 'begin \n' + statements_else_code + 'end';
    return code;
  };

  Blockly.Verilog['logic_operation_2'] = function(block) {
    // Operations 'and', 'or'.
    var operator = (block.getFieldValue('OP') == 'AND') ? 'and' : 'or';
    var order = (operator == 'and') ? Blockly.Verilog.ORDER_AND :
        Blockly.Verilog.ORDER_OR;
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

  Blockly.Verilog['always_simu'] = function(block) {
    var value_delay = Blockly.Verilog.valueToCode(block, 'delay', Blockly.Verilog.ORDER_ATOMIC);
    var statements_code = Blockly.Verilog.statementToCode(block, 'code');
    // TODO: Assemble Verilog into code variable.
    var code = 'always #' + value_delay + ' '+ statements_code + '\n';
    return code;
  };

  Blockly.Verilog['logic_operation3'] = function(block) {
    // Basic arithmetic operators, and power.
    var OPERATORS = {
      'And': [' & ', Blockly.Verilog.ORDER_BITWISE_AND],
      'Or': [' | ', Blockly.Verilog.ORDER_BITWISE_OR],
      'Xor': [' ^ ', Blockly.Verilog.ORDER_BITWISE_XOR],
      'Xnor': [' ~^ ', Blockly.Verilog.ORDER_BITWISE_XNOR]
    };
    var tuple = OPERATORS[block.getFieldValue('OP')];
    var operator = tuple[0];
    var order = tuple[1];
    var argument0 = Blockly.Verilog.valueToCode(block, 'A', order) || '0';
    var argument1 = Blockly.Verilog.valueToCode(block, 'B', order) || '0';
    var code = argument0 + operator + argument1;
    return [code, order];
  };

  Blockly.Verilog['logic_negate3'] = function(block) {
    // Negation.
    var argument0 = Blockly.Verilog.valueToCode(block, 'BOOL',
        Blockly.Verilog.ORDER_NEG) || '1';
    var code = '~' + argument0;
    return [code, Blockly.Verilog.ORDER_NEG];
  };

  Blockly.Verilog['variables_set_parallel'] = function(block) {
    var text_var = block.getFieldValue('var');
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = text_var + ' <= ' + value_name + ';\n';
    return code;
  };

  Blockly.Verilog['bit_select'] = function(block) {
    var value_number = Blockly.Verilog.valueToCode(block, 'number', Blockly.Verilog.ORDER_ATOMIC);
    var value_name = Blockly.Verilog.valueToCode(block, 'NAME', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = value_name + '[' + value_number + ']';
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_NONE];
  };

  Blockly.Verilog['concat'] = function(block) {
    var value_arg1 = Blockly.Verilog.valueToCode(block, 'arg1', Blockly.Verilog.ORDER_ATOMIC);
    var value_arg2 = Blockly.Verilog.valueToCode(block, 'arg2', Blockly.Verilog.ORDER_ATOMIC);
    // TODO: Assemble Verilog into code variable.
    var code = '{' + value_arg1 + ' , ' + value_arg2 +'}';
    // TODO: Change ORDER_NONE to the correct strength.
    return [code, Blockly.Verilog.ORDER_ATOMIC];
  };
  