# Digital #

Digital ist ein Simulator für digitale Schaltkreise. Er ist für Lehrzwecke entwickelt worden und
wird von mir in meinen Vorlesungen eingesetzt.
Bevor ich mit der Entwicklung von Digital begonnen habe, habe ich
[Logisim](http://www.cburch.com/logisim/) von Carl Burch
eingesetzt. Zu erkennen ist das daran, dass die Farbschemata sich ähneln.

Logisim ist ein ganz hervorragendes Werkzeug welches sich beim Einsatz in der Lehre vielfach 
bewährt hat. Leider hat Carl Burch die Entwicklung von Logisim 2014 eingestellt.
Es gibt eine Reihe von Forks, über welche eine Weiterentwicklung betrieben wird:

- [Logisim-Evolution](https://github.com/reds-heig/logisim-evolution) aus der Schweiz
- [Logisim](https://github.com/lawrancej/logisim) von Joseph Lawrance
- [Logisim-iitd](https://code.google.com/archive/p/logisim-iitd/) aus Indien
- [Logisim](http://www.cs.cornell.edu/courses/cs3410/2015sp/) des CS3410 Kurses der Cornell Universität

Dennoch bin ich der Meinung, das es gute Gründe für eine komplette Neuentwicklung gibt.

## Features ##

Folgende Features zeichnen Digital aus:

- Messwertegraph zur Anzeige von Signalzuständen.
- Gatterschrittmodus zu Analyse von Oszillationen.
- Analyse und Synthese von kombinatorischen Schaltungen und Schaltwerken.
- Einfaches Testen von Schaltungen: Es können Testfälle erstellt und aussgeführt werden.
- Viele Beispiele: Vom Transmission-Gate D-FF bis zum kompletten MIPS-ähnlichem Prozessor.
- Fast-Run-Mode um eine Simulation ohne Aktualisierung des HMI durchzuführen.
  Ein einfacher Prozessor kann mit 100kHz getaktet werden.
- Anzeige von LST-Files bei der Ausführung von Assembler-Programmen.
- Einfache Remote TCP-Schnittstelle um z.B. mit einer Assembler-IDE den Simulator zu steuern.
- SVG-Export von Schaltungen, incl. einer LaTeX-tauglichen SVG-Variante (siehe [ctan](ftp://ftp.fau.de/ctan/info/svg-inkscape/InkscapePDFLaTeX.pdf))
- Keine Altlasten im Code
- hohe Testabdeckung (exclusiv der GUI-Klassen etwa 80%)

## Motivation ##

Im Folgenden möchte ich kurz auf die Punkte eingehen, die mich motiviert haben, eine Neuentwicklung zu 
starten:

### Einschalten ###

Logisim hat Schwierigkeiten mit dem "Einschalten" einer Schaltung. Ein einfaches Master-Slave Flipflop 
lässt sich mit Logisim nicht realisieren, da die Schaltung nicht "eingeschaltet" wird, gibt es keine 
Stabilierungsphase nach dessen Abschluss sich die Schaltung in einem stabilen Zustand befindet. 
Ein Master-Slave Flipflop lässt sich nur mit einem Reset-Eingang simulieren, und dieser muss auch betätigt werden, um die 
Schaltung verwenden zu können.

Um zu verstehen, wie Digital mit diesem Problem umgeht, muss man sich ansehen, wie die Simulation in Digital arbeitet:
Digital verwendet einen Ansatz, welcher an einen Event-Based Simulator erinnert. Jedes mal, wenn ein 
Gatter eine Änderung an einem seiner Eingänge erfährt, werden zwar die Eingänge eingelesen, jedoch 
werden die Ausgänge des Gatters nicht aktualisiert. Erst wenn alle betroffenen Gatter die Änderungen an 
ihren Eingängen eingelesen haben, werden die Ausgänge aller Gatter aktualisiert. Alle Gatter scheinen 
vollkommen synchron umzuschalten. Sie scheinen alle die exakt gleiche Gatterlaufzeit zu haben.
Dieser Ansatz führt jedoch dazu, dass sich schon ein einfaches RS-Flipflop unter Umständen nicht stabilisieren kann.  
Während der Einschaltphase wird daher ein anderer Modus verwendet: Alle Gatter lesen ihre Eingänge ein und 
aktualisieren sofort ihre Ausgänge. Dies geschieht Gatter für Gatter in zufälliger Reihenfolge der Gatter, bis es keine 
Veränderungen mehr gibt und die Schaltung sich stabilisiert hat.
Auf diese Weise stabilisiert sich auch ein Master-Slave Flipflop nach dem "einschalten", jedoch ist der sich einstellende 
Endzustand nicht definiert.
 
Um eine Schaltung in einem definierten Zustand zu Starten gibt es ein spezielles Reset-Gatter. 
Dieses Gatter hat einen einzigen Ausgang welcher während der Stabilisierungsphase auf Low gehalten wird und 
auf High wechselt sobald diese abgeschlossen ist.

Ein Nachteil dieses Vorgehens ist die Tatsache, dass sich in einer laufenden Simulation die Schaltung nicht verändern
läst. Diese muss zuerst "ausgeschaltet" werden, und nach der Modifikation ist sie wieder "einzuschalten". Dieses Vorgehen ist 
jedoch auch bei einer realen Schaltung ratsam. 

### Oszillationen ###

In Logisim ist es schwer möglich eine oszillierende Schaltung zu untersuchen. Erkennt Logisim eine Oszillation,
wird dies zwar angezeigt, es ist aber nicht möglich, die Ursache näher zu untersuchen, bzw. im Detail 
nachzuvollziehen, wie es zu der Oszillation kommt.

Die simultane Aktualisierung aller Gatter, welche eine Änderung an einem Ihrer Eingängen erfahren haben, kann auch 
bei Digital zu einer Oszillation führen. Auch hier wird die Oszillation erkannt, und die Simulation abgebrochen.
Es gibt jedoch einen Einzelgattermodus welcher es erlaubt die Propagation einer Signaländerung Gatter für Gatter 
auf dem Weg durch die Schaltung zu verfolgen. Dabei wird nach jedem Schritt angezeigt, welche Gatter der Schaltung 
eine Veränderung an einem ihrer Eingänge erfahren haben.
Auf diese Weise wird visualisiert, wie sich eine Signaländerung "im Kreis bewegt" und so zu der Oszillation führt.  

### Eingebettete Schaltkreise ###

Wie bei Logisim können auch mit Digital Schaltkreise in neue Schaltungen eingebettet werden. Auf diese Weise lassen sich 
hierachische Schaltungen aufbauen. Jedoch werden in Digital Schaltungen, die eingebunden werden tatsächlich so oft 
in die Schaltung aufgenommen, wie sie importiert werden. Das ist vergleichbar mit einem C-Programm bei welchem alle 
Funktiosaufrufe per inline eingebunden werden. Es verhällt sich wie bei einer echten Schaltung: Jeder Schaltkreis ist tatsächlich 
so oft vorhanden, wie er in der Schaltung verwendet wurde. Dieses Vorgehen vergrößert zwar die Datenstrukturen für die Simulation,
vereinfacht diese aber zugleich sehr deutlich. So sind z.B. die Ein- und Ausgänge einer eingebetteten Schaltung nicht speziell
zu behandeln, sie existieren nach der Modelbildung schlicht nicht mehr. Auch bidirektionelle Anschlüsse stellen somit kein Problem dar.
Aus diesem Vorgehen folgt auch, dass sich in Digital z.B. ein UND-Gatter, welches als eigener Schaltkreis eingebettet wurde, genau so 
verhällt, als wäre es auf oberster Ebene eingefügt worden. Auf Simulationsebene gibt es tatsächlich keinen Unterschied zwischen 
diesen beiden Varianten.
Logisim arbeitet hier etwas anders, was gelegentlich zu Überraschungen führt, z.B. durch unerwartete Signallaufzeiten.

### Performance ###

Werden komplette Prozessoren simuliert, ist es möglich die Simulation zu berechnen ohne die grafische Anzeige zu aktualisieren.
Ein einfacher Prozessor (siehe Beispiel) lässt sich so mit etwa 100kHz takten (Intel® Core™ i5-3230M CPU @ 2.60GHz) was auch für
komplexere Übungen ausreichend ist.
Es gibt ein Break-Gatter welches einen einzelnen Eingang hat. Wechselt dieser Eingang von low auf high wird dieser 
schnelle Lauf beendet. Auf diese Weise lässt sich eine Assembler-Anweisung BRK implementieren, womit sich dann Break-Points 
in Assembler-Programme einfügen lassen. 

### Debugging ###

In Logisim gibt es keine geeignete Möglichkeit um ein Assembler-Programm in einem simulierten Prozessor zu debuggen.
Steht ein Assembler zur Verfügung welcher ein List-File des Source-Codes erzeugt (Code-Adresse gefolgt vom Source-Code)
kann Digital dieses Listing in einem Trace-Fenster anzeigen, wobei der aktuelle Befehl hervorgehoben ist. 
So lässt sich im Simulator ein Assembler-Programm im Einzelschrittmodus ausführen und debuggen. 
Da Digital über eine einfache, TCP basierte Schnittstelle zur Steuerung verfügt, kann auch eine Assembler-IDE den Simulator 
steuern, und auf diese Weise Anwendungen in den simulierten Prozessor laden, den Prozessor starten und beenden, Einzelschritte 
ausführen usw..     

### Schaltungssynthese ###

Logisim kann kombinatorische Schaltungen aus einer Wahrheitstabelle erzeugen. In Digital ist dies ebenfalls möglich. 
Zudem lassen sich mit Digital auch Automaten aus einer geeigneten Zustandsübergangstabelle erzeugen. Dabei kann sowohl 
das Übergangsschaltnetz als auch ein Ausgangsschaltnetz erzeugt werden. Die  Minimierung der Ausdrücke erfolgt dabei 
nach dem Verfahren von Quine und McCluskey. Ebenso lässt sich aus einer Schaltung, die D-Flipflops oder JK-Flipflops 
enthält die Zustandsübergangstabelle ermitteln. Zu beachten ist jedoch, dass dies nur mit den eingebauten Flipflops funtioniert.
Ein FlipFlop welches z.B. aus NOr Gattern aufgebaut wurde, wird nicht als solches erkannt. 
Die Analyse sequentieller Schaltungen funktioniert nur, wenn rein kombinatorische 
Schaltungen mit den eingebauten D- oder JK-Flopflops kombiniert werden.     
