[![Build Status](https://travis-ci.org/hneemann/Digital.svg?branch=master)](https://travis-ci.org/hneemann/Digital)

[Download latest Release](https://github.com/hneemann/Digital/releases/latest)

# Digital #

Digital is a simulator for digital circuits. It is designed for educational purposes and
is used by me in my lectures.
Before I started the development of Digital, I have
used [Logisim](http://www.cburch.com/logisim/) developed by Carl Burch. 
If you are familiar with Logisim you will recognize the color scheme.

Logisim is a excellent tool which has proven itself as well suited for the use in teaching. 
Unfortunately, Carl Burch has discontinued the development of Logisim in 2014.
There are a number of forks, which are created to continue the work of Carl Burch:

- [Logisim-Evolution](https://github.com/reds-heig/logisim-evolution) by people of a group of swiss institutes (Haute École Spécialisée Bernoise, Haute École du paysage, d'ingénierie et d'architecture de Genève, and Haute École d'Ingénierie et de Gestion du Canton de Vaud)
- [Logisim](https://github.com/lawrancej/logisim) by Joseph Lawrance
- [Logisim-iitd](https://code.google.com/archive/p/logisim-iitd/) from the Indian Institute of Technology Delhi
- [Logisim](http://www.cs.cornell.edu/courses/cs3410/2015sp/) from the CS3410 course of the Cornell University

Nevertheless, I believe that there are good reasons for a complete new development from scratch.

## Features ##

This are the main features of Digital:

- Measurement graph to visualize signal states.
- Single gate mode to analyse oscillations.
- Analysis and synthesis of combinatorial and sequential circuits.
- You can test your design by creating test cases and execute them. 
- Many examples: From a transmission gate D-flipflop to a complete (simple) MIPS-like processor.
- Fast-run mode to perform a simulation without updating the HMI.
  A simple processor can be clocked at 3MHz.
- Displaying a LST file when executing assembler programs within such a processor.
- Simple remote TCP interface to allow e.g. an assembler IDE to control the simulator.
- SVG export of circuits, including a LaTeX-compatible SVG version (see [ctan](https://www.ctan.org/tex-archive/info/svg-inkscape))
- No legacy code
- Good test coverage (exclusive of GUI classes about 80%)

## Motivation ##

Below I would like to explain briefly the points that have motivated me to start a new development instead of using
one of the available Logisim forks:

### Switch On ###

Logisim has difficulties with the "switching on" of a circuit. A simple master-slave flip-flop
can not be realized with Logisim, since the circuit is not switched on, there is no
phase of stabilisation which brings the circuit to a stable condition after its completion.
A master-slave flip-flop can only be implemented with a reset input. And you have to activate this 
reset input to make the circuit operational.

To understand how Digital deals with this issue, you have to look at how the simulation works in Digital:
Digital uses an approach, which is similar to an event based simulator. Each time a
gate undergoes a change at one of its inputs, the new input states are read, however,
the outputs of the gate will not be updated. Only when all the affected gates have read their inputs, 
the outputs of all gates are updated. All gates seem to change synchronous. 
They seem to have all the exact same gate delay time.
This approach, however, means that even a simple RS flip-flop might not be able to stabilize.
During the stabilisation phase another mode therefore is used: Each time a
gate undergoes a change at one of its inputs all gate inputs are read and their outputs are updated
immediately. This happens gate to gate in a random order until there are no further changes and the 
circuit has stabilized. It behaves as if the gates had random delay times.  
In this way, a master-slave flip-flop stabilises after the "switch on", but it also means that the final 
state is undefined.
 
To start a circuit in a defined state there is a special reset gate.
This gate has a single output which is held low during the stabilization phase and goes to 
high when the stabilization phase has finished.

A disadvantage of this approach is the fact that you can not change a running simulation. The circuit must 
first be switched off, and after the modification, you have to switch it on again. This procedure is
however advisable even in a real circuit.

### Oscillations ###

In Logisim it is difficult to investigate an oscillating circuit. If Logisim detects an oscillation,
this will be displayed, but it is not possible to investigate the cause in more detail, so its hard to
understand what happens.

The simultaneous update of all gates, which have seen a change to one of their inputs, can also cause
a oscillation in Digital. Here again, the oscillation is detected and the simulation is stopped.
However, there is a single gate mode which allows to propagate a signal change gate by gate. So you can
follow the way through the circuit. After each step, it is shown which gates have seen a change at one 
of its inputs.
In this way you can see how a signal change moves around in a circle and thus leads to the oscillation.

### Embedded circuits ###

As with Logisim also with Digital circuits can be embedded in new circuits. In this way,
you can build hierarchical circuits. However, in digital circuits that are embedded are in fact included as often
the circuit is used. This is similar to a C program in which all
function calls are compiled like inlined functions. It behaves like a real circuit: Each circuit is actually
present as often, as it is used in the circuit. Although this approach increases the size of the data structure for the simulation,
but at the same time it simplifies the simulation itself. Thus, for example, the inputs and outputs of an 
embedded circuit not specifically treat, they simply don't exist anymore after the formation of the simulations model. 
Even bidirectional connections are no problem in this way.
That approach also causes that for instance an AND gate which has been embedded as a separate circuit, exactly
behaves like an AND gate which is inserted at the top level. 
From the simulations perspective, there is actually no difference between these two variants.
Logisim works somewhat different, which sometimes leads to surprises like unexpected signal propagation times.
 
### Performance ###

If a complete processors is simulated, it is possible to calculate the simulation without an update of the 
graphical representation.
A simple processor (see example) can so simulated with a roughly 3MHz clock (Intel® Core ™ i5-3230M CPU @ 2.60GHz) 
which is suitable also for more complex exercises.
There is a break gate having a single input. If this input changes from low to high this quick run is stopped. 
In this way, an assembler instruction BRK can be implemented, which then can be used to insert break points
to assembly language programs. So the debugging of the assembly programs became very simple.

### Debugging ###

In Logisim there is no suitable way to debug an assembly program in a simulated processor.
If an assembler is available which creates a LST file of the source code (code address followed by the source code line)
Digital can view this listing in a trace window, where the current instruction is highlighted.
So the simulator can run an assembler program in a debug friendly single step mode.
Since Digital has a simple TCP-based remote control interface, also an assembler IDE can control the simulator, 
and load the different assembly programs in the simulated processor, start the program, perform single steps 
and so on.

### Circuit Synthesis ###

Logisim can generate combinatorial circuits from a truth table. In Digital, this is also possible.
In addition, also a sequential circuit can be generated from an appropriate state transition table. 
You can specify both the transition circuit and the output circuit. The minimization of the expressions is done
by the method of Quine and McCluskey. 
Also the truth table, can be derived from a circuit which contains simple combinatorial logic, 
D flip-flops or JK flip-flops, including the generation of the state transition table. 
Note, however, that a flip-flop which is constructed of Nor gates is not recognized as such.
The analysis of sequential circuits works only with purely combinatorial
circuits combined with the built-in D or JK flop flops.

## How do I get set up? ##

* maven is used as a build system
* So you can simply run `mvn install` to build Digital 
* With `mvn site` you can create a checkstyle, a findbugs and a cobertura code coverage report

## Contribution guidelines ##

* If you want to contribute send me just a pull request
* Don't introduce new checkstyle issues
* Don't introduce new findbugs issues
* Try to keep the test coverage high. The target is 80% test coverage at all non GUI components.
* Until now there are no GUI tests. Try to keep the amount of untested GUI code low. 
