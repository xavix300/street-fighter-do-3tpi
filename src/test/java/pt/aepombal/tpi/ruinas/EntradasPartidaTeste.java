package pt.aepombal.tpi.ruinas;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public final class EntradasPartidaTeste {
    public static void main(String[] args) {
        var config = new ConfiguracaoPartida();
        var entrada = new EntradasPartida();
        entrada.premir(KeyCode.D);
        entrada.premir(KeyCode.U);
        entrada.premir(KeyCode.LEFT);
        entrada.premir(KeyCode.NUMPAD6);
        verificar(entrada.botoes(config, 0, 1, 0) == 0x4009);
        verificar(entrada.botoes(config, 1, 1, 0) == 0);
        config.alternarModo();
        verificar(entrada.botoes(config, 0, 0x2000, 0x8000) == 0x4008);
        verificar(entrada.botoes(config, 1, 0x2000, 0x8000) == 0x104);
        entrada.libertar(KeyCode.D);
        verificar(entrada.teclado(0) == 0x4000);
        entrada.limpar();
        verificar(entrada.teclado(0) == 0 && entrada.teclado(1) == 0);
        Set<String> combinacoes = new HashSet<>();
        for (var fonte : ConfiguracaoPartida.Fonte.values()) {
            if (config.fonte(1) == fonte) config.seguinteFonte(1);
            for (int i = 0; config.fonte(0) != fonte && i < 4; i++) config.seguinteFonte(0);
            verificar(config.fonte(0) == fonte);
            for (int j = 0; j < 3; j++) {
                verificar(config.fonte(0) != config.fonte(1));
                combinacoes.add(config.fonte(0) + "/" + config.fonte(1));
                int p1 = entrada.botoes(config, 0, 0x2000, 0x8000);
                int p2 = entrada.botoes(config, 1, 0x2000, 0x8000);
                verificar(p1 == esperado(config.fonte(0)) && p2 == esperado(config.fonte(1)));
                config.seguinteFonte(1);
            }
        }
        verificar(combinacoes.size() == 12); // Todas as atribuições distintas possíveis.
        verificar(EntradasPartida.direcao(12) == 0);
        verificar(EntradasPartida.direcao(4) == -1 && EntradasPartida.direcao(8) == 1);
        verificar(EntradasPartida.saltar(1) && !EntradasPartida.saltar(3));
        System.out.println("Entradas versus: OK (12 atribuições, teclados independentes, diagonais)");
    }

    private static int esperado(ConfiguracaoPartida.Fonte fonte) {
        return switch (fonte) {
            case COMANDO_1 -> 0x2000;
            case COMANDO_2 -> 0x8000;
            default -> 0;
        };
    }

    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Atribuição de controlos incorreta");
    }
}
