package pt.aepombal.tpi.ruinas.modelo;

import java.util.List;

// [IA] Um frame lógico suporta várias hurtboxes, pushboxes e hitboxes independentes.
/** Dados de combate de um único frame visual. */
public record FrameCombate(int duracaoFrames, List<CaixaFrame> caixas) {

    public FrameCombate {
        caixas = List.copyOf(caixas);
        if (duracaoFrames <= 0 || caixas.isEmpty()) {
            throw new IllegalArgumentException("Todo o frame precisa de duração positiva e caixas.");
        }
    }

    /** Devolve apenas as caixas de um tipo, por exemplo HURT ou HIT. */
    public List<CaixaFrame> caixasDoTipo(TipoCaixa tipo) {
        return caixas.stream().filter(caixa -> caixa.tipo() == tipo).toList();
    }

    /** Indica se o frame tem pelo menos uma área ofensiva ativa. */
    public boolean temHitbox() {
        return caixas.stream().anyMatch(CaixaFrame::eOfensiva);
    }
}
