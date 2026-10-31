package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Question 27 : ce que fait vraiment un parallelStream. */
class Q26_28_PerformanceTest {

    @Test
    @DisplayName("Q27 - parallelStream tourne sur le ForkJoinPool commun, partage par toute la JVM")
    void poolCommun() {
        // GIVEN : sur une machine a un seul thread de pool commun, il n'y a rien a montrer
        assumeTrue(ForkJoinPool.getCommonPoolParallelism() > 1);
        Set<String> threads = ConcurrentHashMap.newKeySet();

        // WHEN : chaque element attend 1 ms, assez pour que les workers du pool prennent du travail
        IntStream.range(0, 100).parallel().forEach(i -> {
            threads.add(Thread.currentThread().getName());
            attendre(1);
        });

        // THEN
        assertTrue(threads.stream().anyMatch(nom -> nom.startsWith("ForkJoinPool.commonPool-worker")));
    }

    private static void attendre(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("Q27 - en parallele, forEach ne garantit pas l'ordre ; forEachOrdered le garantit")
    void ordre() {
        // GIVEN
        List<Integer> attendu = IntStream.range(0, 1_000).boxed().toList();
        List<Integer> forEach = Collections.synchronizedList(new ArrayList<>());
        List<Integer> forEachOrdered = Collections.synchronizedList(new ArrayList<>());

        // WHEN
        IntStream.range(0, 1_000).parallel().forEach(forEach::add);
        IntStream.range(0, 1_000).parallel().forEachOrdered(forEachOrdered::add);

        // THEN : forEach traite tout, dans un ordre quelconque (affiche, pas verifie)
        System.out.printf("Q27 - forEach : %s...%n", forEach.subList(0, 10));
        assertEquals(new HashSet<>(attendu), new HashSet<>(forEach));
        assertEquals(attendu, forEachOrdered);
    }
}
