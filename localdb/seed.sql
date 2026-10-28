-- ReadNest – Beispieldaten für die lokale SQLite-Datenbank
-- {{MEDIEN}} wird von setup.sh durch die file://-URL des Medienordners ersetzt.
-- ISBNs sind Platzhalter.

-- Logins: admin / admin (Administrator), leser / leser
INSERT INTO Nutzer (NutzerID, Nutzername, Passwort) VALUES
    (1, 'admin', '$2a$10$dZxNdvhjmL3roEG2DHhFVeS7.7rGlC0zVWZR4izhmzvlFlcEqpWAC'),
    (2, 'leser', '$2a$10$g4KT7iISDus.OVVJPmp8N.mkaW.CKGyJf1hBgfAvOTBDYD3X3fEtO');

INSERT INTO Administrator (NutzerID) VALUES (1);

INSERT INTO Autor (AutorID, Name) VALUES
    (1, 'Franz Kafka'),
    (2, 'Johann Wolfgang von Goethe'),
    (3, 'Theodor Fontane'),
    (4, 'E. T. A. Hoffmann'),
    (5, 'Theodor Storm');

INSERT INTO Verlag (VerlagsID, Bezeichnung) VALUES
    (1, 'Nestverlag'),
    (2, 'Federkiel Bücher'),
    (3, 'Lesezeichen Edition');

INSERT INTO Buch (BuchID, Buch, ISBN, AutorID, VerlagsID, Veroeffentlichungsdatum, Seitenanzahl, Buchdateipfad, Vorschaubildpfad) VALUES
    (1, 'Die Verwandlung',                 '978-3-00-000001-0', 1, 1, '1915-10-01', 5, '{{MEDIEN}}/verwandlung.pdf',    '{{MEDIEN}}/verwandlung.png'),
    (2, 'Faust. Der Tragödie erster Teil', '978-3-00-000002-0', 2, 2, '1808-04-01', 5, '{{MEDIEN}}/faust.pdf',          '{{MEDIEN}}/faust.png'),
    (3, 'Die Leiden des jungen Werthers',  '978-3-00-000003-0', 2, 2, '1774-09-29', 5, '{{MEDIEN}}/werther.pdf',        '{{MEDIEN}}/werther.png'),
    (4, 'Effi Briest',                     '978-3-00-000004-0', 3, 3, '1896-01-01', 5, '{{MEDIEN}}/effi.pdf',           '{{MEDIEN}}/effi.png'),
    (5, 'Der Sandmann',                    '978-3-00-000005-0', 4, 1, '1816-01-01', 5, '{{MEDIEN}}/sandmann.pdf',       '{{MEDIEN}}/sandmann.png'),
    (6, 'Der Schimmelreiter',              '978-3-00-000006-0', 5, 3, '1888-04-01', 5, '{{MEDIEN}}/schimmelreiter.pdf', '{{MEDIEN}}/schimmelreiter.png');

INSERT INTO Beschreibung (BuchID, Text) VALUES
    (1, 'Gregor Samsa erwacht eines Morgens als ungeheures Ungeziefer und wird für seine Familie zur Last.'),
    (2, 'Der Gelehrte Faust schließt einen Pakt mit Mephisto, um zu erkennen, was die Welt im Innersten zusammenhält.'),
    (3, 'Briefroman über die unglückliche Liebe des jungen Werther zu Lotte.'),
    (4, 'Die junge Effi wird mit dem deutlich älteren Baron von Innstetten verheiratet – mit tragischen Folgen.'),
    (5, 'Nathanael wird von einer Kindheitserinnerung an den unheimlichen Sandmann verfolgt.'),
    (6, 'Deichgraf Hauke Haien kämpft gegen die Nordsee und den Aberglauben seines Dorfes.');

-- Zwei Exemplare pro Buch
INSERT INTO Exemplar (ExemplarID, BuchID) VALUES
    (1, 1), (2, 1),
    (3, 2), (4, 2),
    (5, 3), (6, 3),
    (7, 4), (8, 4),
    (9, 5), (10, 5),
    (11, 6), (12, 6);

-- leser hat "Effi Briest" aktuell ausgeliehen, "Die Verwandlung" ist bereits zurückgegeben
INSERT INTO Verleihung (NutzerID, ExemplarID, Ausleihdatum, BisDatum) VALUES
    (2, 7, datetime('now', 'localtime', '-3 days'),  date('now', 'localtime', '+11 days')),
    (2, 1, datetime('now', 'localtime', '-30 days'), date('now', 'localtime', '-16 days'));
