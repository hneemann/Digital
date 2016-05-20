# Digital #

Digital ist ein Simulator für digitale Schaltkreise. Er ist für Lehrzwecke entwickelt worden und
wird von mir in meinen Vorlesungen eingesetzt.
Bevor ich mit der Entwicklung von Digital begonnen habe, habe ich [Logisim](www.cburch.com/logisim/) von Carl Burch 
eingesetzt. Daher rührt die Ähnlichkeit des verwendeten Farbschemas.

Logisim ist ein ganz hervorragendes Werkzeug welches sich beim Einsatz in der Lehre vielfach 
bewährt hat. Dennoch hat es meiner Meinung nach einige Schwächen, die eine Neuentwicklung rechtfertigen.

## Motivation ##

Im Folgenden möchte ich kurz auf die Punkte eingehen, die mich motiviert haben, eine Neuentwicklung zu 
starten:

### Einschalten ###

Logisim hat Schwierigkeiten mit dem "Einschalten" einer Schaltung. Ein einfaches Master-Slave Flipflop 
lässt sich mit Logisim nicht realisieren, da die Schaltung nicht "eingeschaltet" wird, gibt es keine 
Stabilierungsphase nach dessen Abschluss sich die Schaltung in einem stabilen Zustand befindet. 
Ein MS-FF lässt sich nur mit einem Reset-Eingang simulieren, und dieser muss auch betätigt werden, um die 
Schaltung verwenden zu können.

Digital verwendet einen Ansatz, welcher an einen Event-Based Simulator erinnert: Jedes mal, wenn ein 
Gatter eine Änderung an einem seiner Eingänge erfährt, werden zwar die Eingänge eingelesen, jedoch 
werden die Ausgänge des Gatters nicht aktualisiert. Erst wenn alle betroffenen Gatter die Änderungen an 
ihren Eingängen eingelesen haben, werden die Ausgänge aller Gatter aktualisiert. Alle Gatter scheienen 
vollkommen synchron umzuschalten.
Während der Einschaltphase, wird jedoch ein anderer Modus verwendet: Alle Gatter lesen ihre Eingänge ein und 
aktualisieren sofort ihre Ausgänge, dies geschieht Gatter für Gatter in zufälliger Reihenfolge der Gatter, bis es keine 
Veränderungen mehr gibt und die Schaltung sich stabilisiert hat.
Auf diese Weise stabiliert sich auch ein Master-Slave Flipflop ohne Probleme, jedoch ist der sich einstellende Zustand 
nicht definert.
 
Um eine Schaltung in einem definierten Zustand zu starten gibt es ein Reset-Gatter. 
Dieses Gatter hat einen einzigen Ausgang welcher während der Stabilisierungsphase auf Low gehalten wird und 
anschließend auf High wechselt.

### Oszillationen ###

In Logisim ist es schwer möglich eine oszillierende Schaltung zu untersuchen. Erkennt Logisim eine Oszillation,
wird dies zwar angezeigt, es ist aber nicht möglich, die Ursache näher zu untersuchen, bzw. im Detail 
nachzuvollziehen, wie es zu der Oszillation kommt.

Die simmultane Aktualisierung aller Gatter, welche eine Änderung an einem Ihrer Eingängen erfahren haben, kann auch 
bei Digital zu einer Oszillation führen. Auch hier wird die Oszillation erkannt, und die Simulation abgebrochen.
Es gibt jedoch einen "Einzelgattermodus" welcher es erlaubt die Propagation einer Signaländerung Gatter für Gatter 
auf dem Weg durch die Schaltung zu verfolgen. 
Auf diese Weise wird visualisiert, wie sich eine Signaländerung "im Kreis bewegt" und so zu der Oszillation führt.  

### Performance ###

Werden komplette Prozessoren simuliert, ist es möglich die Simulation zu berechnen ohne die grafische Anzeige zu aktualisieren.
Ein einfacher Prozessor (siehe Beispiel) lässt sich so mit 3MHz takten (Intel® Core™ i5-3230M CPU @ 2.60GHz) was auch für
komplexere Übungen ausreichend ist.
Es gibt ein Break-Gatter welches einen einzelnen Eingang hat. Wechselt dieser Eingang von low auf high wird dieser 
schnelle Lauf beendet. Auf diese Weise lässt sich eine Assembler-Anweisung BRK implementieren, womit sich dann Break-Points 
in Assembler-Programme einfügen lassen.

### Debugging ###

In Logisim gibt es keine geeignete Möglichkeit um ein Assembler-Programm in einem simulierten Prozessor zu debuggen.
Steht ein Assembler zur Verfügung welcher ein List-File des Source-Codes erzeugt (Code-Adresse gefolgt vom Source-Code)
kann Digital dieses Listing in einem Trace-Fenster anzeigen, wobei der aktuelle Befehl hervorgehoben ist. 
So lässt sich im Simulator ein Assembler-Programm im Einzelschrittmodus ausführen.

### Schaltungssynthese ###

Logisim kann Schaltungen aus einer Wahrheitstabelle erzeugen. In Digital ist dies auch möglich. Zudem lassen sich
mit Digital auch Automaten aus einer geeigneten Zustandsübergangstabelle erzeugen. Dabei kann sowohl das 
Übergangsschaltnetz als auch ein Ausgangsschaltnetz erzeugt werden. Die  Minimierung der Ausdrücke erfolgt dabei 
nach dem Verfahren von Quine und McCluskey. 

## Development ##

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
