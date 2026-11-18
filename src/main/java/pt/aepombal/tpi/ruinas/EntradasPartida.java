package pt.aepombal.tpi.ruinas;

import javafx.scene.input.KeyCode;
import java.util.EnumSet;
import java.util.Set;

/** Dois esquemas de teclado sem teclas sobrepostas, convertidos para a máscara comum. */
final class EntradasPartida {
    private static final KeyCode[][] TECLAS = {
            {KeyCode.W, KeyCode.S, KeyCode.A, KeyCode.D, KeyCode.U, KeyCode.I, KeyCode.O,
                    KeyCode.J, KeyCode.K, KeyCode.L, KeyCode.T},
            {KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.NUMPAD7, KeyCode.NUMPAD8,
                    KeyCode.NUMPAD9, KeyCode.NUMPAD4, KeyCode.NUMPAD5, KeyCode.NUMPAD6, KeyCode.NUMPAD0}
    };
    private static final int[] MASCARAS = {1, 2, 4, 8, 0x4000, 0x8000, 0x200, 0x1000, 0x2000, 0x100, 0x20};
    private final Set<KeyCode> premidas = EnumSet.noneOf(KeyCode.class);

    boolean premir(KeyCode tecla) { return premidas.add(tecla); }
    void libertar(KeyCode tecla) { premidas.remove(tecla); }
    void limpar() { premidas.clear(); }

    int teclado(int indice) {
        int resultado = 0;
        for (int i = 0; i < MASCARAS.length; i++) {
            if (premidas.contains(TECLAS[indice][i])) resultado |= MASCARAS[i];
        }
        return resultado;
    }

    int botoes(ConfiguracaoPartida config, int jogador, int comando1, int comando2) {
        if (!config.versus()) return jogador == 0 ? teclado(0) | comando1 : 0;
        return switch (config.fonte(jogador)) {
            case TECLADO_1 -> teclado(0);
            case TECLADO_2 -> teclado(1);
            case COMANDO_1 -> comando1;
            case COMANDO_2 -> comando2;
        };
    }

    static int direcao(int botoes) {
        return ((botoes & 8) != 0 ? 1 : 0) - ((botoes & 4) != 0 ? 1 : 0);
    }

    static boolean saltar(int botoes) { return (botoes & 3) == 1; }
}
