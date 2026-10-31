# Mesures de la vidéo « Entretien Java senior »

Relevées le 31/10/2026 avec `mvn test`, sur Temurin 25.0.5, Windows 11, 8 processeurs logiques.
Les tests affichent ces valeurs : relance-les, tes chiffres seront différents, l'ordre de grandeur non.

## Question 1 : `count++` sur un champ `volatile`, 4 threads × 100 000 incréments

| Exécution | `count++` | `AtomicInteger` |
|---|---|---|
| 1 | 184 510 | 400 000 |
| 2 | 168 662 | 400 000 |
| 3 | 172 641 | 400 000 |
| 4 | 171 544 | 400 000 |
| 5 | 164 644 | 400 000 |

**Plus de la moitié des incréments perdus** (54 à 59 %), et un résultat différent à chaque fois.
`volatile` ne rend pas `count++` atomique (question 2). Le test `compteurPartage` affiche cette
valeur sans la vérifier, puisqu'elle n'est pas prévisible ; c'est `incrementPerdu` qui prouve la
perte, en forçant l'entrelacement de deux threads.

## Question 9 : 1 000 tâches qui attendent 100 ms

| Exécution | Pool de 10 threads | Threads virtuels |
|---|---|---|
| 1 | 10 066 ms | 152 ms |
| 2 | 10 071 ms | 150 ms |
| 3 | 10 066 ms | 144 ms |
| 4 | 10 069 ms | 135 ms |
| 5 | 10 065 ms | 150 ms |

Le pool fait 100 vagues de 10 tâches, donc au moins 10 s. Les threads virtuels attendent tous en
même temps : **~70 fois plus rapide**, sur une tâche qui attend.

## Question 13 : profondeur avant `StackOverflowError`

38 213, 20 924, 21 645, 16 006, 11 353 appels selon l'exécution. La taille de la pile est fixe,
mais la place prise par chaque appel dépend de l'état de la méthode (interprétée, ou déjà compilée
par le JIT) : la profondeur atteinte varie.
