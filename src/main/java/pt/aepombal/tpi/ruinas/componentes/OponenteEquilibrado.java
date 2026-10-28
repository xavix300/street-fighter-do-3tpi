package pt.aepombal.tpi.ruinas.componentes;

import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;

// [IA] IA intermédia que alterna aproximação, bloqueio e golpes médios.
/** IA equilibrada para a primeira ronda. */
public final class OponenteEquilibrado extends ComportamentoOponente {

    private boolean proximoAtaque;

    public OponenteEquilibrado(GestorCombate gestor) {
        super(gestor);
    }

    @Override
    protected void decidir() {
        if (gestor.distanciaAteJogador(lutador()) > 105) {
            lutador().moverParaFrente();
            return;
        }
        if (proximoAtaque) {
            lutador().atacar(AcaoCombate.MK);
        } else {
            lutador().bloquear(false);
        }
        proximoAtaque = !proximoAtaque;
    }

    @Override
    protected double intervaloDecisao() {
        return 0.42;
    }
}
