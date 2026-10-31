package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 9 a 11 : immuabilite, pool de chaines, StringBuilder. */
class Q09_11_StringTest {

    @Test
    @DisplayName("Q9 - les methodes de String renvoient une nouvelle chaine")
    void immuable() {
        // GIVEN
        String nom = "java";

        // WHEN
        nom.toUpperCase(); // resultat ignore : nom ne change pas

        // THEN
        assertEquals("java", nom);
        assertEquals("JAVA", nom.toUpperCase());
    }

    @Test
    @DisplayName("Q10 - les litteraux partagent le pool, new String non")
    void poolDeChaines() {
        // GIVEN
        String a = "java";
        String b = "java";
        String c = new String("java");

        // WHEN / THEN
        assertTrue(a == b);
        assertFalse(a == c);
        assertTrue(a == c.intern());
        assertTrue(a.equals(c));
    }

    @Test
    @DisplayName("Q11 - en boucle, on construit avec StringBuilder")
    void stringBuilder() {
        // GIVEN
        StringBuilder sb = new StringBuilder();

        // WHEN
        for (int i = 0; i < 3; i++) {
            sb.append(i).append(';');
        }

        // THEN
        assertEquals("0;1;2;", sb.toString());
    }
}
