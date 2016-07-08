SKGB-offline
============

Der verschlüsselte Austausch von Kontodaten übers Netz hat sich als durchführbar, aber aufwändig und teils kompliziert erwiesen. Daher bereitet [SKGB-intern](http://intern1.skgb.de) die Lastschriftlisten (Sammelaufträge) nur mit allen anderen nötigen Angaben vor (alles bis auf die Kontodaten). Anschließend müssen offline auf dem eigenen Rechner die Kontodaten aus der Sammlung der SEPA-Mandate herausgesucht und eingefügt werden. Es läuft also auf einen simplen `JOIN` der folgenden zwei Tabellen hinaus:

- **Lastschriftliste** (ohne Kontodaten, aber mit allen Beträgen etc. und mit der Mandatsreferenz)
- **Mandatssammlung** (Kontodaten, sortiert nach Mandatsreferenz)

Verhältnismäßig einfach ließe sich dies mit gängigen Tabellenkalkulationen durchführen. Um es noch einfacher und weniger fehleranfällig zu machen, begonn der IT-Ausschuss die Entwicklung einer kleinen App, die genau diese Aufgabe übernimmt; Arbeitstitel „SKGB-offline“.

- [Versionsgeschichte](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/changelog_page.php) lesen
- [ältere Versionen](http://intern1.skgb.de/digest/temp-offline/?C=M;O=D) laden (in SKGB-intern)
- [geplante Features](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/roadmap_page.php) ansehen
- [Liste bekannter Probleme / Bugtracker](http://intern1.skgb.de/bugs/set_project.php?project_id=14&ref=/bugs/view_all_bug_page.php)
- [API-Dokumentation / Javadoc](http://intern1.skgb.de/temp-offline-doc/) (unvollständig)
