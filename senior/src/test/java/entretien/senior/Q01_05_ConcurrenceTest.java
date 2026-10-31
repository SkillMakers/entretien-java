package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 1 a 5 : compteur partage, deadlock, ExecutorService, CompletableFuture. */
class Q01_05_ConcurrenceTest {

    static final int THREADS = 4;
    static final int INCREMENTS = 100_000;

    /** Lance {@code THREADS} threads qui executent chacun {@code INCREMENTS} fois l'action. */
    static void enParallele(Runnable action) throws InterruptedException {
        Thread[] threads = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            threads[t] = Thread.ofPlatform().start(() -> {
                for (int i = 0; i < INCREMENTS; i++) {
                    action.run();
                }
            });
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // Question 1 : count++ n'est pas atomique (lire, ajouter, ecrire).
    // volatile ne change rien a l'atomicite (question 2) : il empeche seulement le JIT de garder
    // le champ dans un registre, ce qui rend la perte observable a chaque execution.
    static volatile int compteurVolatile;

    @Test
    @DisplayName("Q1 - mesure : 4 threads x 100 000 count++ sur un champ volatile, AtomicInteger exact")
    void compteurPartage() throws InterruptedException {
        // GIVEN
        compteurVolatile = 0;
        AtomicInteger compteurAtomique = new AtomicInteger();

        // WHEN
        enParallele(() -> compteurVolatile++);
        enParallele(compteurAtomique::incrementAndGet);

        // THEN : la perte varie a chaque execution, elle est affichee (voir MESURES.md), pas verifiee
        System.out.printf("Q1 - attendu %d, count++ : %d, AtomicInteger : %d%n",
                THREADS * INCREMENTS, compteurVolatile, compteurAtomique.get());
        assertEquals(THREADS * INCREMENTS, compteurAtomique.get());
    }

    static volatile int stock;

    @Test
    @DisplayName("Q1 - preuve : deux threads lisent avant d'ecrire, un increment est perdu a chaque fois")
    void incrementPerdu() throws Exception {
        // GIVEN : la barriere force l'entrelacement lire, lire, ecrire, ecrire
        stock = 0;
        CyclicBarrier lecturesFaites = new CyclicBarrier(2);
        Runnable incrementer = () -> {
            try {
                int lu = stock;          // lire
                lecturesFaites.await();  // les deux threads ont lu la meme valeur
                stock = lu + 1;          // ajouter, ecrire
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        // WHEN
        Thread a = Thread.ofPlatform().start(incrementer);
        Thread b = Thread.ofPlatform().start(incrementer);
        a.join();
        b.join();

        // THEN : deux increments, un seul compte
        assertEquals(1, stock);
    }

    @Test
    @DisplayName("Q3 - deux threads, deux verrous pris dans l'ordre inverse : deadlock detecte")
    void deadlock() throws InterruptedException {
        // GIVEN
        ReentrantLock compteA = new ReentrantLock();
        ReentrantLock compteB = new ReentrantLock();
        CountDownLatch premierVerrouPris = new CountDownLatch(2);
        Thread virementAB = Thread.ofPlatform().daemon().start(() -> transferer(compteA, compteB, premierVerrouPris));
        Thread virementBA = Thread.ofPlatform().daemon().start(() -> transferer(compteB, compteA, premierVerrouPris));

        try {
            // WHEN : on interroge la JVM, comme jstack
            long[] bloques = attendreDeadlock();

            // THEN : ce sont bien nos deux virements
            assertNotNull(bloques, "aucun deadlock detecte en 5 s");
            Arrays.sort(bloques);
            long[] attendus = {virementAB.threadId(), virementBA.threadId()};
            Arrays.sort(attendus);
            assertArrayEquals(attendus, bloques);
        } finally {
            // lockInterruptibly permet de sortir les deux threads de l'attente
            virementAB.interrupt();
            virementBA.interrupt();
            virementAB.join(1_000);
            virementBA.join(1_000);
        }
        assertFalse(virementAB.isAlive() || virementBA.isAlive());
    }

    /** Prend le premier verrou, attend que l'autre thread ait pris le sien, puis demande le second. */
    private static void transferer(ReentrantLock premier, ReentrantLock second, CountDownLatch pris) {
        try {
            premier.lockInterruptibly();
            try {
                pris.countDown();
                pris.await();
                second.lockInterruptibly();
                second.unlock();
            } finally {
                premier.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Renvoie les identifiants des threads en deadlock, ou null si aucun apres 5 s. */
    private static long[] attendreDeadlock() throws InterruptedException {
        ThreadMXBean jvm = ManagementFactory.getThreadMXBean();
        for (int essai = 0; essai < 50; essai++) {
            long[] ids = jvm.findDeadlockedThreads();
            if (ids != null) {
                return ids;
            }
            Thread.sleep(100);
        }
        return null;
    }

    @Test
    @DisplayName("Q4 - depuis Java 19, un ExecutorService se ferme avec try-with-resources")
    void executorAutoCloseable() {
        // GIVEN
        AtomicInteger taches = new AtomicInteger();

        // WHEN : close() attend la fin de toutes les taches soumises
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 100; i++) {
                executor.submit(taches::incrementAndGet);
            }
        }

        // THEN
        assertEquals(100, taches.get());
    }

    record Client(String id) {}

    record Commandes(String clientId, int nombre) {}

    static CompletableFuture<Client> chargerClient(String id) {
        return CompletableFuture.supplyAsync(() -> new Client(id));
    }

    static CompletableFuture<Commandes> chargerCommandes(Client client) {
        return CompletableFuture.supplyAsync(() -> new Commandes(client.id(), 3));
    }

    @Test
    @DisplayName("Q5 - thenCompose enchaine deux appels asynchrones, thenApply les emboite")
    void thenComposeContreThenApply() throws Exception {
        // GIVEN
        String clientId = "C-1";

        // WHEN
        CompletableFuture<CompletableFuture<Commandes>> emboite =
                chargerClient(clientId).thenApply(Q01_05_ConcurrenceTest::chargerCommandes);
        CompletableFuture<Commandes> aplati =
                chargerClient(clientId).thenCompose(Q01_05_ConcurrenceTest::chargerCommandes);

        // THEN
        assertEquals(3, emboite.get(1, TimeUnit.SECONDS).get(1, TimeUnit.SECONDS).nombre());
        assertEquals(3, aplati.get(1, TimeUnit.SECONDS).nombre());
    }

    @Test
    @DisplayName("Q5 - exceptionally rattrape l'erreur d'une etape asynchrone")
    void exceptionally() {
        // GIVEN
        CompletableFuture<Integer> stockDistant = CompletableFuture.supplyAsync(() -> {
            throw new IllegalStateException("service stock indisponible");
        });

        // WHEN
        int resultat = stockDistant.exceptionally(erreur -> -1).join();

        // THEN
        assertEquals(-1, resultat);
    }
}
