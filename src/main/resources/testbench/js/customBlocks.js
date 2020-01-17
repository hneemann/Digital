Blockly.Blocks['one'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("1");
    this.setOutput(true, "Number");
    this.setColour(230);
 this.setTooltip('Value of 1');
 this.setHelpUrl("https://www.csee.umbc.edu/portal/help/VHDL/verilog/types.html");
  }
};

Blockly.Blocks['zero'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("0");
    this.setOutput(true, "Number");
    this.setColour(230);
 this.setTooltip("Value of 0");
 this.setHelpUrl("https://www.csee.umbc.edu/portal/help/VHDL/verilog/types.html");
  }
};

Blockly.Blocks['high_impedence'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Z");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("High Impedence");
 this.setHelpUrl("https://www.csee.umbc.edu/portal/help/VHDL/verilog/types.html");
  }
};

Blockly.Blocks['dont_care'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("X");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("Don't care");
 this.setHelpUrl("https://www.csee.umbc.edu/portal/help/VHDL/verilog/types.html");
  }
};

Blockly.Blocks['always_blk'] = {
  init: function() {
    this.appendValueInput("condition")
        .setCheck(null)
        .appendField("Always at");
    this.appendStatementInput("body")
        .setCheck(null)
        .appendField("do");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(165);
 this.setTooltip("Always Block to keep looping forever");
 this.setHelpUrl("http://referencedesigner.com/tutorials/verilog/verilog_16.php");
  }
};

