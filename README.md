[![Build Status](https://travis-ci.org/hneemann/Digital.svg?branch=master)](https://travis-ci.org/hneemann/Digital)

[Download latest Release](https://github.com/hneemann/Digital/releases/latest)

# Digital #

Digital is a simulator for digital circuits. It is designed for educational purposes and I use it in my lectures.
Prior to the development of Digital, I used [Logisim](http://www.cburch.com/logisim/), developed by Carl Burch.
If you are familiar with Logisim you will recognize the color scheme.

Logisim is an excellent and proven tool for teaching purposes. Unfortunately, Carl Burch discontinued the development of 
Logisim in 2014.
But he has made Logisim open source so there are a number of forks to continue the work of Carl Burch:

- [Logisim-Evolution](https://github.com/reds-heig/logisim-evolution) by people of a group of swiss institutes (Haute École Spécialisée Bernoise, Haute École du paysage, d'ingénierie et d'architecture de Genève, and Haute École d'Ingénierie et de Gestion du Canton de Vaud)
- [Logisim](https://github.com/lawrancej/logisim) by Joseph Lawrance
- [Logisim-iitd](https://code.google.com/archive/p/logisim-iitd/) from the Indian Institute of Technology Delhi
- [Logisim](http://www.cs.cornell.edu/courses/cs3410/2015sp/) from the CS3410 course of the Cornell University

Nevertheless, I believe that there are good reasons for a completely new development from scratch.

## Features ##

These are the main features of Digital:

- Visualization of signal states with measurement graphs
- Single gate mode to analyze oscillations.
- Analysis and synthesis of combinatorial and sequential circuits.
- Simple testing of circuits: You can create test cases and execute them to verify your design.
- Many examples: From a transmission gate D-flip-flop to a complete (simple) MIPS-like processor.
- Fast-run mode to perform a simulation without updating the GUI.
  A simple processor can be clocked at 100kHz.
- Display of LST files when executing assembler programs within such a processor.
- Simple remote TCP interface which  e.g. enables an assembler IDE to control the simulator.
- SVG export of circuits, including a LaTeX-compatible SVG version (see [ctan](https://www.ctan.org/tex-archive/info/svg-inkscape))
- No legacy code
- Good test coverage (exclusive of GUI classes about 80%)

## Motivation ##

Below I would like to explain briefly the reasons which led me to start a new development:

### Switch On ###

Logisim has difficulties with the "switching on" of a circuit. A simple master-slave flip-flop
can not be realized with Logisim, since the circuit is not switched on, there is no
settling time to bring the circuit to a stable condition after its completion.
A master-slave flip-flop can only be implemented with a reset input. This
reset input needs to be activated to make the circuit operational.

To understand how Digital deals with this issue, you have to look at how the simulation works in Digital:
Digital uses an event based simulator approach, i.e. each time a 
gate undergoes a change at one of its inputs, the new input states are read, however, 
the outputs of the gate are not updated instantly. Only when all gates involved have read their inputs, 
the outputs of all gates are updated. All gates seem to change synchronously, i.e.
they seem to have all the exact same gate delay time.
However, an undesirable feature of this approach is that even a simple RS flip-flop might not be able to 
reach a stable state.
For that reason, another mode is used during settling time: Each time a
gate undergoes a change at one of its inputs all gate inputs are read and their outputs are updated immediately.
This happens gatewise in random order until no further changes occur and the circuit reaches a stable state.
The gates appear to have random delay times now.
This way, a master-slave flip-flop reaches a stable state after "switch on", however, the final state is still undefined.

To start a circuit in a defined state a special reset gate is used.
This gate has a single output which is low during settling time and goes 
high when settling time is over.

A disadvantage of this approach is the fact that a running simulation cannot be changed.
In  order to do so, the circuit needs be switched off, modified and switched on again.
However, this procedure is also advisable for real circuits.

### Oscillations ###

With Logisim it is hard to find the root cause for oscillating circuits. If Logisim detects an oscillation,
a corresponding message is issued, but it is not possible to investigate the cause in more detail, so it is difficult to
understand what happens.

The synchronous update of all gates, which have seen a change at one of their inputs may also cause
oscillations in Digital. In such a case, the oscillation is detected and simulation stops.
However, there is also a single gate mode which allows to propagate a signal change gate by gate. This feature allows to
follow the way through the circuit. After each step, it all gates with a change at one
of its inputs is highlighted.
This way you can see how a signal change propagates in a circuit, thus you are able to find the root cause of an oscillation.

### Embedded circuits ###

Similar to Logisim, Digital also allows to embed previously saved circuits in new designs, so hierarchical
circuits can be created. However, in Digital embedded circuits are included as often as 
the circuit is used. This is similar to a C program in which all 
function calls are compiled as inlined functions. This is similar to a real circuit: Each circuit is "physically present" 
as often as it is used in the circuit. Although this approach increases the size of the data structure, 
it simplifies the simulation itself.
Thus, for example, the inputs and outputs of an embedded circuit not specifically treat, they simply don't exist anymore
after the formation of the simulations model. Even bidirectional connections can be implemented.
Because of that approach for instance a separately embedded AND gate behaves exactly like an AND gate inserted at top
level although there is actually no difference between these two variants from the simulation perspective.

Logisim works somewhat different, which sometimes leads to surprises like unexpected signal propagation times.

### Performance ###

If a complete processor is simulated, it is possible to calculate the simulation without an update of the 
graphical representation.
A simple processor (see example) can be simulated with a 100kHz clock (Intel® Core ™ i5-3230M CPU @ 2.60GHz),
which is suitable also for more complex exercises like Conway's Game of Live.
There is a break gate having a single input. If this input changes from low to high this quick run is stopped.
This way, an assembler instruction BRK can be implemented, which then can be used to insert break points
in assembly language programs. So the debugging of assembly programs becomes very simple.

### Debugging ###

In Logisim there is no way to debug an assembly program in a simulated processor.
If an assembler is available which creates a LST file of the source code (code address followed by the source code line)
Digital can view this listing in a trace window with the current instruction being highlighted.
So the simulator can run an assembly program in a debug friendly single step mode.
Since Digital has a simple TCP-based remote control interface, also an assembler IDE can be used to control the simulator
and load assembly programs in the simulated processor, start the program, perform single steps and so on.

### Circuit Synthesis ###

Logisim can generate combinatorial circuits from a truth table. In Digital, this is also possible.
In addition, also a sequential circuit can be generated from an appropriate state transition table.
You can specify both the transition circuit and the output circuit. The minimization of the expressions is done
by the method of Quine and McCluskey.
Also the truth table, can be derived from a circuit which contains simple combinatorial logic,
D flip-flops or JK flip-flops, including the generation of the state transition table.
Note, however, that a NOR-Gate-flip-flop is not recognized as such.
The analysis of sequential circuits only works with purely combinatorial
circuits combined with the built-in D or JK flop-flops.

## How do I get set up? ##

* maven is used as a build system
* So you can simply run `mvn install` to build Digital
* With `mvn site` you can create a checkstyle, a findbugs and a cobertura code coverage report

## Contribution guidelines ##

* If you want to contribute send me just a pull request
* Don't introduce new checkstyle issues
* Don't introduce new findbugs issues
* Try to keep the test coverage high. The target is 80% test coverage at all non GUI components.
* Up to now there are no GUI tests so the overall test coverage is only somewhat above 50%.
  Try to keep the amount of untested GUI code low.
