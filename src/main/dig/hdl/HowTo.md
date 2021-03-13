# How to create a Board Integration File #

## The Elements of the `.config` Files

### The Commands to Execute

The `.config` File describes two things. At first, it defines which external
executables are needed to be started to run a circuit on a specific board.
And second it creates the files which are required to run a circuit on a specific
board.

Here you can see how external tools are started:
```xml
    <commands>
        <command name="Export VHDL &amp; Start Vivado" requires="vhdl" timeout="0" filter="true">
            <arg>vivado</arg>
            <arg>vivado/{?=shortname?}.xpr</arg>
        </command>
        <command name="Export VHDL" requires="vhdl" />
    </commands>
```
The `commands` tag contains the different commands which are available. Every command appears
in the main menu as a menu item. If this menu item is clicked, the command is started.
The first command "Export VHDL & Start Vivado" has some arguments which are used to start a program.
The first argument is the executable which needs to be started and the further arguments are passed
to this executable. In this case the programm `vivado` is started. The attribute `requires` defines 
which hardware description language (hdl) is necessary to start the program.
Up to now only `vhdl` and `verilog` is supported.
The `timeout` attribute defines how long digital should wait before a timeout exception is thrown
which also terminates the external program.
Because Vivado is a GUI application, which can run for a long time, this value is set to zero
which means no timout check at all.
The `filter` attribute allows to filter the arguments which means the given string is not passed
to the external program as it is, instead it is preprocessed to allow more flexible arguments which
are depending on the name of the circuit and so on. If filtering is enabled, the arguments are scanned 
for either `<?...?>` or `{?...?}` fragments. If such a fragment is found the enclosed text is
evaluated by a code parser. If the code starts with a `=` the following expression is evaluated 
and inserted. The variables that are available are:

