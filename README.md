# Entretien Java : les questions et les réponses attendues

Le code des vidéos « Entretien Java » de la chaîne [ItSkillsMaker](https://www.youtube.com/@ItSkillsMaker).
Les pièges montrés dans la vidéo sont prouvés par des tests JUnit : lance-les, puis casse-les.

| Dossier | Vidéo |
|---|---|
| [`junior/`](junior) | Entretien Java : 30 questions et les réponses attendues (Java 25) |
| [`senior/`](senior) | Entretien Java senior : 30 questions et les réponses attendues |

## Prérequis

**JDK 25** : les deux projets compilent en `release 25` (junior, question 30 : `void main()` et
`IO.println` ; senior : `ScopedValue` et le code avant `super()`, définitifs depuis Java 25). Maven 3.9.

```bash
cd junior && mvn test
cd ../senior && mvn test
```

Avec un JDK plus ancien, la compilation échoue sur `release 25` : vérifie `java -version`.

## Senior : les chiffres de la vidéo

Les tests de `senior/` affichent leurs mesures (compteur partagé, threads virtuels, profondeur de pile).
Celles de la vidéo sont dans [`senior/MESURES.md`](senior/MESURES.md). Le test des threads virtuels
dure environ 10 secondes : c'est le pool de 10 threads qu'il chronomètre.

Sans test dans `senior/` :

| Question | Pourquoi |
|---|---|
| 2 | volatile et synchronized : question de cours (le test de la question 1 utilise un champ `volatile`) |
| 10, 11 | Quand éviter les threads virtuels, pinning : [JEP 444](https://openjdk.org/jeps/444), [JEP 491](https://openjdk.org/jeps/491) (Java 24) |
| 14 | Garbage collectors : [JEP 248](https://openjdk.org/jeps/248) (G1 par défaut), [JEP 474](https://openjdk.org/jeps/474) (ZGC générationnel) |
| 16 | Diagnostic d'une OutOfMemoryError : options de la JVM, `jcmd`, Java Flight Recorder |
| 22, 26, 28, 29, 30 | Questions de méthode, sans code |

## Junior : une question, un test

Les fichiers sont nommés par questions : `Q12_14_EqualsHashCodeTest` couvre les questions 12 à 14.
Chaque test suit le schéma `GIVEN / WHEN / THEN`, et son `@DisplayName` commence par le numéro de
la question. La question 30 est testée en lançant `java q30/Bonjour.java` depuis le test.

Sans test :

| Question | Pourquoi |
|---|---|
| 1, 5, 7, 12, 15, 20, 23 | Questions de cours, sans code |
| 4 | `finalize()` : `@Deprecated(since="9", forRemoval=true)` dans `java/lang/Object.java` du JDK 25 |
| 11 | Concaténation sur une ligne optimisée par le compilateur : [JEP 280](https://openjdk.org/jeps/280) |
| 16 | Constantes de `java/util/HashMap.java` du JDK 25 : 16 cases, facteur 0,75, arbre à partir de 8 éléments par case (table d'au moins 64) |

`mvn test` affiche aussi les deux avertissements du compilateur montrés dans la vidéo
(`[overrides]` pour la question 13, `[finally]` pour la question 21).
