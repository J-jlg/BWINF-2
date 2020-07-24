# 38. Bundeswettbewerb Informatik
## 2. Runde

> Teilnehmer: Jan-Luca Gruber  
> Betreuer: Frank Schell, Friedrich-Hecker-Schule Sinsheim  
> Programmiersprache: Java  
> Entwicklungsumgebung: Eclipse  

### 1.a) Lösungsidee/Abstrakt:

Ziel des Aufgabenteils ist es, einen Weg bzw. Pfad so zu finden, dass alle Batterien, wenn möglich entladen werden. Die Aufgabenstellung lässt die Art und Weise, also zudem auch die Qualität des Weges offen, was auf die Benutzung eines heuristischen Suchverfahrens als Optimum schließen lässt. Da es sich dabei um eine [offline-Suche](https://xlinux.nist.gov/dads/HTML/offline.html) handelt, können unter Anbetracht der Laufzeit,beliebig viele Vorverarbeitungen stattfinden, um ungültige Wege, die schlussendlich nicht in den Zielzustand führen, auszuschließen. Dies gelingt durch das grundlegende Vermischen einer [Sackgassenerkennungs- und Gruppierungsheuristik](https://www.uni-kassel.de/eecs/fileadmin/datas/fb16/Fachgebiete/PLM/Dokumente/Master_Bachelor_Diplom/masterarbeit.pdf). Doch zuerst einmal gilt es das Problem so zu
Modellieren, dass man es mit klassischen Algorithmen bearbeiten kann. Dafür benötigt man in
diesem Fall einen Graphen.

#### 1.1. Modellierung der Knoten:

Der erste wichtige Schritt, ist die Repräsentation des Problems und somit die Wahl der
Datenstruktur. Gegeben ist ein k*k großes Spielfeld: S, wobei k für die Länge/Breite des
quadratischen Spielfeldes steht. Der Roboter a (für Agent), wird durch die Notation: x,y,Ladung in
dieses Feld instanziiert. Er ist auf die sogenannte [Manhattandistanz](https://deacademic.com/dic.nsf/dewiki/) reduziert, er kann sich also
lediglich nach oben, unten, rechts und links jeweils einen Schritt bewegen. Pro absolvierten Schritt,
wird seine Ladung um eins reduziert. Folgend werden durch die weiteren Zeilen die Ersatzbatterien
definiert und auf das Feld übertragen. Um nun den Roboter nicht wild durch die Gegend rennen zu
lassen, wird die 2d Map auf einen gerichtet- und gewichteten Multigraphen 
```sh
G := (V, E), 
```

mit V := Menge von Batterien und E := Menge von Kanten, reduziert. Kante 
```sh
 e∈E mit e(v, v') bzw. e(v, v)
```
ist dabei eine zusammengefasste Mehrfachkante, worauf in Generieren der Kanten weiter eingegangen
wird. Durch Benutzung eines Graphen kann man den Roboter zielstrebig von Batterie zu Batterie
schicken, was massiv Wege also Rechenleistung erspart.


[![Build Status](https://i.ibb.co/yg52Vvw/Unbenannt1.png)](https://travis-ci.org/joemccann/dillinger)

Für jede Ersatzbatterie 

> v<sub>1</sub> ∈ V ... v <sub>n</sub> ∈ V,   

sowie für den Agenten
```sh
 v<sub>0</sub> ∈ V gilt: v∈V:v ∈S bzw. V ⊆S. 
```

Diese Prozedur führt in Bsp: 1,4,5,6 zu einer massiven Verkleinerung des Spielfeldes bzw. der
Knoten V(siehe Abbildung 1 für Bsp: 1).
```sh
 G<sub>0</sub> (Graph <sub>r</sub>) = (V<sub>anzBatt</sub>, E<sub>0</sub>)
```
mit
```sh
E<sub>0</sub> := ∅ und |V| := Anzahl an Ersatzbatterien + dem der Agentenbatterie.
```

This is some <sup>superscript</sup> text.
This is some <sub>superscript</sub> text.

Die Laufzeit für das Durchsuchen der Knoten, welches später oft durchgeführt und
somit eine große Rolle spielt, reduziert sich also von 
> O(k*k) -> O(n).

Wobei für die Anzahl Knoten
n gilt: n <= kk. Der Fall n = kk(zudem auch n ≈ k) wird später im Abschnitt: SZK und Geo-
Gruppen behandelt. Um nun die einzelnen Batterien entsprechend zu identifizieren, bekommt jede
Batterie, inklusive der des Agenten(grün) eine Nummer/Index zugewiesen. Der Agent bekommt die
Nummer 0, während die anderen nach Inputreihenfolge sortiert werden. Jedes Objekt der Klasse
Batterie, besitzt entsprechend x und y Koordinate im kk Feld, Anzahl an Energie, ein Boolean, zur
Überprüfung, ob die Batterie abgearbeitet wurde. Wobei gilt:
{v|vindex.energie=0}→vindex.isDone=true. S wird in einem zweidimensionalen Array
gespeichert(siehe Generieren der Kanten ) und G verwendet eine doppelte Linkedlist(Adjazenzlist
adj) als Datenstruktur für die Kanten und speichert die Batterien in einer Arraylist mit gleicher
Nummerierung/Indexierung, in welcher für jeden Knoten die entsprechenden Kanten gespeichert
sind.

2. Dynamischer vs Statischer Graph

Bevor man sich an das Generieren der Kanten wagt, muss die optimale Datenstruktur für diese
gewählt werden. Es kommen zwei Modelle infrage, das des statischen- und das des dynamischen
Graphen. Entweder man erschafft einen Graphen, der alle Kanten behält und für jeden neuen Pfad
die Kosten des Weges mit der Energie des entsprechenden Knotens abgleicht(statisch) oder man
nimmt für den Graphen nur das nötigste(Reichweite=Energie) und löscht bspw. alle Kanten nach
erfolgreichen Abarbeiten des Knotens(v'→v mit eingeschlossen). Ist die Energie einer Batterie nach
einem Wechsel jedoch höher als die alte Energie(v.energieneu>v.energyalt), so werden dem Knoten v
alle möglichen Kanten aus einer externen Liste(siehe Generieren von Kanten ) zugewiesen. Der
folgende Operator Expandieren bezieht sich auf das heuristische Erweitern eines Pfades, durch


Wählen einer Kante e(v,v') oder e(v, v), also somit auf ein kostengebundenes Handeln jeglicher
Form.

Dynamischer Graph Statischer Graph

Vorteile:
-Expandiert nicht zu fertigen Nodes, da es zu
diesen logischerweise keine Kanten gibt. Somit
werden Abfragen reduziert
-Gruppierung möglich
-weniger Kanten und somit auch schnellere
Laufzeit, da |E|dynamisch <= |E|statisch
-insert Operation → O(1)

Vorteile:
-keine delete → O(m[n])→ Alle Kanten, des
Knoten v, da eine Linked List als Datenstruktur
gewählt wurde.
-insert Operationen → O(1)
-pro Expansion muss lediglich V gespeichert
werden, da E immer gleich bleibt.

Nachteile:
-delete → O(m[n]) → w.c.
-pro Expansion muss V+E gespeichert werden,
da die Parameter des Graphen Gzustand, für jeden
Pfad einzigartig sind.

Nachteile:
-Graph benötigt mehr Speicher, da
|E|statisch >= |E|dynamisch
-Benötigt mehr Laufzeit, da schließlich Kanten,
selbst wenn der Knoten „fertig" ist, bleiben. →
min O(1) pro Expansion.
-hat keine ersichtliche Methode zum Gruppieren
-Graph kann zumindest im average case nicht
sinnvoll unterteilt bzw. vereinfacht werden.
-Es muss viel vorverarbeitet werden, da in dem
Node 3(siehe bsp. oben) auch die Energie 6
reingehen würde, wegen 0→3. Es muss also für
jeden Node die maximale Energie gefunden
werden.

Ich habe mich in diesem Fall für den vollen dynamischen Graphen^4 entschieden(insert + deletion),
da vor allem die folgenden Gruppierungen(siehe SZK und Geo-Gruppen ), massiv Expandierungen
reduzieren. Es gilt die Heuristik so schlau bzw. informiert wie möglich Expandieren zu lassen, also
nur die best möglichen Wege zu gehen. Denn eine uninformierte Suche wäre reines Bruteforce.
(siehe SZK und Geo-Gruppen Bruteforcevergleich)

3. Generieren der Kanten

Da nun die Datenstruktur für die Knoten sorgfältig gewählt wurde, gilt es die Kanten e E von G∈ init
zu generieren, wobei Ginit den dynamischen Ausgangsgraph darstellen soll.(siehe oben links
dynamischer Graph)
Da es nun für jede Kanten, die von v → v' = e(v,v') E und v → v = e(v,v) E verläuft, mehrere∈∈
Kosten geben kann(Multigraph), wie in dem Beispiel oben, von dem Agent mit der ID:
0→3(x:5,y:4) kann die Kante k quasi jede Kosten von dem kürzesten Pfad(3 Schritte) bis
Agentenbatterie von dem minimum ausgehend immer plus zwei haben.

4 https://www.geeksforgeeks.org/dynamic-connectivity-set-1-incremental/

∑
i = 1

n
min ( v→v' )+( i ∗ 2 )

Um jetzt nicht jede einzelne Kante in dem dynamischen Graphen, der schließlich für eine
Reduzierung der Kanten gedacht ist in dem Graph abzubilden, wird nur der kürzeste Pfad von
v → v' bzw. v → v abgebildet. In diesem sind alle anderen Kanten nach v' mit inbegriffen, was die
Kante e zu einer zusammengefassten Mehrfachkante macht. Der Algorithmus berücksichtigt dies
dann für jede Expansion und expandiert wenn nötig, mit höheren Kosten zu v'. Jedoch gilt diese
Regel nicht für jeden Fall: Beträgt die Entfernung: 1 und es gibt keinen anderen Weg zu diesem
Knoten, so ist die Kante nicht expandierfähig(festgehalten wird dies in dem boolean isExpandable
von e). Bei der Entfernung 2 gilt dasselbe. Ab der Entfernung: 3, kann der Agent sich innerhalb des
Pfades unendlich oft vor und zurückbewegen und für jede k.energie >= 3 gilt: k.isExpandable=true.
Kante k → IsExpandable:

true false true false true

Oben ein paar Beispiele. Zudem muss berücksichtigt werden, dass der Agent(grün) kein eigentlicher
Node ist, da seine Batterie mit ihm mitgeht. Daraus folgt, dass sein Feld als Freies angesehen wird.
Also wird nach dem ersten Bewegen des Roboters von dem Startfeld: 0 → isDone=true. Der Node
0 wird anschließend als freies Feld angesehen. Die Aufgabenstellung erlaubt es außerdem noch,
dass der Roboter mittels min(2) Schritten bzw. auch wie bei v→v' jeweils plus zwei Schritte von
v → v = e(v,v) geht. Dabei ist wieder zu beachten, dass dies nur in gewissen Fällen funktioniert,
wenn mindestens ein Feld Mitteles der Manhattandistanz von dem Node frei ist, betragen die
Kosten von e: 2 Schritte und sobald 2 freie Felder Kombinationen von dem Node aus frei sind, kann
v mit 2*x(x: 0...∞) Kosten nach v. Natürlich gilt auch hier, dass Nodestart 0(grün) zu sich selbst
logischerweiße keine Kante(n) hat.

Kante k(2) → IsExpandable: true false false false

Folgende Beispiele beweisen/zeigen auf, warum diese zwei Überprüfungen von Nöten sind:

Wie schon angedeutet, wird jede Kante mittels einer Breitensuche ermittelt. Dies stellt sich als
effizient heraus, da weder Ziel bekannt ist und zudem noch jeder Knoten in einem Durchlauf
gefunden werden kann.^5 Durch diese kann nun für jeden Knoten die geringsten Kosten der Kanten
von v → v' abgelesen werden, sowie für v → v wenn die obigen Kriterien erfüllt sind. Für den Node
3 im Bsp:1(1. Seite)

5 Trotz dass Algorithmen, wie: https://zerowidth.com/2013/a-visual-explanation-of-jump-point-search.html(JPS(+))
schneller wären, benötigt die Breitensuche, genau wegen nur einem Durchlauf weniger Laufzeit.

würde der 2d Array(welcher aus diesem Grund in Modellierung des Graphen angelegt wurde) nun
wie folgt aussehen:

Natürlich wird die Breitensuche nur entsprechend bis zur Energie des Nodes gemacht. Die generelle
Laufzeit für das Generieren aller Kanten in der AdjazenzList beträgt: O(nd) im worst case.
(Distanz d := 4e(n)) Wobei e(n) für die Energie des Knotens n steht. Für die Ausnahmen(zu sich
selbst/Agent bzw. Batt: 0 beachten): wird entsprechend max O(3) → O(1) Zeit pro BFS benötigt,
was nicht erwähnenswert ist. Die Laufzeit der BFS ist zwar relativ hoch, doch ist sie für eine
korrekte Lösung nötig. Wobei 4e(n)^ maximal kk Nodes besuchen kann. → d <= kk-2(wobei -
für den Knoten selbst, sowie den des Agenten steht).
Um nun für den Fall vorbereitet zu sein, dass die neue Batterie, nach einem Wechsel größer ist, als
die Alte, wird eine Extraliste angelegt. Diese AdjazenzListe: externList, speichert für jeden Knoten
mit Hilfe einer Breitensuche wie oben, die Kanten, die bei dem maximal Möglichen, was in die
Batterie reinkommen kann, entstehen können. Das Maximum, wird mit Hilfe der SZK^6 ermittelt,
worauf später noch weiter eingegangen wird. Was aber der markante Unterschied zu dem statischen
Graphen ist, dass die externList sekundär und nicht primär gedacht ist, was der Dead-End
Detection^7 etc. nicht im Weg steht. Man verwendet hier also nochmals eine weitere O(nd) Suche,
was sich allerdings dadurch ausgleicht, dass später für jede neue (höhere) Batterienanzahl in v, eine
weitere Breitensuche getätigt werden müsste. Und der Unterschied zwischen O(m[n]) und
O(4*e(n)) ist wohl deutlich klar.

4. Zielzustand

Da nun Kanten und Knoten generiert wurden, gilt es, bevor man einen Pfad zum Ziel sucht, dieses
erstmals zu definieren. Es wird ein Zielzustand in Form eines Graphen gesucht, inwelchem die
Agentenbatterie gleich der Anzahl an gesammten Batterie Energien des Spielfeldes ist und es eine
Kante von vAgenten → v' bzw. vAgenten → v mit den Kosten >= a.getEnergie() gibt. Zudem muss für alle
anderen Knoten v gelten: ∀∈v V:v.isDone=true.

5. SZK und Geo-Gruppen

Nachdem Ginit erfolgreich generiert wurde, müssen einige grundlegende Dinge für das heuristische
Verfahren vorbereitet werden. Für das schnelle Finden eines gültigen Weges, gilt es die Heuristik so
optimal wie möglich expandieren zu lassen. Würde man jetzt mittels reinen Bruteforces, die
Beispiele in Angriff nehmen wollen, so würde man für Beispiel 1 zwar „nur" ~50mal expandieren
müssen, für Beispiel 5 hingegen lässt sich aufgrund der vielen Variationen an Expandierungen
selbst nach mehreren Millionen Expandierungen kein nennenswerter Fortschritt zeigen. Deshalb
werden nun die einzelnen Knoten mittels zweier Verfahren in kleinere Gruppen unterteilen.
(I)Der erste Algorithmus findet die stark zusammenhängende Komponenten SZK. Und kann somit
Aussagen darüber treffen, welche Knoten nahe aneinander sind, bzw. ob sie von einander erreichbar
sind. Ich habe hier den Algorithmus von Tarjan implementiert, weil er mit seiner Laufzeit von
O(V+E) in diesen Fall eine optimale Laufzeit hat.^8

6 Die deutsche Abkürzung der Starken Zusammenhangskomponente. (Strongly connected component)
7 Vgl. Sackgassenerkennungs Seite 3
8 Kosaraju's algorithmus O(V^2 ) würde im a.c. mehr Laufzeit beanspruchen. Vorallem in Fällen, wie k=n*n würde die
Lauzeit drastisch fallen.

Beispiel: 5 SCC von Beispiel 5

Warum dieser Algorithmus für die Heuristik so mächtig ist, zeigt sich gleich noch. Vorher wird von
jeder SCC Gruppe das Maximum ausgerechnet mit der Methode Helper.calcMax → O(n). → Grün
anschließend noch mit einer weiteren Breitensuche jeweils die kürzesten Distanzen von SCC→
SCC' gefunden und in einer adjSCCMatrix übertragen → O(n*4e(n)), jedoch ist nun alles
vorverarbeitet(Runtime wird später noch gezeigt). Hier wird nun auch das Maximum für die
externList bestimmt, indem geschaut wird, was ist das Maximum an Energie, was eine SCC Gruppe
erreichen kann. Dieser individuelle maximale Wert für jede SCC Gruppe wird anschließend als
maximale Energie für jeden Knoten in dieser SCC Gruppe genommen. Dadurch enthält externList
garantiert den maximalen Energie Wert für v V(Agent: 0 ausgeschlossen). Das nun Geniale dieser∀∈
Ansammlung an Informationen über den Graphen lässt die Heuristik so expandieren, dass jede
SCC-Gruppe von v SCC∈ i erreichbar ist. Würde der Agent sich nun von 0→(SCC-Gruppe von 0:
isDone=true, da alle v SCC∈ 0 isDone=true) bewegen, so kann die Heuristik nur so expandieren, dass
das Maximum in der linken SCC Gruppe mindestens 10 bleibt.(Helper.calcMax wird Runtime
aufgerufen. Also pro Expandierungen: → O(n) Runtime mehr!) Die „Informed Heuristic Search"
bezieht sich hier auf das Modell des Greedy-Algorithmen, was bedeutet, dass jeder Expandierte
Weg, anhand einer Bewertungsfunktion in der Priority Queue oder auch OpenList sortiert wird.
(Der Weg mit dem höchsten Prioritätswert, kommt dann entsprechend in die Queue ganz nach
vorne. Was meine Heurstik nun so speziell macht ist, dass sie keine Closed List besitzt. Dies hat den
einfachen und simplen Grund, weil es sich in diesem Programm um einen dynamischen Graphen
handelt und Expandierungen als Zustand in der OpenList gespeichert werden.
Die Expandierungen reduzieren sich jedoch trotzdem drastisch:

Expandierungen: Brutforce Informed Heuristic Search
Beispiel 1: 50 4
Beispiel 4: 296 16
Beispiel 6: Weit über >40.000.000! 481398

Runtime: Brutforce Informed Heuristic Search
Beispiel 1: ~8,4ms ~6ms
Beispiel 4: ~20,6ms ~8,2ms
Beispiel 6: >10min ~4,7sek

Laufzeitfaktoren pro Expansion:
Bruteforce Informed Heuristic Search
O(m(n)) → durchsuchen der Kanten von v → v'
O(2*(m+n)) → O(m+n) ==>kopieren der Listen
Falls v.battalt < v.battneu => O(m(n))→extendList
Falls v.isDone → O(m(n)) => löschen Kanten
von v

O(m(n)) → durchsuchen der Kanten von v →v'
O(2*(m+n)) → O(m+n) ==>kopieren der Listen
Falls v.battalt < v.battneu => O(m(n))→extendList
Falls v.isDone → O(m(n)) => löschen Kanten
von v
Helper.calcMax, da dynamischer Abgleich →
O(n)
Helper.copyDistMatrix → O(SCC.size)

Wie man unschwer erkennen kann, schlägt die Informed Heuristic Search: IHS den banalen
Brutforce Algorithms, trotz extra Runtime pro Expandierung bei Weitem. Die Kostenfunktion der
IHS^9 , beläuft sich auf Kosten f(x) = h(x). Mit h(x) → gesAnzahlMax-gesAnzahlcurrent. Was IHS als
eine greedy(„gierige") Suche definiert.
Doch das alleinige Unterteilen des Graphen in SCC-Gruppen, genügt nicht, denn nicht bei allen
Spielinstanzen(Beispielen), lässt sich der Graph in mehrere SCC-Gruppen aufteilen. Siehe Bsp: 2,
SCC.size=2,. was die Informed Heuristic Search wieder zum banalen Bruteforce Algorithmus
verwandelt, da das Verfahren darauf bestand Wege anhand der SCC-Gruppe auszuschließen. Es gilt
nun noch andere Methoden zu ermitteln, um das Aufteilen des Graphen in
mehrere Gruppen, des Expandierens willen, so einfach wie möglich zu gestalten.
(II) Der andere Ansatz bedient sich der geografischen Abbildung des Graphen. So werden z.B. die
Graphen:

aufgeteilt. Sinn und Zweck dieser Blöcke ist es, wie auch schon bei Ansatz (I), die Anzahl V und
somit auch die Expansionen von Ginit drastisch zu reduzieren. So kann man z.B. bei einer geraden
Anzahl an Knoten im k*k Feld: |Vneu| = |Valt|/4. Geht man einen Schritt weiter, so könnte man immer
größere Blöcke nehmen, was die Laufzeit, jedoch nur bis zu einem gewissen Punkt reduziert, da
irgendwann 1 Block:= 100 groß. Deshalb verwendet der Algorithmus nur 2x2 Blöcke für gerade-,
3x3 und 2x3 Blöcke für ungerade Graphen, um einen allgemeinen Standard zu halten.
Es wurden diese Normen gewählt, da sie sich bei verschiedenen Tests als schnell berechenbar und
flexibel darstellten. Anzumerken ist hierbei, dass (II) als Ersatz für (I) gilt, wenn: |SCCanzahl| == 2.
Der Vorgang, von (I) bleibt allerdings vorerst der Gleiche. Die Laufzeit für das generieren,
verändert sich entsprechend von O(|V|+|E|) → O(g(k)). Wobei g(k) natürlich abhängig von der
Größe ist. Im Beispiel Obenlinks, hätte g(k) eine Laufzeit von O(4)→O(1)=konstant, was natürlich
im Vergleich zu Tarjan eine gewaltige Reduzierung ist.

6. Priority Queue

Die wohl wichtigste Methode, um gezielt einen möglichen Weg zu finden, ist die Kostenfunktion.
Kleine Änderungen dieser, führen zu großen Runtime Verbesserungen, der möglichen Beispiele. Es
gilt jedoch trotzdem, bei |ae| := Anzahl Expansions, dass |aegreedy| = |aefolgend|, da der w.c.^10 gleich
bleibt! Die jetzige „gierige" Kostenfunktion, beläuft sich auf: f(x) = h(x). Um nun eine gezieltere

9 Informed Heuristic Search
10 Hier: worst case

Suche zu starten, wird dass Modell, des Bergsteigeralgorithmus^11 , mit runtergehen implementiert.
Das heißt auf die SZK und Geo-Gruppen Modelle bezogen, dass f(x) versucht, jede Gruppe(Station
in dieser Analogie) einzeln abzuarbeiten, um anschließend jede abgeschlossen Gruppe, als weitere
Stufe anzusehen. Auf die Beispiele bezogen ergibt sich somit eine Laufzeit von:

Runtime: Informed Heuristic Search
Beispiel 1: ~6ms
Beispiel 2: ~0,118sek
Beispiel 3: ~0,97sek
Beispiel 4: ~8,2ms
Beispiel 5: 0ms
Beispiel 6: ~4,7sek

7. Preprocess vs Runtime

Da die Diagramme in siehe SZK und Geo-Gruppen deutlich machen, dass das Gruppieren eine
erhebliche Laufzeitsteigerung mit sich bringt, kam die Idee, den SCC zu Maintainen, also während
jeder Expansion den SCC mit allem drum und dran neu zu generieren um Runtime, falsche
Expandierungen zu bestimmen. Doch die praktische Umsetzung zeigte, dass dies länger dauern
würde, als ohne. Dies hat zwei Gründe:

    Es wird pro Expandierung: O(|V|+|E| + |E|) → O(|V|+2*|E|) → O(|V|+|Emehr|) → O(|V|+|E|),
    Laufzeit mehr benötigt.
    Doch der eigentliche Gedanke, war eine Verkürzung der Expandierungen, was sich aber als
    falsch rausstellte, da sich nun viele kleine Gruppen, während des Expandierens/Suchens bilden, die
    zwar zu lokalen anderen SCC Gruppen expandieren konnten, jedoch global falsch waren.

Wie man in diesem Beispiel unschwer erkennen kann, gibt es keinen Weg von SCC 0 →SCC 1 (links)
was jedoch von dem Runtime-SCC Algorithmus nicht gesehen wird, da nur nach den lokalen
Nachbarn gesucht wird! Würde man den Runtime/Laufzeit-Algorithmus um den globalen Faktor
erweitern, würde dass entsprechend O(|SCC|²) Zeit extra in Anspruch nehmen, was gegen die
Laufzeit Implementierung spricht! Daher habe ich es beim Vorverarbeiten, aus den gegebenen
Gründen belassen.

11 https://www.edureka.co/blog/hill-climbing-algorithm-ai/#hillclimbingapplications

8. OpenList/Zustandsspeicherung

Pro Zustand werden 7 Faktoren gespeichert, welche für das Expandieren und Überprüfen, wie in
den vorherigen Kapiteln besprochen von Nöten sind. Es ist noch anzumerken, dass Variationen der
OpenList, etwa der rekursiven Verwaltung per Pfad-Speicherung in der Praxis scheitern, da sich die
Laufzeit entsprechend um O(n[m]) pro Löschung addiert. Um nun auf die 7 Parameter zu kommen:
Zustand z:
[0] = e([2]), [1] = e[v'], [2] = Energieges, [3] = adjList, [4] = bList, [5] = DistanzMatrix, [6] = Pfad

9. Speicheranalyse der OpenList

pro Expansion
Bruteforce: O(1), O(1), O(1), O(n), O(m), O(SCCMatrix),
O(1) => O(n+m+|SCCMatrix|)
Informed Heuristic Search: O(1), O(1), O(1), O(n), O(m), O(SCCMatrix),
O(1) => O(n+m+|SCCMatrix|)

Wobei anzumerken ist, dass für die OpenList(PriorityQueue): pq, bei Gendlich nie gilt: |pq| ≠ ∞, weil
|E| ≠ ∞, somit hält sich die Speicherauslastung pro Expansion im Rahmen. Da auch Pfade falsch
sein können, besteht kein Bedarf diesen weiterhin in eine List zu speichern, weshalb dieser Platz für
einen neuen macht. Würde der Algorithmus eine Closed List besitzen, so könnte der Speicher bis in
das Unendliche steigen.

10. Besonderheiten in der Geo-Gruppierung

Da die Gruppierung, je nach Größe des Spielfeldes: k variiert, ist es von dringenden Nöten,
nochmals darauf einzugehen. Wie bereits festgestellt wurde, kann ein gerades Spielfeld je in 2x
Blöcke aufgeteilt werden, was zu einer Halbierung des Suchraumes führt. Doch muss bei einer
ungeraden Aufteilung auf die Größe des Spielfeldes geachtet werden. So ist es nicht immer
möglich, ein Muster für jedes ungerade Feld zu wählen. Deshalb habe ich ein Clusterverfahren^12
entwickelt, welches erlaubt die ungeraden Spielfelder in drei Hauptgruppen einzuteilen. Das
Clusterverfahren besteht auf der Erkenntnis, dass kungerade %3=0 ∨ 1 2. Diese Isomorphie hat zur ∨
Folge, dass man für jedes Ergebnis je eine einheitliche Aufteilung vornehmen kann. Dass Verfahren
beginnt das Spielfeld von linksoben ausgehend, bis kungerade-(kungerade%3)*2 zu Unterteilen. Die
Motivation dahinter, lässt sich am besten anhand von Beispielen, welche als Repräsentation der
entsprechenden Hauptgruppen Dienen erklären:
(3%3)2=0 (5%3)2=2 (7%3)2=4 (9%3)2=

Wie man sehen kann, werden die Bereiche bis zu dem Ergebnis des Termes oben in 3er Blöcke
unterteilt und dann je Ergebnis individuell unterteilt. Dabei bleiben bei jedem Ergebnis/Isomorph
die Reihen außen immer gleich.

12 Vgl. https://i11www.iti.kit.edu/_media/teaching/sommer2007/graphclustering/ausarbeitung-bc.pdf aber zudem auch
bezogen auf den sogenannten: HPA* Algorithmus(Variation von A*), der ähnlich wie die Gateway-Heuristik siehe
Seite 2 das 2D Feld in Abschnitt unterteilt.

11. Optimierung der Gruppierung

Da jedoch nicht alle Spielfelder ein einheitliches Muster, wie z.B. in 2 & 3 besitzen, sondern
teilweise auch komplexer aufgebaut sind, wurde das Clusterverfahren zur Bestimmung der Gruppen
um die Allgemeinheit optimiert. Folgende Beispiele wäre suboptimal für den bisweilen
Clusteralgorithmus:

Es würde in diesen Beispielen nichtnur die Performance, wegen der unnötigen Vorverarbeitung
leiden, sondern auch die Effizienz und Zielfindung(Heuristik). Wie die Beispiele veranschaulichen,
wird die ganze behutsam ausgewählte Anordnung von Blöcken, durcheinandergebracht und es
entstehen bspw. 1x1 Blöcke, was im negativen Zusammenspiel mit dem Bergsteigeralgorithmus
steht, da er darauf basiert, strukturiert Blöcke/Stationen abzuarbeiten. Bei einzelnen Blöcken bleibt
er im w.c. auf einem lokalen Gipfel stecken. Um das Clusterverfahren nun zu Optimieren, wird
Start- und Endpunkt der Clusteranalyse, nach SZK und Geo-Gruppen bzw. Besonderheiten in der
Geo-Gruppierung jeweils von den Eckwertenentnommen.
Eine weitere Optimierung ist, das Zusammenfügen der beiden Gruppierungsverfahren. Es wird nun
sowohl auf SCC, als auf Geo-Gruppen(nach dem gerade optimierten Clusterverfahren) getestet.
Dabei spielt der SZK-Algorithmus(Sackgassenerkennungsalgorithmus) eher bei der
Vorverarbeitung, aber auch beim Geo-Gruppen Maintainen eine große Rolle. Das (II)
Clusterverfahren dient dabei eher für die Heuristik an sich Beihilfe. Durch das Optimieren und
letzendlich auch dass Zusammenfügen, lässt sich die Laufzeit jedes Beispieles um einen Hauch und
für die Beispiele am Anfang drastisch verbessern. Das bis jetzt „Zeitintensivste" Beispiel: 5,
benötigt jetzt lediglich 3,9sek anstatt 4,7sek. Diese Verbesserung zieht sich wie gesagt durch alle
Beispiele logischerweise durch.

12. Ungültige Map ausschließen/EndNodeDetection

Dies soll dazu dienen, Felder ohne Ergebnis im vorhinein Auszuschließen und zu erkennen. Diese
Abfrage wird also beim Vorverarbeiten bzw. Maintainen^13 getätigt. Sie versucht, wenn möglich den
letzten Knoten des Spielfeldes zu ermitteln. Das hierfür verwendete Verfahren wird anhand des
folgenden Beispieles(Zustand) deutlich:

Wie man erkennen kann, muss entweder die Rote oder einer der beiden gelben Batterien, das Letzte
zu befahrene Node sein. Die EndNodeDetection schaut sich also grundlegend solche Extrempunkte,
die Umgeben von „fertigen" Batterien sind an und entscheidet anschließend, ob dieses Spielfeld

13 Es sich ersichtlich, dass dies natürlich ein Zustand mit Batterien, der Anzahl 0 ist. Mit Vorverarbeiten wird hier dass
ermitteln der Nachbarn, zeitgleich mit dem BFS gemeint. Maintainen bei Veränderung bzw. Nachbar.isDone=true.
Dies spiegelt sich in der konstanten Laufzeit von O(1) wieder.

lösbar ist oder nicht. Grundlegend aber gilt, ermittelt das Verfahren zwei solcher Endpunkte oder
mehr, wird zum nächsten Zustand gesprungen. Dieses Verfahren ist zwar nicht für die
Beispielaufgaben nützlich, jedoch hat sich in eigenen bzw. auch Extremstellung/Abänderungen der
Aufgaben, wie z.B. dass das gesammte Feld mit nur Batterien mit der Energie eins gefüllt ist und
dann eine Batterie wie oben eine höhere Batterie(8) hat gezeigt, dass dieses Verfahren massiv
Rechenleistung und damit Laufzeit wegen Sackgassen bzw. ungültigen Stellungen spart.

13. Grenzverhalten der Heuristik/Limitierung der Heuristik

Die Heuristik schafft es zwar den Graphen einzuteilen bzw. so intelligent wie möglich zu
expandieren, jedoch stoßt sie, wie auf Dauer alle Such-Heuristiken bei gewissen Spielfeldern an
ihre Grenzen. Man kann zwar ein 9x9 großes Feld, mit 81 Batterien/81 Knoten in 9x3x3 große
Gruppen einteilt, was somit die Gesamtgröße: √81. Aber natürlich kommt man auch nicht
drumrum, dass das Feld und somit auch die Anzahl Batterien theoretisch unendlich groß sein
könnte, was die bisherigbenutzten Cluster- und Suchverfahren ab einer gewissen Größe entkräftet.

14. GUI

Die grafische Oberfläche besteht aus dem Spielfeld(Mitte) und den Buttons(Rechts). Der Roboter
ist als Grüner Kreis dargestellt und die Ersatzbatterien als Graue kleinere Kreise. Das Spielfeld
kann entweder als Textdatei mit einem Filechooser per „Lade Datei" Button oder manuell als
Prompt^14 per „Eingabe" Button eingelesen werden. Ist ein Spielfeld ungültig, wird dies mittels eines
Promtes dem Nutzer mitgeteilt. Möchte man das Spielfeld löschen, muss man auf den Button
„Löschen" klicken oder nochmals ein neues Spielfeld eingeben. Die Koordinaten, befinden sich
rechts, bzw. unter dem Spielfeld. Diese können mithilfe des Scrollpanels am Rand des Spielfeldes
erreicht werden. Die aktuelle Energie des Roboters wird Rechts(Orange) dargestellt. Darüber wird
die aktuelle Ersatzbatterie angezeigt, welche man mit der Maus überfährt/überfahren hat. Wurde
dass Spielfeld nun erfolgreich eingelesen, bedarf es des „Start" Buttons, dass ein Weg, mit den
beschriebenen Verfahren errechnet wird. Falls kein Weg gefunden wurde, taucht ein Promt auf,

14 Oder auch: Pop-up genannt

welches dies mitteilt. Wird ein Weg gefunden, wird das Spielfeld automatisch zum Zielzustand
gebracht und der Weg wird ausgegeben(siehe Darstellung der Lösung ). Um nun den Pfad grafisch
zu betrachten, muss der Button: „<---" unter „Anfang" gedrückt werden. Dies lässt das Spielfeld
zum Eingangszustand springen. Einzelne Schritte können mit dem: „→ " Button
dargestellt/ausgeführt werden. Der Button: „--->" bzw. „Ende" lässt dabei den Benutzer wieder vom
aktuellen Zustand zum Endzustand springen.

15. Darstellung der Lösung

Die Lösung wird zudem auch in Textform dargestellt. Ein Beispiel dafür wäre:
Hoch,Rechts,Rechts,Hoch,Hoch,Hoch,Runter,Runter,Runter,Hoch,Hoch,Links,Links,
Links,Links,Runter,Hoch,
Die Zwischenschritte lassen sich anhand der Grafischen Oberfläche ablesen.

16. Beispiele

0) Eingabe:
5
3,5,
3
5,1,
1,2,
5,4,
(Grafik siehe GUI )

Ausgabe:
Hoch,Rechts,Rechts,Hoch,Hoch,Hoch,Runter,Runter,Runter,Hoch,Hoch,Links,Links,
Links,Links,Runter,Hoch,

1) Eingabe:
10
1,1,
99
1,2,

Ausgabe:
Rechts,Runter,Links,Runter,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Runter,Rechts,Runter,
Links,Runter,Runter,Rechts,Hoch,Rechts,Runter,Rechts,Hoch,Hoch,Links,Hoch,Rechts,Hoch,Links
,Hoch,Rechts,Hoch,Links,Hoch,Rechts,Hoch,Links,Hoch,Rechts,Rechts,Rechts,Runter,Links,Runte
r,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Runter,Runter,Recht
s,Hoch,Rechts,Runter,Rechts,Hoch,Hoch,Links,Hoch,Rechts,Hoch,Links,Hoch,Rechts,Hoch,Links,
Hoch,Rechts,Hoch,Links,Hoch,Rechts,Rechts,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Run
ter,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Runter,Rechts,Runter,Links,Links,

2) Eingabe:
11
6,6,
120
1,1,
1,2,
1,3,
1,4,
1,5,
1,6,
1,7,
1,8,
1,9,
1,10,
1,11,
2,1,
2,2,
2,3,
2,4,
2,5,
2,6,
2,7,
2,8,

Ausgabe:
Links,Rechts,Hoch,Runter,Links,Links,Hoch,Rechts,Links,Rechts,Links,Hoch,Rechts,Rechts,Links
,Rechts,Links,Hoch,Links,Hoch,Rechts,Hoch,Links,Rechts,Links,Rechts,Rechts,Runter,Runter,Hoc
h,Runter,Hoch,Links,Links,Runter,Links,Links,Links,Rechts,Links,Hoch,Rechts,Hoch,Links,Recht
s,Links,Rechts,Rechts,Runter,Links,Rechts,Links,Runter,Runter,Rechts,Runter,Links,Links,Hoch,R
unter,Hoch,Runter,Runter,Rechts,Rechts,Hoch,Hoch,Runter,Links,Runter,Rechts,Runter,Links,Link
s,Runter,Rechts,Links,Rechts,Hoch,Links,Runter,Runter,Rechts,Rechts,Hoch,Runter,Links,Rechts,
Hoch,Rechts,Rechts,Hoch,Rechts,Links,Links,Rechts,Rechts,Links,Runter,Rechts,Runter,Links,Lin
ks,Rechts,Links,Rechts,Rechts,Runter,Runter,Links,Links,Hoch,Rechts,Links,Links,Links,Links,Re
chts,Links,Runter,Rechts,Rechts,Links,Hoch,Runter,Rechts,Rechts,Hoch,Rechts,Runter,Rechts,Rec
hts,Rechts,Hoch,Links,Rechts,Rechts,Links,Links,Rechts,Runter,Rechts,Rechts,Rechts,Links,Hoch,
Rechts,Runter,Links,Rechts,Hoch,Hoch,Hoch,Links,Runter,Hoch,Rechts,Hoch,Links,Runter,Hoch,
Runter,Runter,Links,Hoch,Links,Links,Rechts,Runter,Links,Hoch,Runter,Hoch,Rechts,Rechts,Hoc
h,Links,Links,Rechts,Links,Rechts,Hoch,Links,Links,Rechts,Hoch,Rechts,Rechts,Runter,Hoch,Hoc
h,Links,Links,Rechts,Links,Rechts,Runter,Rechts,Runter,Rechts,Rechts,Hoch,Links,Rechts,Links,
Hoch,Rechts,Runter,Runter,Hoch,Hoch,Hoch,Hoch,Links,Runter,Hoch,Rechts,Hoch,Links,Runter,
Hoch,Runter,Runter,Links,Links,Links,Rechts,Links,Hoch,Rechts,Rechts,Hoch,Links,Links,Rechts
,Links,Rechts,Rechts,Runter,Links,Runter,Links,
3) Eingabe:

    1,3,
    1,4,
    1,5,
    1,6,
    1,7,
    1,8,
    1,9,
    1,10,
    2,1,
    2,2,
    2,3,
    2,4,
    2,5,
    2,6,
    2,7,
    2,8,
    2,9,
    2,10,
    3,1,
    3,2,
    3,3,
    3,4,
    3,5,
    3,6,
    3,7,
    3,8,
    3,9,
    3,10,
    4,1,
    4,2,
    4,3,
    4,4,
    4,5,
    4,6,
    4,7,
    4,8,
    4,9,
    4,10,
    5,1,
    5,2,
    5,3,
    5,4,
    5,5,
    5,6,
    5,7,
    5,8,
    5,9,
    5,10,
    6,1,
    6,2,
    6,3,
    6,4,
    6,5,
    6,6,
    6,7,
    6,8,
    6,9,
    6,10,
    7,1,
    7,2,
    7,3,
    7,4,
    7,5,
    7,6,
    7,7,
    7,8,
    7,9,
    7,10,
    8,1,
    8,2,
    8,3,
    8,4,
    8,5,
    8,6,
    8,7,
    8,8,
    8,9,
    8,10,
    9,1,
    9,2,
    9,3,
    9,4,
    9,5,
    9,6,
    9,7,
    9,8,
    9,9,
    9,10,
    10,1,
    10,2,
    10,3,
    10,4,
    10,5,
    10,6,
    10,7,
    10,8,
    10,9,
    10,10,
    2,9,
    2,10,
    2,11,
    3,1,
    3,2,
    3,3,
    3,4,
    3,5,
    3,6,
    3,7,
    3,8,
    3,9,
    3,10,
    3,11,
    4,1,
    4,2,
    4,3,
    4,4,
    4,5,
    4,6,
    4,7,
    4,8,
    4,9,
    4,10,
    4,11,
    5,1,
    5,2,
    5,3,
    5,4,
    5,5,
    5,6,
    5,7,
    5,8,
    5,9,
    5,10,
    5,11,
    6,1,
    6,2,
    6,3,
    6,4,
    6,5,
    6,7,
    6,8,
    6,9,
    6,10,
    6,11,
    7,1,
    7,2,
    7,3,
    7,4,
    7,5,
    7,6,
    7,7,
    7,8,
    7,9,
    7,10,
    7,11,
    8,1,
    8,2,
    8,3,
    8,4,
    8,5,
    8,6,
    8,7,
    8,8,
    8,9,
    8,10,
    8,11,
    9,1,
    9,2,
    9,3,
    9,4,
    9,5,
    9,6,
    9,7,
    9,8,
    9,9,
    9,10,
    9,11,
    10,1,
    10,2,
    10,3,
    10,4,
    10,5,
    10,6,
    10,7,
    10,8,
    10,9,
    10,10,
    10,11,
    11,1,
    11,2,
    11,3,
    11,4,
    11,5,
    11,6,
    11,7,
    11,8,
    11,9,
    11,10,
    11,11,
    3,5,
    6,4,
    5,12,
    6,2,

Ausgabe:

4) Eingabe:
100
40,25,20
0

Ausgabe:
Links,Rechts,Links,Rechts,Links,Rechts,Links,Rechts,Links,Rechts,Links,Rechts,Links,Rechts,Lin
ks,Rechts,Links,Rechts,Links,Rechts,

5) Eingabe:

20
10,15,20
34
14,15,10
18,15,4
18,18,2
18,20,10
14,9,2
5,7,2
5,5,2
5,3,2
4,4,2
4,6,2
4,8,1
4,9,5
3,8,1
2,8,1
1,8,1
1,9,1
1,10,1
1,11,1
1,12,3
1,13,1
1,14,1
1,15,1
1,16,1
1,17,1
1,18,1
2,17,1
2,18,3
2,19,1
3,17,1
3,18,1

3,19,1
4,17,1
4,18,1
4,19,1

Ausgabe:

17. Wichtigste Teile des Quellcodes

Grundlegend sei gesagt, dass jede Methode mittels Javadoc. im Quellcode ausführlich dokumentiert
wurde. Sollten sich hier bei den wichtigsten Codeabschnitten fragen ergeben, kann im Quellcode
nachgeschaut werden. Die grafische Oberfläche wurde mit dem JavaFX Framework programmiert.
Dies setze ich mit dem MVC(Model-View-Controller) Prinzip um. Die FXML-Datei wurde dabei
mit dem externen Programm Scenebuilder bearbeitet.
Der Button: „Lade Datei" ist mit der Methode ladeDatei in der Controller Klasse: GuiController
verbunden. Nach dem Aufrufen der Methode wird mithilfe des FilChooser
FileChooser dateiAuswaehlen = new FileChooser();
File eingabe = dateiAuswaehlen .showOpenDialog(stage);
eine Beispieldatei eingelesen und der setup-Methode: Stromrallye. startHeuristik übergeben_._
Diese liest den Inhalt der Datei und wertet entsprechend ihre Daten aus. Mit:
initMap2d = new int [ Mapsize ][ Mapsize ];

wird wie schon in Modellierung der Knoten angedeutet, das Spielfeld als 2D Array gespeichert. Die
Batterien werden in der ArrayListe: bList gespeichert. Eine Batterie ist durch die Klasse Battery
wie folgt aufgebaut:
public Battery( int x, int y, int energy, int id, int sscid, boolean isDone, int
geoGroup)
Gleichzeigt beim Initialisieren der Batterien wird auch entsprechende initMap2d die ID
zugewiesen: initMap2d [battery.getY()][battery.getX()] = i;
Nachdem nun alles wichtige ausgewertet, gespeichert und auf Richtigkeit überprüft wurde, werden
die in Generieren von Kanten besprochenen Kanten- bzw. LinkedListen:
adj = new LinkedList[bSize + 1];
externList = new LinkedList[bSize + 1];
initialisiert. Mit der Methode GenerateEdges. genPosEdges werden nun alle Kanten mittels einer
Breitensuche ermittelt. Die Besonderheit dieser Methode ist die Überprüfung auf Expendable, also
auf die ganzen Ausnahmen, welche die Expandierfähigkeit der Kante bestimmen. Dies wird
einerseits mit if-Abfragen, ob der aktuelle Knoten schon mal gefunden wurde(mit evtl. nur einem
Schritt, was also auf Expandierfähigkeit schließen lässt, weil beim zweiten Fund es mindestens 3
Schritte bedarf) oder bei v→ v bzw. e(v,v), wie viel Schritte es zu dem Ausgangsknoten bedarf. Jede
Abfrage ist von weiteren Arrayboundsabfragen, wie z.B. if (( int )p[1] != (size - 1))
für die Aktion Runter begleitet, da man natürlich nicht über der Array greifen will/darf. Die
Breitensuche expandiert radial und fügt jeden neuen Node, dargestellt als Object[] posBattery
einer Warteschlange: Queue<Object[]> queue = new LinkedList<>(); hinzu. posBattery
besteht dabei aus 0 : x | 1 : y | 2 : Schritte | 3 : Weg.
Die ermittelten Kanten, welche durch die Klasse Kante wie folgt aufgebaut sind:
public Kante(int von, int nach, int kosten, boolean isExpendable)
werden anschließend adj hinzugefügt. Anschließend wird mit SCC. generate die Strongly
Connected Components, nach dem Algorithmus von Tarjan^15 ermittelt. Dabei werden diese global
in dem Array: ArrayList<List> SCC = new ArrayList<List>();
gespeichert. Folgend wird die in SZK und Geo-Gruppen beschriebene Distanzmatrix mit
SCC. genEdgesSCC ,welche wie GenerateEdges. genPosEdges eine Breitensuche ist, für jede SCC
Gruppe als int [][] SCCmatrix ; gespeichert. Letztlich für den Setup wird mit:
int z = GenerateEdges. BFSSSC ( SCCmatrix , sumCalc, maxList .get(destination)[1],
SCC. SCC .size(),destination);
best = (z > best)? z : best;
den maximalen Energiewert ermitteln und mit:
GenerateEdges. genPosEdges ( initMap2d , Mapsize , posBatt2, best,
bList .get(SCC. SCC .get(destination).get(i)).getId(), externList );
die Externeliste erstellt. Wieder in GuiController.ladeDatei wird mit
drawField(Stromrallye. Mapsize ); drawBatt(Stromrallye. Mapsize );
Das grafische Feld im GUI erstellt. Der Button Start, ruft nun die Methode startAgenten auf,
welche das Heuristische Verfahren startet. Die Methoden Helper.generateUnEvenMap und
Helper. generateEvenMap sind die im Abschnitt Optimierung der Gruppierung beschriebenen
Clusterverfahren. Grundlegend wird mit Schleifen wie:
for ( int i = minSize; i < size; i = i + 3) {
die Abschnitte durchsucht. Mit:
Stromrallye. bList .get(initMap2d[x][i]).setGeoGroup(geoGruppe);
wird der Batterie die entsprechende Gruppe zugewiesen. Die Anzahl an Gruppen, spiegelt sich in
der Ganzzahl Stromrallye. geoGruppe wieder. Die ist die Vorverarbeitung beendet und die
Heuristik InformedHeuristik13. initGraph wird aufgerufen. Der in
OpenList/Zustandsspeicherung beschriebene Zustand, wird als Objektarray
Object[] agent dargestellt. Der beschriebene Bergsteigeralgorithmus manifestiert sich durch:

15 https://en.wikipedia.org/wiki/Tarjan
%27s_strongly_connected_components_algorithm

ArrayList<PriorityQueue<Object[]>> Bergsteiger mit:
PriorityQueue<Object[]> OpenLists
Die schon vorgestellt Kostenfunktion vergleicht mit einer Costume Comparator Methode:
sortPriorityQueue
return (Helper. isGeoDone ((ArrayList) arg0[4], Stromrallye. geoGruppe ) <
Helper

. isGeoDone ((ArrayList) arg1[4], Stromrallye. geoGruppe ))? -1
: ( sccGroup == ((ArrayList) arg0[4]).get(( int )
arg0[0]).getGeoGroup())? -1 : 1;
die Zustände. Helper.isGeoDone testet dabei lediglich wie viel Gruppen des Clusterverfahrens
abgearbeitet wurden und gibt den entsprechenden integer Wert zurück. Anschließend wird mit:
for ( int i = 0; i < Stromrallye. adj [0].size(); i++) {
Kante k = Stromrallye. adj [0].get(i);
int x = (k.isExpendable())? ( initAgentBatt ) : k.getKosten() + 1;
for ( int iy = k.getKosten(); iy <= x; iy = iy + 2) {
jede Kante, einschließenlich deren Expandierte Form(en) durchgegangen und nach einer
Überprüfung nach dem SCC-DeadEnd Verfahren, wie beschrieben überprüft. Die Methode
addQueueItem(k.getNach(), newBatt, initMaxBattery - iy, (LinkedList[])
agent[3], (ArrayList) agent[4], ( int [][]) agent[5], "0-" + k.getNach() +
"-" + iy + ",", iy, stageID );
ist dazu Zuständig, dass der gültige Zustand zum entsprechenden PQ-Level^16 hinzugefügt wird.
Dies wird grundlegend in kleiner Abänderung in startHeuristik für jeden Zustand wiederholt,
bis ein Ergebnis oder keines gefunden wurde. Um nicht in einer Suche ewig stecken zu bleiben,
wird, dass beschriebene Beamlimit mit:
beamSearch ++;
if ( beamSearch == beamLimit ) {
beamSearch = 0;
break BeamSearch;
}
benutzt. Damit jeder gültiger Zustand von der Heuristik überprüft wird, gibt es die Methode:
public static boolean testfkt() {
welche alle Stufen des Bergsteigeralgorithmus überprüft. Wurde ein Weg gefunden, wird er in dem
String: Bewegungsabfolge in Stromrallye gespeichert, wo er dann grafisch bzw. als Aktion
umgesetzt wird. Dies gehört jedoch eher zur Implementierung der grafischen Oberfläche, was für
die Implementierung des Aufgabenteiles an sich irrelevant ist.

2.b) Lösungsidee/Abstrakt:

1. Abstrakt

In diesem Teil der Aufgabenstellung, geht es darum, eigene lösbare Spielfelder für den Benutzer zu
erschaffen. Dieser kann die Schwierigkeit des Feldes anhand der Checkboxen(rechtsoben)
auswählen. Diese Unterscheiden sich untereinander, anhand von verschiedenen Attributen und
Faktoren. Es gilt, dass der Benutzer den Roboter mithilfe der Tastatur, aber zudem auch mit den
Pfeil-Buttons steuern kann.

2. Definition

Es gibt 4 verschiedene Schwierigkeitsstufen, welche: „Leicht, Mittel, Schwer und Sehr Schwer"
lauten. Jede Schwierigkeit soll anhand von Parametern definiert werden, welche im Abschnitt
Parameter beschrieben werden. Die Häufigkeit dieser, ist stellvertretend für den Grad an
Schwierigkeit. Die Komplexität ist zudem Äquivalent zu dem Zeitaufwand, der zum Lösen dieses
Feldes benötigt wird.

16 Oder auch Station

3. Paramter

1) Bildet die Größe des Spielfeldes. Die Größe ist zwar unabhängig von der Schwierigkeit, jedoch
dient sie hier der Übersichtswillen als Schwierigkeit. Verbunden mit anderen Parametern, wird
ersichtlicher, warum dies nichtnur der Dichtenswillen wichtig ist.
Leicht: 5-10
Mittel: 7-13
Schwer: 8-16
Sehr Schwer: 9-22
→ Dabei wird eine Größe in dem Intervall zufällig mit gleicher Gewichtung für jede Zahl bestimmt.

2) Mindestanzahl an Batterien auf dem Spielfeld.
Leicht: 4
Mittel: 6
Schwer: 7
Sehr Schwer: 8
→ Dieser Parameter hängt unmittelbar mit 3) und 4) zusammen.

3) Prozentzahl, zu wie viel Prozent noch eine weitere Batterie generiert werden soll. Dieser
Parameter tritt jedoch erst nach der Mindestanzahl von 2) in Kraft.
Leicht: 70%
Mittel: 75%
Schwer: 80%
Sehr Schwer: 100%
→ Hat zur Folge, dass je schwerer es wird, desto mehr Batterien, mit eine erhöhten
Wahrscheinlichkeit einer Komplexität können generiert werden.

