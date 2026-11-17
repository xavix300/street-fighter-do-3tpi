package pt.aepombal.tpi.ruinas.modelo;

/** Quantidade de poses por fase visual; não modifica o relógio nem as caixas de combate. */
public record PlanoVisualAtaque(int preparacao, int ativos, int recuperacao) {
    public PlanoVisualAtaque {
        if (preparacao < 1 || ativos < 1 || recuperacao < 1) {
            throw new IllegalArgumentException("Cada fase visual precisa de pelo menos uma pose.");
        }
    }

    public int quantidade() {
        return preparacao + ativos + recuperacao;
    }

    public int indice(DefinicaoAnimacao animacao, int frame, double tempoNoFrame) {
        return animacao.indiceVisualAtaque(frame, tempoNoFrame, preparacao, ativos, recuperacao);
    }
}
