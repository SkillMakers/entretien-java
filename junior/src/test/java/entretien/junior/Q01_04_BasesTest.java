package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 2 et 3 : passage par valeur, == contre equals. */
class Q01_04_BasesTest {

    /** Reaffecte le parametre : l'appelant ne voit rien, car seule la copie de la reference change. */
    static void reassigner(List<String> liste) {
        liste = new ArrayList<>();
        liste.add("perdu");
    }

    /** Modifie l'objet pointe : l'appelant voit le changement. */
    static void modifier(List<String> liste) {
        liste.add("garde");
    }

    @Test
    @DisplayName("Q2 - reassigner le parametre ne change rien pour l'appelant")
    void passageParValeur() {
        // GIVEN
        List<String> courses = new ArrayList<>();

        // WHEN
        reassigner(courses);

        // THEN
        assertEquals(List.of(), courses);

        // WHEN
        modifier(courses);

        // THEN
        assertEquals(List.of("garde"), courses);
    }

    @Test
    @DisplayName("Q3 - Integer : 127 == 127 est vrai, 128 == 128 est faux (cache par defaut)")
    void cacheInteger() {
        // GIVEN : par defaut, la JVM met en cache les Integer de -128 a 127
        // (borne haute reglable avec -XX:AutoBoxCacheMax)
        Integer a = 127, b = 127;
        Integer c = 128, d = 128;

        // WHEN / THEN
        assertTrue(a == b);   // le meme objet, sorti du cache
        assertFalse(c == d);  // deux objets distincts
        assertTrue(c.equals(d));
    }
}
