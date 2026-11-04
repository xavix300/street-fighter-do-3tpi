package pt.aepombal.tpi.ruinas.componentes;

import com.almasb.fxgl.entity.component.Component;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import java.util.concurrent.ThreadLocalRandom;

// [IA] Base abstrata para as três IAs de oponente, usada para R1/R2.
/** Comportamento comum de uma IA de oponente. */
public abstract class ComportamentoOponente extends Component {

    protected final GestorCombate gestor;
    private double tempoAteDecisao;
    private AcaoCombate ultimoAtaque;

    protected ComportamentoOponente(GestorCombate gestor) {
        this.gestor = gestor;
    }

    @Override
    public final void onUpdate(double tpf) {
        tempoAteDecisao -= tpf;
        if (tempoAteDecisao <= 0) {
            decidir();
            tempoAteDecisao = intervaloDecisao();
        }
    }

    /** Executa a decisão específica deste estilo de IA. */
    protected abstract void decidir();

    /** Define a frequência de decisão deste estilo. */
    protected abstract double intervaloDecisao();

    /** Alterna golpes disponíveis sem repetir imediatamente o anterior. */
    protected void atacarVariado(AcaoCombate... opcoes) {
        AcaoCombate escolha;
        do {
            escolha = opcoes[ThreadLocalRandom.current().nextInt(opcoes.length)];
        } while (opcoes.length > 1 && escolha == ultimoAtaque);
        ultimoAtaque = escolha;
        lutador().parar();
        lutador().atacar(escolha);
    }

    /** Devolve o componente do lutador que tem esta IA. */
    protected ComponenteLutador lutador() {
        return entity.getComponent(ComponenteLutador.class);
    }
}