4) Gegenstück zum dritten Parameter, er ist die Prozentanzahl, wie viel pro weiterer Batterie von
dem dritten Parameter abgezogen werden soll.
Leicht: 30%
Mittel: 20%
Schwer: 12%
Sehr Schwer: 7%
→ Dies hat natürlich zur Folge, dass nicht das ganze Feld mit je einer Batterie bedeckt ist. Jedoch
wurde dieser Parameter bewusst eingesetzt, um einseitige Muster(volles Feld) zu vermeiden.

5) Senkt die Möglichkeiten, durch einen Faktor, welcher von 3) Prozente in Relation zu der
generierten Batterienergie abzieht. Der Faktor spiegelt sich in der Formel:
Faktor(3) = (Faktor(3))-(energieFaktor(5)/(sizesize)) wieder.
Leicht: 20
Mittel: 18
Schwer: 15
Sehr Schwer: 5
→ Dies schränkt die Wahrscheinlichkeit ein, dass ein Spielfeld nur reines Herumwandern(riesige
Energienanzahlen) ist und trägt konsequenterweise auch zur Komplexität bei.

6) Bestimmt, zu wie viel Prozent eine Ersatzbatterie mehrmals befahren werden soll.
Leicht: 40
Mittel: 65

Schwer: 75
Sehr Schwer: 120
→ Dies hat nicht nur zur Folge, dass man auch in Betracht ziehen muss, in eine Batterie mehrmals
zu fahren(mehr Möglichkeiten), sondern stellt verbunden mit den Parametern 10) & 11) , nochmals
eine extra Schwierigkeit dar.

