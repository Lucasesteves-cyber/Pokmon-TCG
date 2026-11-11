package modelo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Testes do triângulo de fraquezas Água → Fogo → Planta → Água. */
class TabelaDeTiposTest {

    @Test
    @DisplayName("Cada tipo vence o próximo do triângulo")
    void trianguloDeFraquezas() {
        assertTrue(TabelaDeTipos.eSuperEfetivo("Água", "Fogo"));
        assertTrue(TabelaDeTipos.eSuperEfetivo("Fogo", "Planta"));
        assertTrue(TabelaDeTipos.eSuperEfetivo("Planta", "Água"));
    }

    @Test
    @DisplayName("A vantagem não vale ao contrário nem para outros tipos")
    void semVantagem() {
        assertFalse(TabelaDeTipos.eSuperEfetivo("Fogo", "Água"));
        assertFalse(TabelaDeTipos.eSuperEfetivo("Água", "Água"));
        assertFalse(TabelaDeTipos.eSuperEfetivo("Elétrico", "Água"));
        assertFalse(TabelaDeTipos.eSuperEfetivo(null, "Fogo"));
    }

    @Test
    @DisplayName("Não diferencia maiúsculas de minúsculas")
    void ignoraCaixa() {
        assertTrue(TabelaDeTipos.eSuperEfetivo("ÁGUA", "fogo"));
    }
}
