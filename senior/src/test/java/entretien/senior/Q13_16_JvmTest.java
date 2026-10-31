package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 13 et 15 : la pile, et une fuite memoire classique. */
class Q13_16_JvmTest {

    static int profondeur;

    /** Une recursion sans condition d'arret : chaque appel ajoute un cadre sur la pile du thread. */
    static void descendre() {
        profondeur++;
        descendre();
    }

    @Test
    @DisplayName("Q13 - la pile d'un thread est limitee : StackOverflowError")
    void pileLimitee() {
        // GIVEN
        profondeur = 0;

        // WHEN / THEN : une Error, pas une Exception
        assertThrows(StackOverflowError.class, Q13_16_JvmTest::descendre);
        System.out.printf("Q13 - StackOverflowError apres %d appels%n", profondeur);
        assertTrue(profondeur > 1_000);
    }

    /**
     * Un cache sans limite ni expiration, dans un champ static : tout ce qui y entre reste reference
     * tant que la classe est chargee, donc le garbage collector ne peut pas le liberer.
     */
    static final Map<String, byte[]> CACHE = new HashMap<>();

    static byte[] rapport(String id) {
        return CACHE.computeIfAbsent(id, cle -> new byte[1_024]);
    }

    @Test
    @DisplayName("Q15 - un cache static sans limite garde tout ce qu'on lui donne")
    void fuiteParCacheStatic() {
        // GIVEN
        CACHE.clear();

        try {
            // WHEN : 10 000 rapports demandes une seule fois chacun
            for (int i = 0; i < 10_000; i++) {
                rapport("R-" + i);
            }

            // THEN : aucun n'est jamais retire, ~10 Mo restent references par le champ static
            assertEquals(10_000, CACHE.size());
        } finally {
            CACHE.clear();
        }
    }
}