7) Prozentzahl, dass Muster in dem Spielfeld entstehen sollen. Dies manifestiert sich bspw. In
gleicher Energienanzahl, zweier folgender Batterien.
Leicht: 30
Mittel: 15
Schwer: 5
Sehr Schwer: 2
→ Dies dient 1. der Leichtigkeitshalber, dass der Benutzer erkennt, welche Ersatzbatterie als
nächstes Befahren werden muss und 2. können dadurch mehrerer Ergebnisse, bzw. Wege zum Ziel
führen.

8) Faktor, die letzte Ersatzbatterie im Vorhinein zu bestimmen, also je höher dieser Faktor ist, desto
höher bzw. niedriger wird die Energie der Ersatzbatterie. Wobei diese natürlich nur optimal ist. Der
Weg ist keinesfalls immer absolut.
Leicht: 3
Mittel: 2
Schwer: 1
Sehr Schwer: 1
→ Die letzte Batterie wird hier durch den Faktor geteilt, was zu dem oben beschriebenen Effekt
führt.

9) Wahrscheinlichkeit, dass eine Ersatzbatterie sich selbst anfährt.
Leicht: 0
Mittel: 15
Schwer: 20
Sehr Schwer: 35
→ Dadurch entstehen noch mehr Möglichkeiten.

