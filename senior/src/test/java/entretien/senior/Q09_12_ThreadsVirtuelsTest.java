package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 9 a 12 : threads virtuels, ScopedValue (Java 25). */
class Q09_12_ThreadsVirtuelsTest {

    static final int TACHES = 1_000;
    static final Duration ATTENTE = Duration.ofMillis(100);

    /** Soumet {@code TACHES} taches qui attendent chacune {@code ATTENTE} (un appel reseau simule). */
    static Duration executer(ExecutorService executor) {
        long debut = System.nanoTime();
        try (executor) {
            for (int i = 0; i < TACHES; i++) {
                executor.submit(() -> {
                    Thread.sleep(ATTENTE);
                    return null;
                });
            }
        }
        return Duration.ofNanos(System.nanoTime() - debut);
    }

    @Test
    @DisplayName("Q9 - 1 000 taches de 100 ms : au moins 10 s sur 10 threads, bien moins en threads virtuels")
    void threadsVirtuels() {
        // GIVEN
        ExecutorService pool = Executors.newFixedThreadPool(10);
        ExecutorService virtuels = Executors.newVirtualThreadPerTaskExecutor();

        // WHEN
        Duration dureePool = executer(pool);
        Duration dureeVirtuels = executer(virtuels);

        // THEN : 100 vagues de 10 taches font au moins 10 s ; en virtuels, toutes attendent ensemble.
        // Le rapport mesure est d'environ 70 (MESURES.md) ; on n'exige que 5 pour une machine chargee.
        System.out.printf("Q9 - pool de 10 : %d ms, threads virtuels : %d ms%n",
                dureePool.toMillis(), dureeVirtuels.toMillis());
        assertTrue(dureePool.compareTo(Duration.ofSeconds(10)) >= 0);
        assertTrue(dureeVirtuels.multipliedBy(5).compareTo(dureePool) < 0);
    }

    @Test
    @DisplayName("Q9 - un thread virtuel n'est pas un thread du systeme")
    void estVirtuel() throws InterruptedException {
        // GIVEN
        boolean[] virtuel = new boolean[1];

        // WHEN
        Thread.ofVirtual().start(() -> virtuel[0] = Thread.currentThread().isVirtual()).join();

        // THEN
        assertTrue(virtuel[0]);
        assertFalse(Thread.currentThread().isVirtual());
    }

    /** Question 12 : la valeur liee n'existe que pendant l'appel, et ne se modifie pas. */
    static final ScopedValue<String> UTILISATEUR = ScopedValue.newInstance();

    static String auditer() {
        return "action par " + UTILISATEUR.get();
    }

    @Test
    @DisplayName("Q12 - ScopedValue (definitive en Java 25) : une valeur liee le temps d'un appel")
    void scopedValue() {
        // GIVEN
        String utilisateurConnecte = "lea";

        // WHEN
        String trace = ScopedValue.where(UTILISATEUR, utilisateurConnecte)
                .call(Q09_12_ThreadsVirtuelsTest::auditer);

        // THEN : liee pendant l'appel, plus rien apres
        assertEquals("action par lea", trace);
        assertFalse(UTILISATEUR.isBound());
    }
}
