package pt.aepombal.tpi.ruinas;

import pt.aepombal.tpi.ruinas.modelo.Personagem;
import java.util.Objects;

/** Escolhas locais do menu; uma fonte nunca controla os dois jogadores no versus. */
final class ConfiguracaoPartida {
    enum Fonte {
        TECLADO_1("Teclado 1 — WASD / UIO / JKL"),
        TECLADO_2("Teclado 2 — Setas / Num 789 / Num 456"),
        COMANDO_1("Comando 1"),
        COMANDO_2("Comando 2");

        final String nome;
        Fonte(String nome) { this.nome = nome; }
    }

    static final ConfiguracaoPartida ATUAL = new ConfiguracaoPartida();
    private boolean versus;
    private final Fonte[] fontes = {Fonte.TECLADO_1, Fonte.TECLADO_2};
    private final Personagem[] personagens = {Personagem.LUTADOR_01, Personagem.LUTADOR_01};

    boolean versus() { return versus; }
    Fonte fonte(int jogador) { return fontes[jogador]; }
    Personagem personagem(int jogador) { return personagens[jogador]; }

    void selecionarPersonagem(int jogador, Personagem personagem) {
        personagens[jogador] = Objects.requireNonNull(personagem);
    }

    void alternarModo() {
        versus = !versus;
        if (versus && fontes[0] == fontes[1]) seguinteFonte(1);
    }

    void seguinteFonte(int jogador) {
        Fonte[] opcoes = Fonte.values();
        int proxima = fontes[jogador].ordinal();
        do {
            proxima = (proxima + 1) % opcoes.length;
        } while (versus && opcoes[proxima] == fontes[1 - jogador]);
        fontes[jogador] = opcoes[proxima];
    }

    String descricao(int jogador) {
        return !versus && jogador == 0 ? "Teclado 1 + Comando 1" : fontes[jogador].nome;
    }
}