10) Wahrscheinlichkeit, dass man auch mehr als nur dass Minimum von v → v' nehmen muss.
Leicht: 50
Mittel: 80
Schwer: 90
Sehr Schwer: 100
→ Stellt vor allem in großen Feldern(schwereren) eine extra Schwierigkeit dar.

11) Maximale Anzahl an extra Schritten.
Leicht: 4
Mittel: 6
Schwer: 8
Sehr Schwer: 10
→ Kann vor allem in kleinen Feldern, eine große Rolle spielen, da verbunden mit 8) sehr viel
Auswahl herrscht.

12) Gegenwert zum Parameter 7) , wie viel Prozent von 3) abgezogen werden soll.
Leicht: 30
Mittel: 20
Schwer: 15
Sehr Schwer: 10
→ Die Anordnung ist hierbei theoretisch egal.

13) Gegenwert zum Parameter 6 ) , wie viel Prozent von 3) abgezogen werden soll.
Leicht: 30
Mittel: 20
Schwer: 20
Sehr Schwer: 10

14) Faktor von Gruppenbildung. Dass wenn große Batterie im Vergleich zum Feld, das dann danach
kleiner, was den Suchraum kleiner und somit einfacher macht.
Leicht: 0.25
Mittel: 0.75
Schwer: 1.25
Sehr Schwer: 2
→ Faktor entsprechend in Relation zur Spielfeldgröße k. Dabei wird 14) immer bei
v.energie >=k*Faktor ausgelöst.

