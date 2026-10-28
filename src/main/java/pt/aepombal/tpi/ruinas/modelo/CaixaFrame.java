package pt.aepombal.tpi.ruinas.modelo;

// [IA] Dados de uma caixa identificada dentro de um frame visual.
/** Uma caixa de colisão individual dentro de um frame de combate. */
public record CaixaFrame(
        TipoCaixa tipo,
        String identificador,
        Caixa caixa,
        int dano,
        int hitStun,
        int blockStun) {

    public CaixaFrame {
        if (tipo == null || identificador == null || identificador.isBlank() || caixa == null) {
            throw new IllegalArgumentException("Uma caixa de frame precisa de tipo, identificador e área.");
        }
    }

    /** Indica se esta caixa tem uma área ofensiva. */
    public boolean eOfensiva() {
        return tipo == TipoCaixa.HIT;
    }
}
