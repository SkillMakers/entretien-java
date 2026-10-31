# Entretien Java : les questions et les réponses attendues

Le code des vidéos « Entretien Java » de la chaîne [ItSkillsMaker](https://www.youtube.com/@ItSkillsMaker).
Chaque piège montré dans la vidéo est prouvé par un test JUnit : lance-les, puis casse-les.

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
la question. Les questions sans code (1, 5, 7, 12, 15, 20, 23) sont expliquées dans la vidéo.
