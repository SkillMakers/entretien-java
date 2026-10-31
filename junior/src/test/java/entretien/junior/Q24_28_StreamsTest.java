package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 24 a 28 : capture dans une lambda, paresse, flatMap, Optional, reutilisation d'un Stream. */
class Q24_28_StreamsTest {

    // Question 24 : ceci ne compile pas.
    //   int total = 0;
    //   produits.forEach(p -> total += p.length());
    //   error: local variables referenced from a lambda expression must be final or effectively final
    @Test
    @DisplayName("Q24 - on ne modifie pas une variable locale depuis une lambda : on reduit")
    void captureDansUneLambda() {
        // GIVEN
        List<String> produits = List.of("clavier", "souris");

        // WHEN
        int total = produits.stream().mapToInt(String::length).sum();

        // THEN
        assertEquals(13, total);
    }

    @Test
    @DisplayName("Q25 - sans operation terminale, rien ne s'execute")
    void paresse() {
        // GIVEN
        AtomicInteger appels = new AtomicInteger();
        Stream<String> s = Stream.of("a", "b", "c").map(x -> {
            appels.incrementAndGet();
            return x.toUpperCase();
        });

        // THEN : le map n'a pas encore tourne
        assertEquals(0, appels.get());

        // WHEN
        Optional<String> premier = s.findFirst();

        // THEN : findFirst s'arrete au premier element
        assertEquals(Optional.of("A"), premier);
        assertEquals(1, appels.get());
    }

    record Commande(List<String> articles) {}

    @Test
    @DisplayName("Q26 - map donne des listes, flatMap les aplatit")
    void mapEtFlatMap() {
        // GIVEN
        List<Commande> commandes = List.of(
                new Commande(List.of("clavier", "souris")),
                new Commande(List.of("ecran")));

        // WHEN
        List<List<String>> parCommande = commandes.stream().map(Commande::articles).toList();
        List<String> articles = commandes.stream().flatMap(c -> c.articles().stream()).toList();

        // THEN
        assertEquals(2, parCommande.size());
        assertEquals(List.of("clavier", "souris", "ecran"), articles);
    }

    @Test
    @DisplayName("Q27 - orElse evalue toujours son argument, orElseGet seulement si vide")
    void orElseContreOrElseGet() {
        // GIVEN : un compteur qui simule une requete en base
        AtomicInteger requetes = new AtomicInteger();
        Optional<String> present = Optional.of("trouve");

        // WHEN
        present.orElse(chargerDepuisLaBase(requetes));

        // THEN : la requete a ete faite pour rien
        assertEquals(1, requetes.get());

        // WHEN
        present.orElseGet(() -> chargerDepuisLaBase(requetes));

        // THEN : orElseGet ne l'a pas executee
        assertEquals(1, requetes.get());
    }

    @Test
    @DisplayName("Q27 - get() et orElseThrow() levent la meme exception, orElseThrow() le dit")
    void getContreOrElseThrow() {
        // GIVEN
        Optional<String> vide = Optional.empty();

        // WHEN / THEN
        assertThrows(NoSuchElementException.class, vide::get);
        assertThrows(NoSuchElementException.class, vide::orElseThrow);
    }

    private static String chargerDepuisLaBase(AtomicInteger requetes) {
        requetes.incrementAndGet();
        return "defaut";
    }

    @Test
    @DisplayName("Q28 - un Stream ne se consomme qu'une fois")
    void streamUnique() {
        // GIVEN
        Stream<String> s = Stream.of("a", "b");

        // WHEN
        s.count();

        // THEN
        assertThrows(IllegalStateException.class, s::count);
    }

    @Test
    @DisplayName("Q28 - toList() est non modifiable, Collectors.toList() modifiable en pratique")
    void toListContreCollectors() {
        // GIVEN
        List<String> nouvelle = Stream.of("a").toList();
        // La Javadoc de Collectors.toList() ne garantit ni le type ni la mutabilite
        List<String> ancienne = Stream.of("a").collect(Collectors.toList());

        // WHEN
        ancienne.add("b");

        // THEN
        assertEquals(List.of("a", "b"), ancienne);
        assertThrows(UnsupportedOperationException.class, () -> nouvelle.add("b"));
    }
}
