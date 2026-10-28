package pt.aepombal.tpi.ruinas.modelo;

// [IA] Identidade e orientação horizontal de cada lutador da ronda.
/** Representa os dois lados de uma ronda de combate. */
public enum LadoLutador {
    JOGADOR(1, "vidaJogador", "guardaJogador", "acaoJogador", "comboJogador"),
    OPONENTE(-1, "vidaOponente", "guardaOponente", "acaoOponente", "comboOponente");

    private final int orientacao;
    private final String variavelVida;
    private final String variavelGuarda;
    private final String variavelAcao;
    private final String variavelCombo;

    LadoLutador(int orientacao, String variavelVida, String variavelGuarda,
                String variavelAcao, String variavelCombo) {
        this.orientacao = orientacao;
        this.variavelVida = variavelVida;
        this.variavelGuarda = variavelGuarda;
        this.variavelAcao = variavelAcao;
        this.variavelCombo = variavelCombo;
    }

    /** +1 para a direita e -1 para a esquerda. */
    public int orientacao() {
        return orientacao;
    }

    /** Nome da variável FXGL que mostra a vida deste lutador. */
    public String variavelVida() {
        return variavelVida;
    }

    /** Nome da variável FXGL que mostra a resistência da guarda. */
    public String variavelGuarda() {
        return variavelGuarda;
    }

    /** Nome da variável FXGL que mostra a ação atual deste lutador. */
    public String variavelAcao() {
        return variavelAcao;
    }

    /** Nome da variável FXGL que mostra o combo deste lado. */
    public String variavelCombo() {
        return variavelCombo;
    }

    /** Devolve o adversário deste lado. */
    public LadoLutador adversario() {
        return this == JOGADOR ? OPONENTE : JOGADOR;
    }
}
