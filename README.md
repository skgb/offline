SKGB-offline
============

Der verschlüsselte Austausch von Kontodaten übers Netz hat sich als durchführbar, aber aufwändig und teils kompliziert erwiesen. Daher bereitet [SKGB-intern](http://intern1.skgb.de) die Lastschriftlisten (Sammelaufträge) nur mit allen anderen nötigen Angaben vor (alles bis auf die Kontodaten). Anschließend müssen offline auf dem eigenen Rechner die Kontodaten aus der Sammlung der SEPA-Mandate herausgesucht und eingefügt werden. Es läuft also auf einen simplen `JOIN` der folgenden zwei Tabellen hinaus:

- **Lastschriftliste** (ohne Kontodaten, aber mit allen Beträgen etc. und mit der Mandatsreferenz)
- **Mandatssammlung** (Kontodaten, sortiert nach Mandatsreferenz)

Verhältnismäßig einfach ließe sich dies mit gängigen Tabellenkalkulationen durchführen. Um es noch einfacher und weniger fehleranfällig zu machen, begonn der IT-Ausschuss die Entwicklung einer kleinen App, die genau diese Aufgabe übernimmt; Arbeitstitel „SKGB-offline“.


Installation
------------

Ausführbare JAR-Files finden sich unter <https://skgb.github.io/offline-release/>.


Build
-----

Bisher wird schlicht [Apache Ant](https://ant.apache.org/) verwendet. Kompilieren z. B. mit `ant build run` oder `ant all`.


Lizenz
------

[BSD 3-clause](https://github.com/skgb/offline/blob/master/LICENSE)

Verwendete freie Software:

opencsv 2.3  
Copyright 2005 Bytecode Pty Ltd.  
Licensed under the Apache License, Version 2.0

Classic Javadoc StyleSheet 1.2  
Copyright (C) 2015 S.Ishigaki  
Licensed under the MIT license


Beitragen
---------

Wir freuen uns über alle Beiträge! Eröffne einfach ein Issue oder einen Pull Request oder wende dich auf andere Weise an den IT-Ausschuss.


Weitere Informationen
---------------------

- [Versionsgeschichte](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/changelog_page.php) lesen
- [geplante Features](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/roadmap_page.php) ansehen
- [Liste bekannter Probleme / Bugtracker](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/view_all_bug_page.php)
- [API-Dokumentation / Javadoc](http://intern1.skgb.de/temp-offline-doc/) (unvollständig)
