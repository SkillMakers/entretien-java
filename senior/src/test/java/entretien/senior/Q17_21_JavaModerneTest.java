package entretien.senior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 17 a 21 : sealed, pattern matching, collections ordonnees, Gatherers, constructeurs flexibles. */
class Q17_21_JavaModerneTest {

    /** Une hierarchie fermee : le compilateur connait toutes les implementations. */
    sealed interface Paiement permits Carte, Virement, Especes {}

    record Carte(String numero, long montantCentimes) implements Paiement {}

    record Virement(String iban, long montantCentimes) implements Paiement {}

    record Especes(long montantCentimes) implements Paiement {}

    /** Switch exhaustif, sans default : ajouter un type de paiement fait echouer la compilation ici. */
    static long frais(Paiement paiement) {
        return switch (paiement) {
            case Carte c when c.montantCentimes() > 100_000 -> 0;
            case Carte c -> c.montantCentimes() / 100;
            case Virement(String iban, _) when iban.startsWith("FR") -> 0;
            case Virement v -> 150;
            case Especes e -> 0;
        };
    }

    @Test
    @DisplayName("Q17 - sealed + switch exhaustif, sans default")
    void sealedEtSwitch() {
        // WHEN / THEN
        assertEquals(50, frais(new Carte("4970", 5_000)));
        assertEquals(0, frais(new Carte("4970", 200_000)));
        assertEquals(150, frais(new Virement("DE89", 5_000)));
        assertEquals(0, frais(new Especes(5_000)));
    }

    @Test
    @DisplayName("Q18 - record pattern : on deconstruit le record dans le case")
    void recordPattern() {
        // GIVEN
        Paiement paiement = new Virement("FR76", 5_000);

        // WHEN
        long frais = frais(paiement);

        // THEN : la branche Virement(String iban, _) when iban.startsWith("FR")
        assertEquals(0, frais);
    }

    @Test
    @DisplayName("Q19 - Java 21 : getFirst, getLast et reversed sur toute collection ordonnee")
    void sequencedCollections() {
        // GIVEN
        List<String> etapes = new ArrayList<>(List.of("panier", "paiement", "livraison"));
        SequencedMap<String, Integer> classement = new LinkedHashMap<>();
        classement.put("lea", 3);
        classement.put("tom", 1);

        // WHEN / THEN
        assertEquals("panier", etapes.getFirst());
        assertEquals("livraison", etapes.getLast());
        assertEquals(List.of("livraison", "paiement", "panier"), etapes.reversed());
        assertEquals("tom", classement.lastEntry().getKey());
    }

    @Test
    @DisplayName("Q20 - Java 24 : un Gatherer decoupe un flux en lots")
    void gatherers() {
        // GIVEN
        Stream<Integer> commandes = Stream.of(1, 2, 3, 4, 5);

        // WHEN
        List<List<Integer>> lots = commandes
                .gather(Gatherers.windowFixed(2))
                .toList();

        // THEN
        assertEquals(List.of(List.of(1, 2), List.of(3, 4), List.of(5)), lots);
    }

    static class Compte {
        final String iban;

        Compte(String iban) { this.iban = iban; }
    }

    /** Java 25 : la validation se fait AVANT super(), sans methode statique de contournement. */
    static class CompteFrancais extends Compte {
        CompteFrancais(String iban) {
            if (!iban.startsWith("FR")) {
                throw new IllegalArgumentException("IBAN francais attendu : " + iban);
            }
            super(iban);
        }
    }

    @Test
    @DisplayName("Q21 - Java 25 : du code avant super() dans un constructeur")
    void constructeurFlexible() {
        // WHEN / THEN
        assertEquals("FR76", new CompteFrancais("FR76").iban);
        assertThrows(IllegalArgumentException.class, () -> new CompteFrancais("DE89"));
    }
}
