[![Build Status](https://travis-ci.org/hneemann/Digital.svg?branch=master)](https://travis-ci.org/hneemann/Digital)

[Letztes Release herunterladen](https://github.com/hneemann/Digital/releases/latest)

Die letzten Änderungen sind in den [Release Notes](distribution/ReleaseNotes.txt) zu finden.

# Digital #

![screnshot](screenshot.png)

![screnshot2](screenshot2.png)

Digital ist ein Simulator für digitale Schaltkreise. Er ist für Lehrzwecke entwickelt worden und
wird von mir in meinen Vorlesungen eingesetzt.
Bevor ich mit der Entwicklung von Digital begonnen habe, habe ich
[Logisim](http://www.cburch.com/logisim/) von Carl Burch
eingesetzt. Zu erkennen noch daran, dass die Farbschemata der Leitungen sich ähneln.

Logisim ist ein ganz hervorragendes Werkzeug welches sich beim Einsatz in der Lehre vielfach 
bewährt hat. Leider hat Carl Burch die Entwicklung von Logisim 2014 eingestellt.
Stattdessen hat er 2013 die Entwicklung eines neuen Simulators [Toves](http://www.toves.org/) begonnen. 
In seinem [Blog](http://www.toves.org/blog/) erklärt er, warum er anstelle einer Weiterentwicklung von Logisim 
eine Neuentwicklung begonnen hat: Es gibt Schwächen im Design von Logisim, die schwer zu beheben sind.
Leider wurde die Entwicklung von Toves sehr früh eingestellt.

Carl Burch hat jedoch Logisim als Open-Source zur Verfügung gestellt, so dass es eine Reihe von Projekten gibt, 
welche eine Weiterentwicklung betreiben:

- [Logisim-Evolution](https://github.com/reds-heig/logisim-evolution) von Mitarbeitern dreier schweizer Institute (Haute École Spécialisée Bernoise, Haute École du paysage, d'ingénierie et d'architecture de Genève, and Haute École d'Ingénierie et de Gestion du Canton de Vaud)
- [Logisim](https://github.com/lawrancej/logisim) von Joseph Lawrance vom Wentworth Institute of Technology, Boston, MA
- [Logisim-iitd](https://code.google.com/archive/p/logisim-iitd/) vom Indian Institute of Technology Delhi
- [Logisim](http://www.cs.cornell.edu/courses/cs3410/2015sp/) des CS3410 Kurses der Cornell Universität

Aber soweit ich weiß, wird in diesen Projekten nicht daran gearbeitet, die architektonischen Schwächen zu beheben.  
Es geht eher darum, neue Funktionen zu implementieren und Fehler zu beheben. [Logisim-Evolution](https://github.com/reds-heig/logisim-evolution), 
wurde zum Beispiel um einen VHDL/Verilog Export erweitert.

Daher habe ich im März 2016 entschieden einen neuen Simulator von Grund auf neu zu entwickeln.
In der Zwischenzeit wurde ein Stand erreicht, der Vergleichbar ist mit Logisim. 
In einigen Gebieten (Performance, Testen von Schaltungen, Schaltungsanalyse, Hardware-Unterstützung) wurde Logisim übertroffen.

## Features ##

Folgende Features zeichnen Digital aus:

- Messwertegraph zur Anzeige von Signalzuständen.
- Gatterschrittmodus zu Analyse von Oszillationen.
- Analyse und Synthese von kombinatorischen Schaltungen und Schaltwerken.
- Einfaches Testen von Schaltungen: Es können Testfälle erstellt und aussgeführt werden.
- Viele Beispiele: Vom Transmission-Gate D-FF bis zum kompletten MIPS-ähnlichem Prozessor.
- Enthält eine Bibliothek mit den am häufgsten genutzen Schaltkreisen der 74xx Serie. 
- Fast-Run-Mode um eine Simulation ohne Aktualisierung des HMI durchzuführen.
  Ein einfacher Prozessor kann mit 100kHz getaktet werden.
- Einfache Remote TCP-Schnittstelle um z.B. mit einer [Assembler-IDE](https://github.com/hneemann/Assembler) den Simulator zu steuern.
- Direkter Export von JEDEC Dateien welche in ein [GAL16v8](https://www.microchip.com/wwwproducts/en/ATF16V8C) 
  oder [GAL22v10](https://www.microchip.com/wwwproducts/en/ATF22V10C) geschrieben werden können. 
  Diese Bausteine sind zwar schon sehr alt (vorgestellt 1985!) jedoch sind sie ausreichend für Anfängerübungen, 
  einfach zu verstehen und sehr gut dokumentiert.
  Zudem werden die Bausteine der [ATF150x](https://www.microchip.com/design-centers/programmable-logic/spld-cpld/cpld-atf15xx-family)
  Familie unterstützt welche bis zu 128 Makrozellen bieten. 
- Export zu VHDL: Eine Schaltung kann zu VHDL exportiert werden. Zudem wird das  
  [BASYS3 Board](https://reference.digilentinc.com/reference/programmable-logic/basys-3/start) unterstützt. 
  Details können der Dokumentation entnommen werden. 
  Die Beispiele enthalten eine Variante der einfachen CPU, welche auf dem BASYS3 Board läuft.
- SVG-Export von Schaltungen, incl. einer LaTeX-tauglichen SVG-Variante (siehe [ctan](ftp://ftp.fau.de/ctan/info/svg-inkscape/InkscapePDFLaTeX.pdf))
- Keine Altlasten im Code
- hohe Testabdeckung (exclusiv der GUI-Klassen etwa 80%). Fast alle Beispiele enthalten Testfälle, 
  welche sicherstellen, dass diese korrekt arbeiten. 

## Kommentare ##

Wenn Sie einen Fehler melden oder einen Verbesserungsvorschlag machen möchten, können Sie die GitHub 
[Fehlerverfolgung](https://github.com/hneemann/Digital/issues/new) verwenden. 
Dies hilft mir, Digital zu verbessern, also zögern Sie nicht.

Alternativ kann auch eine private Nachricht an [digital-simulator@web.de](mailto:digital-simulator@web.de) 
gesendet werden.

## Motivation ##

Im Folgenden möchte ich kurz auf die Punkte eingehen, die mich motiviert haben, eine Neuentwicklung zu 
starten:

### Einschalten ###

In Logisim gibt es kein echtes "Einschalten" einer Schaltung. die Schaltung ist auch aktiv, während sie verändert wird. 
Dies kann zu unerwartetem Verhalten führen: Ein einfaches Master-Slave JK-Flipflop 
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
Dies ist das selbe Problem welches auch bei Logisim auftritt.  
  
Um dieses Problem zu lösen, wird während der Einschaltphase wird ein anderer Simulationsmodus verwendet: 
Alle Gatter lesen ihre Eingänge ein und aktualisieren sofort ihre Ausgänge. 
Dies geschieht Gatter für Gatter in zufälliger Reihenfolge der Gatter, bis es keine 
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
komplexere Übungen wie z.B. Conway's Game of Live ausreichend ist.
Es gibt ein Break-Gatter welches einen einzelnen Eingang hat. Wechselt dieser Eingang von low auf high wird dieser 
schnelle Lauf beendet. Auf diese Weise lässt sich eine Assembler-Anweisung BRK implementieren, womit sich dann Break-Points 
in Assembler-Programme einfügen lassen. 

### Debugging ###

In Logisim gibt es keine einfache Möglichkeit um ein Assembler-Programm in einem simulierten Prozessor zu debuggen.
Digital bietet eine einfache TCP-basierte Schnittstelle, so dass eine [assembler IDE](https://github.com/hneemann/Assembler)
den Simulator steuern kann. Ein solche IDE kann Anwendungen in den simulierten Prozessor laden, den Prozessor starten und beenden, 
Einzelschritte ausführen usw..
Nach jeder Aktion wird die aktuelle Code-Adresse an die IDE zurückgegeben. Auf diese Weise kann in der IDE der aktuell 
ausgeführte Befehl hervorgehoben werden. Auf diese Weise kann ein Assembler-Programm sehr einfach analysiert werden.   

### Schaltungssynthese ###

Logisim kann kombinatorische Schaltungen aus einer Wahrheitstabelle erzeugen und umgekehrt. In Digital ist dies ebenfalls möglich. 
Zudem lassen sich mit Digital auch Automaten aus einer geeigneten Zustandsübergangstabelle erzeugen. Dabei kann sowohl 
das Übergangsschaltnetz als auch ein Ausgangsschaltnetz erzeugt werden. Die  Minimierung der Ausdrücke erfolgt dabei 
nach dem Verfahren von Quine und McCluskey. Ebenso lässt sich aus einer Schaltung, die D-Flipflops oder JK-Flipflops 
enthält die Zustandsübergangstabelle ermitteln. Zu beachten ist jedoch, dass dies nur mit den eingebauten Flipflops funtioniert.
Ein FlipFlop welches z.B. aus NOr Gattern aufgebaut wurde, wird nicht als solches erkannt. 
Die Analyse sequentieller Schaltungen funktioniert nur, wenn rein kombinatorische 
Schaltungen mit den eingebauten D- oder JK-Flopflops kombiniert werden.     
Nach der Erzeugung der Wahrheitstabelle kann eine JEDEC-Datei für ein 
[GAL16v8](http://www.atmel.com/devices/ATF16V8C.aspx) oder ein [GAL22v10](http://www.atmel.com/devices/ATF22V10C.aspx).
Danach kann diese Datei in einen entsprechenden Baustein geschrieben werden, um sie in einem realen Aufbau zu testen.
Wie erwähnt sind diese Bausteine zwar schon sehr alt, jedoch mit 8 bzw. 10 Makrozellen ausreichend für einfache Übungen. 
Werden mehr Makro-Zellen benötigt, kann in der PDF Dokumentation nachgelesen werden, wie Digital konfiguriert werden muss, 
um die Bausteine [ATF1502](http://www.microchip.com/wwwproducts/en/ATF1502AS) und
[ATF1504](http://www.microchip.com/wwwproducts/en/ATF1504AS) zu unterstützen, welche 32 bzw. 64 Makrozellen bieten.  

## Wie fange ich an? ##

Am einfachsten ist es, das [letzte Release](https://github.com/hneemann/Digital/releases/latest) herunter zu laden. 
In der ZIP-Datei findet sich das Binary (Digital.jar) und alle Beispiele. Ein Java JRE 1.8 wird benötigt, um Digital zu starten.

Wenn Digital direkt aus dem Source Code gebaut werden soll:
 
* Zunächst ist dieses Repository zu clonen.
* Ein JDK 1.8 wird benötigt (entweder Oracle JDK 1.8 oder OpenJDK 1.8)  
* maven wird als Build-System verwendet, daher ist es das einfachste [maven](https://maven.apache.org/) zu installieren.
* Danach kann mit `mvn install` Digital gebaut werden.
* Mit `mvn site` kann ein findbugs und ein cobertura code coverage report erzeugt werden.
* Die meisten IDEs (Eclipse, NetBeans, IntelliJ) könne die Datei `pom.xml` importieren um ein Projekt zu erzeugen.

## Contribution guidelines ##

* Wer beitragen möchte kann mir einen pull request schicken.
  * Bevor ein Pull-Request geschickt wird, sollte mindestens `mvn install` ohne Fehler durchlaufen.
* Es sollten keine neuen findbugs Fehler hinzugefügt werden.
* Die Testabdeckung sollte noch gehalten werden. Das Ziel ist 80% Testabdeckung in allen nicht GUI Komponenten.
* Bis jetzt gibt es nur wenige GUI Tests. Dahier liegt die gesamte Testabdeckung nur bei etwas unter 80%.
  Versuche den Anteil an ungetestetem GUI-Code so gering wie möglich zu halten.