4. Generieren des Feldes

Zur Repräsentation des Feldes wird grundlegend wie im Aufgabenteil a) auch ein 2D Array
verwendet. Ähnlich wie bei den Maze-Generating Algorithmen habe ich mich hier für einen
rückwährtslaufenden Algorithmus entschieden^17. Das Spielfeld wird also rückwärts erstellt. Dies
war vor allem wegen dem Parameter der Mehrfachbetretung einer Batterie von Nöten. Bei dem
rückwährtslaufenden Generieren sind die zu befahrenen Batterien nämlich schon da. Der Array wird
zunächst mit nullen gefüllt, was die leere Map darstellen soll. Wenn der Benutzer nun eine
Schwierigkeit auswählt und auf „Generieren" klickt, werden die Parameter dem Algorithmus
übergeben, welcher anhand diesen anschließend das Feld generiert. Die Position der letzten
Batterie(erste zu generierende) wird anhand einer Zufallsrechnung bestimmt. Anschließend wird
anhand dem Parameter 8) der Batterie die Energie zugewießen. Die Richtungen(Links⋁ Rechts und
Hoch Runter) und Weite werden anhand von zwei weiteren Zufallsrechnungen, in Anbetracht der⋁
genannten Parameter bestimmt. Eine Batterie wird nun auf das Feld gesetzt(GUI) und in dem Array
wird diese Stelle mit einer ID(positive GZ) gekennzeichnet. Da auf den befahrenen Feldern keine
Batterie erstellt werden darf, wird dies in dem Array mit -1, an jeder befahrenen Stelle bemerkt. Die
weiteren Batterien werden nun nach dem gleichen Prinzip und den Faktoren der Parameter erstellt.
Es ist eine rein logische Konsequenz, dass neue Batterien nur auf Positionen des Arrays mit dem
Wert 0 erstellt werden dürfen. Es ist außerdem wichtig zu beachten, dass auf dem Weg zwischen
den Batterien auch andere Batterien sein können. Der Algorithmus guckt nun, in Anbetracht der
Parameter, ob eine anderer Batterie auf dem Weg befahren werden soll(wegen bspw. 6) ). Soll keine
befahren werden(bspw. 6 =false), wird entweder nach einem anderen Weg mithilfe von
Richtungswechsel- oder einer Breitensuche- oder wenn nicht möglich ein neuer Punkt gesucht.
Dabei wird zudem darauf geachtet, dass je Schwierigkeit, z.B. -1, also eine schon befahrene Stelle
befahren wird, dass mehr Batterien bei bedarf generiert werden können. Ist 3) fertig, soll also keine
neue Batterie mehr generiert werden, wird die letzte erstellte Batterie grün markiert(sowie mit der
ID: 1 im Array) und als Startpunkt festgelegt. Während der ganzen Prozedur, wird der gegangene

