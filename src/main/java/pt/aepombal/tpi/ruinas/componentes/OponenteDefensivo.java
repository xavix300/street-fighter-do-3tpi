package pt.aepombal.tpi.ruinas.componentes;

import pt.aepombal.tpi.ruinas.logica.GestorCombate;

// [IA] IA que prefere defender e recuar quando o jogador se aproxima.
/** IA que dá prioridade à defesa e ao controlo de distância. */
public final class OponenteDefensivo extends ComportamentoOponente {

    public OponenteDefensivo(GestorCombate gestor) {
        super(gestor);
    }

    @Override
    protected void decidir() {
        if (gestor.distanciaAteJogador(lutador()) < 120) {
            lutador().bloquear(false);
        } else {
            lutador().moverParaFrente();
        }
    }

    @Override
    protected double intervaloDecisao() {
        return 0.48;
    }
}
