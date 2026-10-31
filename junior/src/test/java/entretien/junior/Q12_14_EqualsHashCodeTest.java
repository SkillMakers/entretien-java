package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 13 et 14 : equals sans hashCode, cle mutable dans une HashMap. */
class Q12_14_EqualsHashCodeTest {

    /**
     * Egal a un autre client de meme email, mais SANS hashCode redefini :
     * chaque instance garde le hashCode d'Object, le contrat est viole.
     */
    static final class Client {
        final String email;

        Client(String email) { this.email = email; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Client c && email.equals(c.email);
        }
    }

    @Test
    @DisplayName("Q13 - equals sans hashCode : le HashSet garde deux clients egaux")
    void equalsSansHashCode() {
        // GIVEN
        Client a = new Client("lea@example.com");
        Client b = new Client("lea@example.com");
        Set<Client> clients = new HashSet<>();

        // WHEN
        clients.add(a);
        clients.add(b);

        // THEN : le HashSet compare d'abord les hashCode ; differents, il n'appelle jamais equals
        assertTrue(a.equals(b));
        assertNotEquals(a.hashCode(), b.hashCode());
        assertEquals(2, clients.size());
    }

    /** Cle dont le hashCode depend d'un champ modifiable : a ne pas faire. */
    static final class Panier {
        String code;

        Panier(String code) { this.code = code; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Panier p && code.equals(p.code);
        }

        @Override
        public int hashCode() { return Objects.hash(code); }
    }

    @Test
    @DisplayName("Q14 - cle modifiee apres insertion : la HashMap ne la retrouve plus")
    void cleMutable() {
        // GIVEN
        Panier panier = new Panier("P-1");
        Map<Panier, Integer> totaux = new HashMap<>();
        totaux.put(panier, 42);

        // WHEN
        panier.code = "P-2";

        // THEN : l'entree est toujours la, rangee sous l'ancien hashCode, donc introuvable
        assertNull(totaux.get(panier));
        assertFalse(totaux.containsKey(panier));
        assertEquals(1, totaux.size());
    }
}
