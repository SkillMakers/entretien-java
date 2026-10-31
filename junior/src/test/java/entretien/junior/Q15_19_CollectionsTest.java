package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 17 a 19 : ConcurrentModificationException, listes non modifiables, tri. */
class Q15_19_CollectionsTest {

    @Test
    @DisplayName("Q17 - supprimer pendant un for-each leve ConcurrentModificationException")
    void suppressionDansForEach() {
        // GIVEN
        List<String> produits = new ArrayList<>(List.of("clavier", "souris", "ecran"));

        // WHEN / THEN
        assertThrows(ConcurrentModificationException.class, () -> {
            for (String p : produits) {
                if (p.equals("clavier")) {
                    produits.remove(p);
                }
            }
        });
    }

    @Test
    @DisplayName("Q17 - la bonne facon : removeIf")
    void removeIf() {
        // GIVEN
        List<String> produits = new ArrayList<>(List.of("clavier", "souris", "ecran"));

        // WHEN
        produits.removeIf(p -> p.equals("clavier"));

        // THEN
        assertEquals(List.of("souris", "ecran"), produits);
    }

    @Test
    @DisplayName("Q18 - List.of : rien n'est modifiable, et null est refuse")
    void listOf() {
        // GIVEN
        List<String> fixe = List.of("a", "b");

        // WHEN / THEN
        assertThrows(UnsupportedOperationException.class, () -> fixe.set(0, "z"));
        assertThrows(NullPointerException.class, () -> List.of("a", null));
    }

    @Test
    @DisplayName("Q18 - Arrays.asList : taille fixe, mais set autorise")
    void arraysAsList() {
        // GIVEN
        List<String> tableau = Arrays.asList("a", "b");

        // WHEN
        tableau.set(0, "z");

        // THEN
        assertEquals(List.of("z", "b"), tableau);
        assertThrows(UnsupportedOperationException.class, () -> tableau.add("c"));
    }

    @Test
    @DisplayName("Q18 - unmodifiableList : une vue qui suit la liste d'origine")
    void unmodifiableList() {
        // GIVEN
        List<String> source = new ArrayList<>(List.of("a"));
        List<String> vue = Collections.unmodifiableList(source);

        // WHEN
        source.add("b");

        // THEN
        assertEquals(List.of("a", "b"), vue);
        assertThrows(UnsupportedOperationException.class, () -> vue.add("c"));
    }

    /** Un prix se represente en BigDecimal, jamais en double. */
    record Produit(String nom, BigDecimal prix) {}

    @Test
    @DisplayName("Q19 - Comparator : par prix, puis par nom")
    void comparator() {
        // GIVEN
        List<Produit> produits = new ArrayList<>(List.of(
                new Produit("souris", new BigDecimal("19.90")),
                new Produit("clavier", new BigDecimal("19.90")),
                new Produit("ecran", new BigDecimal("149.00"))));

        // WHEN
        produits.sort(Comparator.comparing(Produit::prix).thenComparing(Produit::nom));

        // THEN
        assertEquals(List.of("clavier", "souris", "ecran"),
                produits.stream().map(Produit::nom).toList());
    }
}
