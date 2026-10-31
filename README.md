# Entretien Java : les questions et les réponses attendues

Le code des vidéos « Entretien Java » de la chaîne [ItSkillsMaker](https://www.youtube.com/@ItSkillsMaker).
Les pièges montrés dans la vidéo sont prouvés par des tests JUnit : lance-les, puis casse-les.

| Dossier | Vidéo |
|---|---|
| [`junior/`](junior) | Entretien Java : 30 questions et les réponses attendues (Java 25) |
| `senior/` | Entretien Java senior (à venir) |

## Prérequis

**JDK 25** (les tests utilisent `release 25`, et la question 30 `void main()` et `IO.println`,
définitifs depuis Java 25). Maven 3.9.

```bash
cd junior
mvn test
java q30/Bonjour.java
```

Avec un JDK plus ancien, la compilation échoue sur `release 25` : vérifie `java -version`.

## Une question, un test

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
