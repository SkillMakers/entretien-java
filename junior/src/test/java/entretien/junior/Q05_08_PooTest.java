package entretien.junior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Questions 6 et 8 : surcharge contre redefinition, methodes static. */
class Q05_08_PooTest {

    // Deux surcharges : le compilateur choisit sur le type DECLARE de l'argument.
    static String decrire(Object o) {
        return "Object";
    }

    static String decrire(String s) {
        return "String";
    }

    static class Animal {
        String cri() { return "..."; }
        static String famille() { return "Animal"; }
    }

    static class Chat extends Animal {
        @Override
        String cri() { return "Miaou"; }

        /** Masque Animal.famille() : une methode static ne se redefinit pas. */
        static String famille() { return "Chat"; }
    }

    @Test
    @DisplayName("Q6 - la surcharge suit le type declare, pas le type reel")
    void surchargeALaCompilation() {
        // GIVEN
        Object texte = "bonjour";

        // WHEN
        String choisie = decrire(texte);

        // THEN
        assertEquals("Object", choisie);
        assertEquals("String", decrire("bonjour"));
    }

    @Test
    @DisplayName("Q6 - la redefinition suit le type reel")
    void redefinitionALExecution() {
        // GIVEN
        Animal animal = new Chat();

        // WHEN
        String cri = animal.cri();

        // THEN
        assertEquals("Miaou", cri);
    }

    @Test
    @DisplayName("Q8 - une methode static est masquee, pas redefinie")
    @SuppressWarnings("static") // appel volontaire d'une methode static via une instance
    void staticMasquee() {
        // GIVEN
        Animal animal = new Chat();

        // WHEN
        String famille = animal.famille();

        // THEN
        assertEquals("Animal", famille);
        assertEquals("Chat", Chat.famille());
    }
}
