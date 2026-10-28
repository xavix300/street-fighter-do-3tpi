package pt.aepombal.tpi.ruinas.modelo;

// [IA] Mantém a dificuldade entre rondas sem colocar regras na classe da aplicação.
/** Estado de progressão da sessão de combate. */
public final class ProgressoCombate {

    private Dificuldade dificuldade = Dificuldade.EQUILIBRADO;

    /** Devolve a dificuldade da ronda seguinte. */
    public Dificuldade dificuldade() {
        return dificuldade;
    }

    /** Aumenta a dificuldade depois de uma vitória do jogador. */
    public void registarVitoria() {
        dificuldade = dificuldade.seguinte();
    }
}
