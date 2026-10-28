package pt.aepombal.tpi.ruinas.modelo;

// [IA] Tipos de caixas usados pelo sistema de combate baseado em frames.
/** Classifica as caixas que compõem um frame de combate. */
public enum TipoCaixa {
    /** Impede que os corpos dos lutadores se atravessem. */
    PUSH("#1E90FF"),
    /** Área vulnerável do corpo. */
    HURT("#32CD32"),
    /** Área que pode causar dano ao adversário. */
    HIT("#FF3333");

    private final String corHexadecimal;

    TipoCaixa(String corHexadecimal) {
        this.corHexadecimal = corHexadecimal;
    }

    /** Cor usada para apresentar esta caixa no modo de depuração. */
    public String corHexadecimal() {
        return corHexadecimal;
    }
}
