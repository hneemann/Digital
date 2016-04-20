# Digital #

Digital is a simple simulator for digital circuits. You can place the circuit in a simple panel and connect the elements
by wires. If your circuit is complete you can turn it on and interact with it.

There are two modes of how to run the circuit:

* At first there is the normal or full step mode started by clicking ![Alt text](https://bitbucket.org/hneemann/digital/raw/master/src/main/resources/run.gif). 
  In most cases this mode is suitable. If you modify the running circuit by
  turning on an input or entering an input value, this modification is propagated along all elements in the circuit.
  What you end up with is the state of the circuit after all changes are calculated and this state is shown in the main panel.
  Now you can do an other modification and the calculation starts again.
  In short: All calculations are done until the circuit became stable again. But sometimes this will never happen: Sometimes 
  the circuit starts to oscillate. In this case an error message is shown. To find out whats going on in this case you can use 
  the single gate mode.
* The single gate mode is started by clicking ![Alt text](https://bitbucket.org/hneemann/digital/raw/master/src/main/resources/micro.gif).
  In the single gate mode the elements are highlighted which had a change at on of their inputs. But the reaction on this change 
  is not calculated, the outputs are not updated. To calculate the reaction on the modification you have to click the single step button ![Alt text](https://bitbucket.org/hneemann/digital/raw/master/src/main/resources/step.gif). 
  Clicking this button the outputs of all highlighted elements are updated. Now maybe some other gates have changes on their inputs. 
  But again the outputs are not updated until the step button ![Alt text](https://bitbucket.org/hneemann/digital/raw/master/src/main/resources/step.gif)
  is clicked. So you can follow the propagation of an input modification gate by gate.
  
In the examples folder you will find a lot of examples of different combinatorial and sequential circuits. Play around 
with them to become familiar with how Digital works.  
  
### How do I get set up? ###

* maven is used as a build system
* So you can simply run `mvn install` to build Digital 
* With `mvn site` you get a checkstyle, a findbugs and a cobertura code coverage report

### Contribution guidelines ###

* If you want to contribute send me just a pull request
* Don't introduce new checkstyle errors
* Don't introduce new findbugs errors
* Try to keep the test coverage high. The target is 80% test coverage at all non GUI components.
* Until now there are no GUI tests. Try to keep the amount of untested GUI code low. 