- `shortname`: The file name of the current circuit without the suffix.
- `name`: The file name of the current circuit including the suffix.         
- `path`: The full path of the current circuit. Including the root folder.         
- `dir`: The directory of the current circuit.
- `extension`: The file extension of the selected hdl. Either `.v` or `.vhdl`.
- `model`: A struct that gives access to details of the current circuits hdl model.
  See the [Model chapter](#the-hdl-model) for more information.
- `clockGenerator`: The short name of the hdl file which contains the 
  clock integration which is used by this board. 
  See the [Clock Integration chapter](#clock-integration) for more information.

See the [HGS chapter](#the-hgs-language) for more information about the possibilities 
the HGS script offers.          

The second command "Export VHDL" has no arguments given, which means no external program is 
executed. But it has a `requires` tag, which means that in this case a vhdl file
is created only. 

### The Files to Create

In most cases it is not sufficient to create the hdl file. There are other files
required in order to process the generated hdl code. In many cases a constraints file 
is required which contains the pin information required by the syntheses tool in order
to generate the appropriate bit stream. Or a simple project file may be handy to
make it easier to open the hdl file in a tool like Vivado or ISE.      

Such files are created by the `files` section of the `.config` file.

```xml
  <files>
    <file name="Pins_{?=shortname?}.pin" overwrite="false" filter="false" id="vivado">
      <content>
         The content of the file created.
      </content>  
    </file>  
  </files>
```

The `file`-tag has a `name` attribute which defines the file name. The `overwrite` attribute 
defines the behaviour if the file already exists. If the value is set to `true` the file is 
overwritten. If the value is set to `false` a existing file is not changed. This is handy for
a project file template, which is modified after its creation by a tool like Vivado or ISE 
and you don't want to overwrite this modifications every time the tool is started by Digital.
The `filter` attribute enables the filtering of the files content. And finally the `id` attribute 
is used to include this file file from other `.config` files.
The `content` tag defines the content of the file created.
In most cases the content of the file depends on the circuit. In this case a the `filter` 
attribute needs to be set to `true`. After that you are able to use the `<?...?>` or `{?...?}` 
fragments to generate the file in a flexible way.
As an example we want to create a file named `Pins_[name of circuit].pin` which contains all
the pins used by the circuit:         
```xml
  <files>
    <file name="Pins_{?=shortname?}.pin" overwrite="true" filter="true">
      <content><![CDATA[
The circuit contains the following pins:

{?

  for (i:=0; i < sizeOf(model.ports); i++) {
      println("Port " + model.ports[i].name + " uses pin " + model.ports[i].pin);
  }

 ?}]]></content>
    </file>
  </files>
```
The code enclosed by the `{?...?}` characters is executed and prints the list of all 
used pins to the text file. The `<![CDATA[ ]]>` statement ensures that the content 
is considered as character data which is not analysed by the XML parser. If this statement 
is not used the `i < sizeOf(...` code causes an XML parser error. 

## The HDL Model

The model struct has up to now only two fields: 

- `frequency` gives the frequency which is selected in the clock element of the circuit.
  Up to now only one clock element is supported.
- `ports` gives you an array containing all the input and output ports of the circuit.

The items of the `ports`-array are structs containing detailed information describing the port:

- `dir`: The direction of the port. Either `IN` or `OUT`. 
- `name`: The name of the port, which is visible in the circuit. 
- `bits`: The number of bits used by this port.
- `pin`:  The pin name which is assigned to this port in the circuit by the `pin` attribute of 
  the input or output component.
- `clock`: A boolean which is `true`, if this port is the clock input. 

If you want to print a list containing all the port names and pins, this can be done like this:
```
  for (i:=0; i < sizeOf(model.ports); i++) {
      println("Port " + model.ports[i].name + " uses pin " + model.ports[i].pin);
  }
```

## Clock Integration

There are two methods to make the hdl model run with the frequency given in the circuits
clock component.
The first one is to define the clock frequency used by the board. This is done by the 
`frequency` attribute in the xml root tag:
```xml
<toolchain name="TinyFPGA BX" frequency="16000000">
  ...
</toolchain>  
```  
In this case a simple counter is inserted into the hdl code which divides the clock
signal provided by the board to match the clock frequency given in the circuit.
Although this is very easy, it is also problematic because the clock is generated 
by flipflops, not by the pll which is usually available on the fpga.  

To utilize the pll available on the fpga, a specific piece of hdl code needs to be 
generated. This is done by defining a clock generator instead of simply define the 
frequency:

```xml
<toolchain name="BASYS3" clockGenerator="clockGenerator">
  ...
</toolchain>  
```
If the `clockGenerator` attribute is given, the circuits hdl code is generated 
in a way that either `clockGenerator.v` or `clockGenerator.vhdl` is used to
generate the clock. The hdl code generator assumes that the clock generator 
has a input port `cin` and a output port `cout`.
But although the corresponding module/entity is called, the corresponding code
is not created. The `.config` file must create the appropriate code to 
parameterize the PLL correctly. See the `BASYS3.config` in the `examples/hdl` 
folder for a more complex example.       
 
## The HGS language

### General

The HGS language (HDL Generator Scripting language) is used as a template 
engine to create files dynamically. There are two main usages: At first 
this language is used to define the VHDL and Verilog templates which are 
necessary to export a circuit to VHDL or Verilog.

Second the HGS language is used to generate the board specific files which are
necessary to run a circuit on a specific board. HGS is a simple, dynamic, 
interpreted language designed for file templating.

To avoid a couple of common mistakes by using a dynamic language it uses 
a uncommon approach to deal with variables. To declare a new variable
you have to use the `:=` operator. This is only possible if the new variables 
does not exist. Once the new variable is created an assignment to the variable 
is done by the `=` operator. This is only possible if the variable already 
exists and the types are matching. So this piece of code runs without errors:
```
  a:=0.1;
  a=5.0;
```
But this code fails with an error message:
```
  a:=0.1;
  a=true;
```
The variable `a` is declared as a float. It is not possible to assign a 
bool to it.

### File Creation
 
A template always begins with plain text which is copied to the file to generate.
Special actions - data evaluations or control structures - are delimited 
by `{?` and `?}` or `<?` and `?>`. Because the `.config` files are XML files the
`{?` and `?}` variant is easier to use in most cases.

If you want to create a file which contains the clock period in ns you can use the 
following template:

```xml
  <file name="clock.txt" overwrite="true" filter="true">
      <content>clock period is {? period:=1000000000/model.frequency; print(period);?}ns</content>
  </file>
```
or in a shorter way:
```xml
  <file name="clock.txt" overwrite="true" filter="true">
      <content>clock period is {?=1000000000/model.frequency?}ns</content>
  </file>
```

### Control Structures

All the most common control structures are available:

The `for` statement:
```
  for (i:=0; i<10; i++) {
      println(i);
  }
```

The `if` statement:

```
  if (a<0) {
      println(a);
  } else {
      println(-a);
  }
```

The `while` statement:

```
  while (a<10) {
      a++;
  }
```

The `repeat` statement:

```
  repeat {
      a++;
  } until a>=10;
```

It is possible to declare a function:

```
  func fibu(n){
      if (n<2)
          return n;
      else
          return fibu(n-1)+fibu(n-2);
  }
  
  print(fibu(10));
```
Closures are also supported:

```
func create() {
   inner:=0;
   return func(){
      inner++;
      return inner;
   };
}

a:=create();
b:=create();

print(a()+","+a()+","+b());
```
The output is "1,2,1".

### Built-In Functions

The following functions are predeclared:

`bitsNeededFor(int)` returns the number of bits needed to store the given integer value.

`ceil(float)` returns the smallest integer number greater than the given float.

`floor(float)` returns the largest integer number smaller than the given float.

`round(float)` returns the integer number closest to the given float.

`float(arg)` converts the given number to a float.
 
`min(a,b,...)` returns the min value of all given arguments.

`max(a,b,...)` returns the max value of all given arguments.

`abs(num)` if the number is positive, the number is returned. otherwise -num is returned.

`print(arg)` prints the given argument

`println(arg)` prints the given argument followed by a new line.

`printf(str,arg,...)` the well known C-style printf function.

`format(str,arg,...)` similar to `printf`, but the result is returned as a string.
 
`log(arg)` prints the given argument to the log file.

`isPresent(arg)` returns true if the given arguments exists. 

`panic(msg)` creates a runtime panic. Throws a java exception with the given message. 

`splitString(string)` splits the given string in substrings. 
   Returns an array of strings. The separator chars are " \n\r\t,:;"
    
`sizeOf(var)` returns the size of an array or a map.

`{[key1]:[value1],[key2]:[value2]}` creates a new map.

`[1,2]` creates a new list.          