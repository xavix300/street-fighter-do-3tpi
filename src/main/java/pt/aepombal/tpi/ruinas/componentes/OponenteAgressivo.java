package pt.aepombal.tpi.ruinas.componentes;

import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;

// [IA] IA que encurta a distância e privilegia golpes fortes.
/** IA que avança constantemente e prefere ataques pesados. */
public final class OponenteAgressivo extends ComportamentoOponente {

    public OponenteAgressivo(GestorCombate gestor) {
        super(gestor);
    }

    @Override
    protected void decidir() {
        if (gestor.distanciaAteJogador(lutador()) > 95) {
            lutador().moverParaFrente();
        } else {
            atacarVariado(AcaoCombate.LP, AcaoCombate.MP, AcaoCombate.HP,
                    AcaoCombate.LK, AcaoCombate.MK, AcaoCombate.HK);
        }
    }

    @Override
    protected double intervaloDecisao() {
        return 0.55;
    }
}
