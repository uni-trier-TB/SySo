# Syso Übung 1

## Aufgabe 1
Eine Anwendung besteht aus einem oder mehreren Prozessen. 
Ein Prozess ist in den einfachsten Worten ein ausführendes Programm. 
Mindestens ein Thread wird im Kontext des Prozesses ausgeführt. 
Ein Thread ist die Basiseinheit, der das Betriebssystem Prozessorzeit zuordnet.
Ein Thread kann einen beliebigen Teil des Prozesscodes ausführen,
einschließlich der Teile, die derzeit von einem anderen Thread ausgeführt werden.
https://learn.microsoft.com/de-de/windows/win32/procthread/processes-and-threads
## Aufgabe 2

Die Formel:
S = 1 / ((1-P) + P/N )

- \( S \) Speedup
- \( P \) parallelisierbare Anteil
- \( N \) Anzahl Kerne
- \( 1 - P \) sequenzielle Teil


#### 1. Parallelisierbarer Anteil \( P = 0.25 \ N = [1, 2, 4, 8])

S = 1 / ((1-0.25) + 0.25/1 ) 1.33

S = 1 / ((1-0.25) + 0.25/2 ) = 1.14

S = 1 / ((1-0.25) + 0.25/4 ) = 1.07

S = 1 / ((1-0.25) + 0.25/8 ) = 1.03
#### 2. Parallelisierbarer Anteil \( P = 0.50 \ N = [1, 2, 4, 8])

S = 1 / ((1-0.50) + 0.50/1 ) 1.00

S = 1 / ((1-0.50) + 0.50/2 ) = 0.80

S = 1 / ((1-0.50) + 0.50/4 ) = 0.71

S = 1 / ((1-0.50) + 0.50/8 ) = 0.61

#### 3. Parallelisierbarer Anteil \( P = 0.75 \ N = [1, 2, 4, 8])

S = 1 / ((1-0.75) + 0.75/1 ) 1.00

S = 1 / ((1-0.75) + 0.75/2 ) = 0.67

S = 1 / ((1-0.75) + 0.75/4 ) = 0.57

S = 1 / ((1-0.75) + 0.75/8 ) = 0.54

## Aufgabe 3

file = SySo

Total number of lines in all files: 1381
0.06262946128845215
Number of Threads:  1

Total number of lines in all files: 1383
0.0581817626953125
Number of Threads:  2

Total number of lines in all files: 1383
0.05747842788696289
Number of Threads:  3

Total number of lines in all files: 1383
0.05120444297790527
Number of Threads:  4


