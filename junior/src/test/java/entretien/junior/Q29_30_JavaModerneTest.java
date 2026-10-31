package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Question 29 : ce qu'un record genere, et pourquoi il n'est immuable qu'en surface.
 * Question 30 : le programme Java le plus court, lance sans compilation prealable.
 */
class Q29_30_JavaModerneTest {

    /** Champs final, mais la liste recue reste celle de l'appelant. */
    record Client(String nom, List<String> commandes) {}

    /** La correction : le constructeur compact garde une copie non modifiable. */
    record ClientSur(String nom, List<String> commandes) {
        ClientSur {
            commandes = List.copyOf(commandes);
        }
    }

    @Test
    @DisplayName("Q29 - un record genere equals, hashCode et toString")
    void recordGenere() {
        // GIVEN
        Client a = new Client("Lea", List.of());
        Client b = new Client("Lea", List.of());

        // WHEN / THEN
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        // Le format exact de toString() n'est pas specifie par la Javadoc de Record
        assertTrue(a.toString().contains("Lea"));
    }

    @Test
    @DisplayName("Q29 - immuable en surface : la liste a l'interieur reste modifiable")
    void immuableEnSurface() {
        // GIVEN
        Client client = new Client("Lea", new ArrayList<>());

        // WHEN
        client.commandes().add("C-1");

        // THEN
        assertEquals(List.of("C-1"), client.commandes());
    }

    @Test
    @DisplayName("Q29 - avec List.copyOf, le record est vraiment immuable")
    void immuableEnProfondeur() {
        // GIVEN
        List<String> commandes = new ArrayList<>();
        ClientSur client = new ClientSur("Lea", commandes);

        // WHEN
        commandes.add("C-1");

        // THEN
        assertEquals(List.of(), client.commandes());
        assertThrows(UnsupportedOperationException.class, () -> client.commandes().add("C-2"));
    }

    @Test
    @DisplayName("Q30 - java Bonjour.java : void main() et IO.println, sans classe ni compilation")
    void programmeLePlusCourt() throws IOException, InterruptedException {
        // GIVEN : le java du JDK qui execute les tests
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");

        // WHEN
        Process process = new ProcessBuilder(java.toString(), "q30/Bonjour.java")
                .redirectErrorStream(true)
                .start();
        String sortie = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // THEN
        assertEquals(0, process.waitFor());
        assertEquals("Bonjour Java 25", sortie.strip());
    }
}