17 https://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking

Weg gespeichert, um dem Benutzer (siehe GUI ) z.B. einen Tipp zu geben. Der Benutzer kann nun
versuchen, das generierte Spielfeld zu lösen.

5. Darstellung der Lösung

Hat der Benutzer erfolgreich alle Batterien nach den Kriterien der Aufgabe „abgearbeitet", so
erscheint die folgende Nachricht:

Ist dem Roboter die Energie jedoch vorher ausgegangen, wird dem Benutzer die Nachricht:

angezeigt. Bei beiden Fällen kann der Benutzer anschließend den Roboter logischerweise nicht
mehr bewegen und muss das Spielfeld mit dem Knopf: „<---" bzw. „Anfang" zurücksetzen.

6. GUI

Die Grafischoberfläche bietet zudem noch die Option für den Benutzer, sich einen Tipp einzuholen.

Nach Betätigen des Buttons: „Tipp", kommt folgendes Fenster hervor:

7. Beispiele

8. Leicht Eingabe:

Lösung:
Rechts,Runter,Runter,Runter,Rechts,Hoch,Links,Links,Runter,Links

9. Mittel

Eingabe:

Lösung:
Hoch,Links,Links,Links,Links,Runter,Runter,Runter,Hoch,Runter,Hoch,Runter,Rechts,Rechts,Rech
ts,Rechts,Runter,Rechts,Hoch,Hoch,Hoch,Hoch,Links,Links,Rechts,
Rechts,

