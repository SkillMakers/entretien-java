package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 23 a 25 : singleton thread-safe, equals et heritage, cache LRU. */
class Q22_25_ConceptionTest {

    /**
     * Singleton paresseux et thread-safe sans verrou : la classe interne n'est chargee qu'au premier
     * appel de {@code instance()}, et la JVM garantit qu'une classe n'est initialisee qu'une fois.
     */
    static final class Configuration {
        private Configuration() {}

        private static final class Holder {
            static final Configuration INSTANCE = new Configuration();
        }

        static Configuration instance() {
            return Holder.INSTANCE;
        }
    }

    @Test
    @DisplayName("Q23 - singleton par classe interne : instance() renvoie toujours le meme objet")
    void singletonHolder() {
        // GIVEN
        Set<Configuration> vues = ConcurrentHashMap.newKeySet();

        // WHEN
        try (ExecutorService executor = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> vues.add(Configuration.instance()));
            }
        }

        // THEN : illustration seulement. Un singleton naif passerait aussi ce test : la vraie
        // garantie vient de la JVM, qui n'initialise une classe qu'une seule fois (JLS 12.4.2).
        assertEquals(1, vues.size());
    }

    /** Question 24 : equals redefini dans une sous-classe qui ajoute un champ. */
    static class Point {
        final int x, y;

        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Point p && x == p.x && y == p.y;
        }

        @Override
        public int hashCode() { return Objects.hash(x, y); }
    }

    static class PointColore extends Point {
        final String couleur;

        PointColore(int x, int y, String couleur) {
            super(x, y);
            this.couleur = couleur;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PointColore p && super.equals(p) && couleur.equals(p.couleur);
        }

        @Override
        public int hashCode() { return Objects.hash(super.hashCode(), couleur); }
    }

    @Test
    @DisplayName("Q24 - equals et heritage : la symetrie et le contrat de hashCode sont casses")
    void symetrieCassee() {
        // GIVEN
        Point point = new Point(1, 2);
        PointColore rouge = new PointColore(1, 2, "rouge");

        // WHEN / THEN : a.equals(b) mais pas b.equals(a)
        assertTrue(point.equals(rouge));
        assertFalse(rouge.equals(point));

        // Et deux objets egaux selon equals n'ont pas le meme hashCode :
        // un HashSet de points ne retrouve plus le point rouge
        assertNotEquals(point.hashCode(), rouge.hashCode());
        Set<Point> points = new HashSet<>(Set.of(rouge));
        assertFalse(points.contains(point));
    }

    /** La reponse attendue : la composition. Un PointColore A un Point, il n'en EST pas un. */
    record PointColoreCompose(Point point, String couleur) {}

    @Test
    @DisplayName("Q24 - avec la composition, equals redevient symetrique")
    void compositionSymetrique() {
        // GIVEN
        PointColoreCompose a = new PointColoreCompose(new Point(1, 2), "rouge");
        PointColoreCompose b = new PointColoreCompose(new Point(1, 2), "rouge");
        Point point = new Point(1, 2);

        // WHEN / THEN
        assertEquals(a, b);
        assertEquals(b, a);
        assertFalse(point.equals(a));
    }

    /**
     * Question 25 : un cache LRU (le moins recemment utilise sort en premier) en quelques lignes.
     * L'ordre d'acces ({@code accessOrder = true}) deplace en fin de liste chaque entree lue.
     * Non thread-safe : a proteger, ou a remplacer par une bibliotheque comme Caffeine en production.
     */
    @SuppressWarnings("serial") // LinkedHashMap est Serializable, ce cache n'est jamais serialise
    static final class CacheLru<K, V> extends LinkedHashMap<K, V> {
        private final int capacite;

        CacheLru(int capacite) {
            super(16, 0.75f, true);
            this.capacite = capacite;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> plusAncienne) {
            return size() > capacite;
        }
    }

    @Test
    @DisplayName("Q25 - cache LRU : LinkedHashMap en ordre d'acces + removeEldestEntry")
    void cacheLru() {
        // GIVEN
        CacheLru<String, String> cache = new CacheLru<>(2);
        cache.put("lea", "profil de Lea");
        cache.put("tom", "profil de Tom");

        // WHEN : on relit Lea, puis un troisieme profil arrive
        cache.get("lea");
        cache.put("sam", "profil de Sam");

        // THEN : Tom, le moins recemment utilise, est sorti
        assertEquals(Set.of("lea", "sam"), cache.keySet());
    }
}
