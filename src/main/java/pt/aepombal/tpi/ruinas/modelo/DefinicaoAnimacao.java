package pt.aepombal.tpi.ruinas.modelo;

import java.util.List;

// [IA] Modelo imutável de uma animação e dos seus frames de combate.
/** Dados de todos os frames de uma ação de combate. */
public record DefinicaoAnimacao(AcaoCombate acao, List<FrameCombate> frames) {

    public DefinicaoAnimacao {
        frames = List.copyOf(frames);
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("Uma animação precisa de pelo menos um frame.");
        }
    }

    /** Obtém um frame pela sua posição na sequência. */
    public FrameCombate frame(int indice) {
        return frames.get(indice);
    }
}