10. Schwer

Eingabe:

Lösung:
Runter,Runter,Hoch,Runter,Runter,Links,Links,Links,Hoch,Hoch,Runter,Hoch,Runter,Hoch,Runter
,Hoch,Hoch,Hoch,Hoch,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Runter,Runter,
Hoch,Runter,Hoch,Runter,Hoch,Runter,Runter,Runter,Runter,Links,Links,Links,Links,Rechts,
Rechts,Rechts,Rechts,Rechts,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Links,Links,Runter,Runter,
Hoch,Runter,Hoch,Runter,Runter,Runter,Runter,Runter,Runter,Rechts,Runter,Runter,Runter,

11. Sehr Schwer

Eingabe:(Anmerkung: Wie schon in Aufgabenteil a) gesagt, gibt es hierfür die Option: mit der
Maus über die Batterie zu fahren und entsprechend den Wert zu bekommen, falls er wie hier nicht
so gut zu erkennen ist)

Lösung:
Runter,Runter,Hoch,Runter,Runter,Runter,Runter,Runter,Runter,Runter,Links,Links,Links,Links,Li
nks,Links,Links,Links,Links,Links,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Hoc
h,Hoch,Links,Links,Links,Links,Links,Runter,Runter,Hoch,Runter,Hoch,Runter,Hoch,Runter,Runte
r,Runter,Runter,Runter,Links,Links,Hoch,Hoch,Runter,Hoch,Runter,Hoch,Hoch,Hoch,Rechts,Recht
s,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,R
echts,Runter,Links,Rechts,Links,Rechts,Links,Rechts,Links,Links,Links,Links,Links,Links,Links,R
unter,Runter,Hoch,Runter,Hoch,Runter,Hoch,Runter,Runter,Runter,Runter,Hoch,Rechts,Rechts,Rec
hts,Links,Links,Rechts,Links,Rechts,Links,Links,Links,Links,Links,Links,Links,Links,Hoch,Hoch,
Hoch,Hoch,Hoch,Hoch,Hoch,Hoch,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rechts,Rec
hts,Rechts,Rechts,Rechts,Rechts,

