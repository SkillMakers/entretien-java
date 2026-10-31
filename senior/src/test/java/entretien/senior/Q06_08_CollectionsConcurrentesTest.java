package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 6 a 8 : ConcurrentHashMap, operations atomiques, CopyOnWriteArrayList. */
class Q06_08_CollectionsConcurrentesTest {

    @Test
    @DisplayName("Q6 - ConcurrentHashMap refuse les cles et les valeurs null")
    void pasDeNull() {
        // GIVEN
        Map<String, Integer> stocks = new ConcurrentHashMap<>();

        // WHEN / THEN : null serait ambigu (absent, ou present a null ?) entre deux threads
        assertThrows(NullPointerException.class, () -> stocks.put("clavier", null));
        assertThrows(NullPointerException.class, () -> stocks.put(null, 1));
    }

    @Test
    @DisplayName("Q7 - merge est atomique : aucun increment perdu")
    void mergeAtomique() {
        // GIVEN
        Map<String, Integer> ventes = new ConcurrentHashMap<>();

        // WHEN : 4 threads, 10 000 ventes chacun, sur la meme cle
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int t = 0; t < 4; t++) {
                executor.submit(() -> {
                    for (int i = 0; i < 10_000; i++) {
                        ventes.merge("clavier", 1, Integer::sum);
                    }
                });
            }
        }

        // THEN
        assertEquals(40_000, ventes.get("clavier"));
    }

    @Test
    @DisplayName("Q8 - CopyOnWriteArrayList : on itere sur une copie, pas de ConcurrentModificationException")
    void copyOnWrite() {
        // GIVEN
        List<String> abonnes = new CopyOnWriteArrayList<>(List.of("lea", "tom"));
        List<String> vus = new ArrayList<>();

        // WHEN : ajouter pendant le parcours
        for (String abonne : abonnes) {
            vus.add(abonne);
            abonnes.add(abonne + "-bis");
        }

        // THEN : la boucle a vu l'instantane du depart, la liste a bien recu les ajouts
        assertEquals(List.of("lea", "tom"), vus);
        assertEquals(4, abonnes.size());
    }
}
