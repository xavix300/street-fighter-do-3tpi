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

    /** Liga as 40 poses às fases reais do golpe, sem antecipar o impacto nem alterar o dano. */
    public int indiceAtaque40(int indiceLogico, double tempoNoFrame) {
        if (!acao.eAtaque()) throw new IllegalStateException("A ação não é um ataque: " + acao);
        double total = 0, decorrido = 0, inicioImpacto = -1, fimImpacto = 0;
        for (int i = 0; i < frames.size(); i++) {
            var atual = frames.get(i);
            if (i < indiceLogico) decorrido += atual.duracaoFrames();
            if (atual.temHitbox()) {
                if (inicioImpacto < 0) inicioImpacto = total;
                fimImpacto = total + atual.duracaoFrames();
            }
            total += atual.duracaoFrames();
        }
        decorrido += Math.clamp(tempoNoFrame, 0, frame(indiceLogico).duracaoFrames());
        if (inicioImpacto < 0) return faseVisual(decorrido, 0, total, 0, 40);
        if (decorrido < inicioImpacto) return faseVisual(decorrido, 0, inicioImpacto, 0, 16);
        if (decorrido < fimImpacto) return faseVisual(decorrido, inicioImpacto, fimImpacto, 16, 8);
        return faseVisual(decorrido, fimImpacto, total, 24, 16);
    }

    private static int faseVisual(double tempo, double inicio, double fim, int primeiro, int quantidade) {
        double fracao = fim <= inicio ? 1 : (tempo - inicio) / (fim - inicio);
        return primeiro + Math.clamp((int) (fracao * quantidade), 0, quantidade - 1);
    }
}
