package pt.aepombal.tpi.ruinas.componentes;

import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;

// [IA] IA que prefere defender e recuar quando o jogador se aproxima.
/** IA que dá prioridade à defesa e ao controlo de distância. */
public final class OponenteDefensivo extends ComportamentoOponente {
    private boolean contraAtacar;

    public OponenteDefensivo(GestorCombate gestor) {
        super(gestor);
    }

    @Override
    protected void decidir() {
        if (gestor.distanciaAteJogador(lutador()) < 120) {
            if (contraAtacar) {
                atacarVariado(AcaoCombate.LP, AcaoCombate.MP, AcaoCombate.LK, AcaoCombate.MK);
            } else {
                lutador().moverParaTras();
            }
            contraAtacar = !contraAtacar;
        } else {
            lutador().moverParaFrente();
        }
    }

    @Override
    protected double intervaloDecisao() {
        return 0.48;
    }
}
