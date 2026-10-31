package entretien.junior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 21 et 22 : return dans finally, try-with-resources. */
class Q20_23_ExceptionsTest {

    /**
     * Un return dans finally ecrase tout, meme une exception.
     * {@code javac -Xlint:finally} le signale : "finally clause cannot complete normally".
     */
    static int lire() {
        try {
            throw new IllegalStateException("fichier introuvable");
        } finally {
            return 0;
        }
    }

    @Test
    @DisplayName("Q21 - un return dans finally avale l'exception")
    void returnDansFinally() {
        // WHEN
        int resultat = lire();

        // THEN : aucune exception n'est remontee
        assertEquals(0, resultat);
    }

    private final List<String> journal = new ArrayList<>();

    /** Ressource qui note sa fermeture, puis echoue en se fermant. */
    final class Ressource implements AutoCloseable {
        private final String nom;

        Ressource(String nom) { this.nom = nom; }

        @Override
        public void close() {
            journal.add("ferme " + nom);
            throw new IllegalStateException("echec fermeture " + nom);
        }
    }

    @BeforeEach
    void viderJournal() {
        journal.clear();
    }

    @Test
    @DisplayName("Q22 - try-with-resources : ordre inverse et exceptions supprimees")
    @SuppressWarnings("try") // les ressources ne sont pas utilisees dans le bloc, c'est voulu
    void tryWithResources() {
        // WHEN
        RuntimeException e = assertThrows(RuntimeException.class, () -> {
            try (var connexion = new Ressource("connexion");
                 var requete = new Ressource("requete")) {
                throw new RuntimeException("erreur metier");
            }
        });

        // THEN : fermeture dans l'ordre inverse, l'exception principale garde les deux autres
        assertEquals(List.of("ferme requete", "ferme connexion"), journal);
        assertEquals("erreur metier", e.getMessage());
        assertEquals(2, e.getSuppressed().length);
    }
}