Blockly.Blocks['decimal_binary'] = {
  init: function() {
    this.appendValueInput("val")
        .setCheck("Number")
        .appendField("Decimal to binary of ");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(160);
    this.setTooltip('Enter a number is decimal to be converted to Binary');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['decimal_hexa'] = {
  init: function() {
    this.appendValueInput("val")
        .setCheck("Number")
        .appendField("Decimal to Hexadecimal of");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(160);
    this.setTooltip('Enter a number is decimal to be converted to Hexadecimal');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['decimal_octa'] = {
  init: function() {
    this.appendValueInput("val")
        .setCheck("Number")
        .appendField("Decimal to Octal of");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(160);
    this.setTooltip('Enter a number is decimal to be converted to Octal');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['end_module'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("end");
    this.setPreviousStatement(true, null);
    this.setColour(345);
 this.setTooltip('Block must be attached at the end');
 this.setHelpUrl("End");
  }
};

Blockly.Blocks['module_dec'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Create new module with name")
        .appendField(new Blockly.FieldTextInput("ModuleName"), "modName");
    this.appendDummyInput()
        .appendField("Write variables you'll use in the program")
        .appendField(new Blockly.FieldTextInput("varNames"), "varNames");
    this.setInputsInline(false);
    this.setNextStatement(true, null);
    this.setColour(150);
 this.setTooltip('Declare new Verilog module');
 this.setHelpUrl('');
  }
};

Blockly.Blocks['pos_edge'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("Rising edge of");
    this.setInputsInline(true);
    this.setOutput(true, null);
    this.setColour(300);
 this.setTooltip('Rising edge of');
 this.setHelpUrl("https://electronics.stackexchange.com/questions/326662/posedge-in-verilog");
  }
};

Blockly.Blocks['neg_edge'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("Falling edge of");
    this.setInputsInline(true);
    this.setOutput(true, null);
    this.setColour(100);
 this.setTooltip('Falling edge of');
 this.setHelpUrl("https://electronics.stackexchange.com/questions/326662/posedge-in-verilog");
  }
};

Blockly.Blocks['input_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Declare new variable")
        .appendField(new Blockly.FieldTextInput("VarName"), "NAME")
        .appendField("as Input");
    this.appendValueInput("size")
        .setCheck("Number")
        .appendField("This input consists of these number of bits");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(165);
 this.setTooltip('Declare variable as Input');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['output_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Declare new variable")
        .appendField(new Blockly.FieldTextInput("VarName"), "NAME")
        .appendField("as Output");
    this.appendValueInput("size")
        .setCheck("Number")
        .appendField("This output consists of these number of bits");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(200);
 this.setTooltip('Declare variable as Output');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['wire_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Declare new variable")
        .appendField(new Blockly.FieldTextInput("VarName"), "NAME")
        .appendField("as Wire");
    this.appendValueInput("size")
        .setCheck("Number")
        .appendField("This wire could hold up to");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(250);
 this.setTooltip('Declare variable as a wire');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['reg_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Declare new variable")
        .appendField(new Blockly.FieldTextInput("VarName"), "NAME")
        .appendField("as Register");
    this.appendValueInput("size")
        .setCheck("Number")
        .appendField("This Register consists of these number of bits");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(180);
 this.setTooltip('Declare variable as Register');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['inout_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Declare new variable")
        .appendField(new Blockly.FieldTextInput("VarName"), "NAME")
        .appendField("as Input/Output");
    this.appendValueInput("size")
        .setCheck("Number")
        .appendField("This I/O consists of these number of bits");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
 this.setTooltip('Declare variable as both Input and Output');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['and_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("AND-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(290);
    this.setTooltip('Perform an AND between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['nand_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("NAND-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(320);
    this.setTooltip('Perform an NAND between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['nor_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("NOR-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(50);
    this.setTooltip('Perform an NOR between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['xnor_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("XNOR-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(200);
    this.setTooltip('Perform an XNOR between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['or_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("OR-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(100);
    this.setTooltip('Perform an OR between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['xor_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("XOR-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(140);
    this.setTooltip('Perform a XOR between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['nand_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Gate name")
        .appendField(new Blockly.FieldTextInput("gateName"), "gName");
    this.appendDummyInput()
        .appendField("Output name")
        .appendField(new Blockly.FieldTextInput("outName"), "oName");
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("Attach first input");
    this.appendDummyInput()
        .appendField("NAND-ed with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("Attach second input");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(180);
    this.setTooltip('Perform a NAND between argument1 and argument2');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['not_gate'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("output")
        .appendField(new Blockly.FieldTextInput("name"), "out");
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("negate");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(20);
    this.setTooltip('Negate the argument');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['assign_block'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("Assign")
        .appendField(new Blockly.FieldVariable("default"), "var");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(330);
 this.setTooltip('Continous assignment');
 this.setHelpUrl("https://stackoverflow.com/questions/28751979/difference-between-behavioral-and-dataflow-in-verilog");
  }
};

Blockly.Blocks['end_module'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("end");
    this.setPreviousStatement(true, null);
    this.setColour(345);
 this.setTooltip('Block must be attached at the end');
 this.setHelpUrl("End");
  }
};

Blockly.Blocks['module_test'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Create module")
        .appendField(new Blockly.FieldTextInput("ModuleName"), "modName");
    this.setNextStatement(true, null);
    this.setColour(150);
 this.setTooltip('Declare new Verilog testbench module');
 this.setHelpUrl('');
  }
};

Blockly.Blocks['input_simu'] = {
  init: function() {
    this.appendValueInput('size')
        .setCheck('Number')
        .appendField('Declare input')
        .appendField(new Blockly.FieldTextInput('VarName'), 'NAME')
        .appendField('with');
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(320);
 this.setTooltip('Declare input variable in Simulation module');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['output_simu'] = {
  init: function() {
    this.appendValueInput('size')
        .setCheck('Number')
        .appendField('Declare output')
        .appendField(new Blockly.FieldTextInput('VarName'), 'NAME')
        .appendField('with');
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(120);
 this.setTooltip('Declare output variable in Simulation module');
 this.setHelpUrl("http://www.asic-world.com/verilog/syntax3.html");
  }
};

Blockly.Blocks['decimal_binary_return'] = {
  init: function() {
    this.appendValueInput("number")
        .setCheck("Number")
        .appendField("Decimal to Binary");
    this.setInputsInline(false);
    this.setOutput(true, null);
    this.setColour(120);
    this.setTooltip('');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['decimal_hexa_return'] = {
  init: function() {
    this.appendValueInput("number")
        .setCheck("Number")
        .appendField("Decimal to Hexadecimal");
    this.setInputsInline(false);
    this.setOutput(true, null);
    this.setColour(120);
    this.setTooltip('');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['decimal_octal_return'] = {
  init: function() {
    this.appendValueInput("number")
        .setCheck("Number")
        .appendField("Decimal to Octal");
    this.setInputsInline(false);
    this.setOutput(true, null);
    this.setColour(120);
    this.setTooltip('');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['monitor_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Monitor variables ")
        .appendField(new Blockly.FieldTextInput("variables"), "names");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(105);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['intial'] = {
  init: function() {
    this.appendDummyInput("condition")
        .appendField("Execute at t=0");
    this.appendStatementInput("body")
        .setCheck(null)
        .appendField("do");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(165);
 this.setTooltip("Block to start the simulation phase");
 this.setHelpUrl("http://referencedesigner.com/tutorials/verilog/verilog_16.php");
  }
};

Blockly.Blocks['intial_par'] = {
  init: function() {
    this.appendDummyInput("condition")
        .appendField("Execute the following all at t=0");
    this.appendStatementInput("body")
        .setCheck(null)
        .appendField("do");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(300);
 this.setTooltip("Block to start the parallel execution simulation phase");
 this.setHelpUrl("http://referencedesigner.com/tutorials/verilog/verilog_16.php");
  }
};

Blockly.Blocks['display_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Print ")
        .appendField(new Blockly.FieldTextInput("text"), "text");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
    this.setTooltip('Used to display some text');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['forever_loop'] = {
  init: function() {
    this.appendDummyInput("condition")
        .appendField("Execute forever");
    this.appendStatementInput("body")
        .setCheck(null)
        .appendField("do");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(325);
 this.setTooltip("Execute the sequence below forever");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['finish_block'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Finish simulation");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(210);
    this.setTooltip('Finish the simulation by appending this block');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['if_else_block'] = {
  init: function() {
    this.appendValueInput("condition_if")
        .setCheck(null)
        .appendField("if");
    this.appendStatementInput("if_code")
        .setCheck(null)
        .appendField("do");
    this.appendValueInput("condition_else")
        .setCheck(null)
        .appendField("else if");
    this.appendStatementInput("else_code")
        .setCheck(null)
        .appendField("do");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(120);
    this.setTooltip('if-else-if blocks with conditions for each');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['time_block'] = {
  init: function() {
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("at time =");
    this.appendStatementInput("arg2")
        .setCheck(null);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(150);
 this.setTooltip("After few seconds, code inside will be executed");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['always_simu'] = {
  init: function() {
    this.appendValueInput("delay")
        .setCheck("Number")
        .appendField("Always delay");
    this.appendStatementInput("code")
        .setCheck(null);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['variables_set_parallel'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("set")
        .appendField(new Blockly.FieldTextInput("variable"), "var")
        .appendField("to");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(345);
 this.setTooltip("set this variable to be equal the input with parallel execution.");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['bit_select'] = {
  init: function() {
    this.appendValueInput("number")
        .setCheck(null)
        .appendField("Select bit");
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("From variable");
    this.setOutput(true, null);
    this.setColour(160);
    this.setTooltip('Enter a number and a variable getter, this will select input bit number');
    this.setHelpUrl('http://www.example.com/');
  }
};

Blockly.Blocks['concat'] = {
  init: function() {
    this.appendValueInput("arg1")
        .setCheck(null)
        .appendField("");
    this.appendDummyInput()
        .appendField("concatinated with");
    this.appendValueInput("arg2")
        .setCheck(null)
        .appendField("");
    this.setOutput(true, null);
    this.setColour(210);
    this.setTooltip('');
    this.setHelpUrl('http://www.example.com/');
  }
};