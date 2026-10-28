package pt.aepombal.tpi.ruinas.componentes;

import com.almasb.fxgl.entity.component.Component;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;

// [IA] Base abstrata para as três IAs de oponente, usada para R1/R2.
/** Comportamento comum de uma IA de oponente. */
public abstract class ComportamentoOponente extends Component {

    protected final GestorCombate gestor;
    private double tempoAteDecisao;

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

    /** Devolve o componente do lutador que tem esta IA. */
    protected ComponenteLutador lutador() {
        return entity.getComponent(ComponenteLutador.class);
    }
}