12. Wichtigste Teile des Quellcodes

Mit der Methode genField wird eine neue 2D Map angelegt: int [][] map und die Schwierigkeit
wird anhand von Abfragen überprüft und anschließend werden die entsprechenden Parameter
der Methode generateField übergeben. Diese übernimmt die ersten Schritte, das Feld zu
initialisieren/zeichnen und errechnet mit:
int posX = ThreadLocalRandom. current ().nextInt(0, size);
int posY = ThreadLocalRandom. current ().nextInt(0, size);
den Start und mit
int randX = ThreadLocalRandom. current ().nextInt((-posX / hardlastNode), (((size

    posX) - 1) / hardlastNode));
    int randY = ThreadLocalRandom. current ().nextInt((-posY / hardlastNode), (((size
    posY) - 1) / hardlastNode));
    den Endpunkt. hardlastNode ist dabei der Faktor 8). mit
    while (randX == 0 && randY == 0)wird schließlich noch garantiert, dass die Endposition nicht
    die Startposition darstellt(Weil sonst die Energie = 0 wäre, was zu einem ungültigen Feld führen
    würde). Wenn randX = negativ, dann heißt dass, dass der Roboter nach links geht. Bei positiv nach
    Rechts. Bei randY dasselbe. Mit map[posX + randX][i] = -1;wird sichergestellt, dass an dieser
    Stelle, wie schon erläutert keine neue Batterie generiert wird. Nachdem nun die erste Batterie auf
    dem Feld ist, wird die Methode generateBatts aufgerufen, welche die restlichen Batterien anhand
    der Faktoren und Parameter generiert. Alle Wahrscheinlichkeiten werden mit der Random random =
    new Random(); Funktion erstellt. Dabei wird eine „zufällige" Zahl perc =
    random.nextInt(100); zwischen 0-100 generiert. Diese wird dann, wie bspw.
    if (perc > percnotDone)mit dem entsprechenden Parameter verglichen. Jede neue Batterie wird
    nach dem oben genannten Prinzip erstellt. Ist jedoch einmal eine andere Batterie im Weg, wird bei
    gegebenen Parameter ein alternativer Weg gesucht, mit der Methode testValid oder wenn 6) dann
    wird die im Weg stehende Batterie befahren und entsprechend ihre Batterie mit dem Weg von
    v→ v' aktualisiert.
    bListinit .get(Helper. findBattbyKoord ((posX + randX), i,
    bListinit )).setEnergy(energie);. Die sich in der im Weg befindene Energie wird
    anschließend in dem Integer energieholder bewahrt mit
    energieholder = Integer. parseInt (batterieText.getText());und den noch zu machenden
    Schritten, bis zur Zielposition hinzuaddiert. Wenn die minimale Anzahl an Batterien 2)
    überschritten und Parameter 3) sagt, dass keine neuen Batterien mehr generiert werden sollen, wird
    die letzte Batterie durch
    Ellipse e = (Ellipse) getBatt( letztery , letzterx , map2D, 2);
    e.setFill(Color. GREEN );
    und
    Battery agent = new Battery( letzterx , letztery , Integer. parseInt (t.getText()),
    0, 0, false , 0); bListinit .set(0, agent);
    zum Robot gemacht. Der Finale, bzw. von dem Algorithmus verwendete Weg, wird in dem String:
    Bewegungsabfolge gespeichert. Da das Spielfeld rückwärts erstellt wird, werden die
    Bewegungsrichtungen vor dem String gepackt, wie z.B. bei einer Horizontalen Aktion:
    Bewegungsabfolge = ((randX < 0)? ",Rechts" : ",Links") + Bewegungsabfolge ;
    Der Roboter kann grundlegend mit den Funktionen geheHoch(), geheRunter(), geheRechts(),
    geheLinks() vom User per Key Event: Main. scene .setOnKeyPressed gesteuert werden. Auch
    hier gilt, dass die restlichen Methoden zur GUI-Verwaltung eher weniger mit der aktuellen
    Aufgabenstellung zu tun haben, weshalb hier nicht mehr weiter darauf eingegangen wird.(Die
    Methoden sind jedoch trotzdem alle kommentiert, siehe Javadoc. bzw. Methodendoc.)

