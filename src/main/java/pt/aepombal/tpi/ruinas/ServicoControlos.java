package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

/** Leitura única por frame, também nos menus, pausa e caixas de diálogo. */
public final class ServicoControlos extends EngineService {
    private final ComandoWindows[] comandos;
    private final int[] botoes = new int[2];
    private final int[] novos = new int[2];
    private Scene cenaAnterior;
    private int direcaoAnterior;
    private double espera;
    private boolean janelaFocada;

    public ServicoControlos() {
        Set<Integer> xbox = new HashSet<>();
        Set<Integer> sony = new HashSet<>();
        comandos = new ComandoWindows[]{
                new ComandoWindows(xbox, sony), new ComandoWindows(xbox, sony)};
    }

    @Override
    public void onMainLoopStarting() {
        for (var comando : comandos) comando.iniciar();
        if (FXGL.getSceneService().getCurrentScene().getRoot().getScene().getWindow() instanceof Stage stage) {
            // Esc abre a pausa sem retirar o jogo do ecrã inteiro.
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        }
    }

    @Override
    public void onUpdate(double tpf) {
        Scene cena = FXGL.getSceneService().getCurrentScene();
        if (cena != cenaAnterior) {
            if (StreetFrameApp.instancia() != null) StreetFrameApp.instancia().limparTeclado();
            cenaAnterior = cena;
            direcaoAnterior = 0;
            espera = .35;
        }
        boolean focado = cena.getRoot().getScene() != null
                && cena.getRoot().getScene().getWindow().isFocused();
        janelaFocada = focado;
        int agregado = 0;
        int premidos = 0;
        for (int i = 0; i < 2; i++) {
            int atual = comandos[i].botoes();
            novos[i] = focado ? atual & ~botoes[i] : 0;
            botoes[i] = atual;
            agregado |= atual;
            premidos |= novos[i];
        }
        if (!focado) {
            if (StreetFrameApp.instancia() != null) StreetFrameApp.instancia().limparTeclado();
            return;
        }
        if (cena == FXGL.getSceneService().getGameScene()) return;
        int direcao = agregado & 15;
        int navegacao = premidos & 15;
        if (direcao != direcaoAnterior) {
            espera = .35;
            direcaoAnterior = direcao;
        } else if (direcao != 0 && (espera -= tpf) <= 0) {
            navegacao = direcao;
            espera = .12;
        }
        int eventos = (premidos & ~15) | navegacao;
        if (eventos == 0) return;
        if (cena instanceof MenuPrincipal menu) menu.comando(eventos);
        else if (cena instanceof MenuPausa menu) menu.comando(eventos);
        else NavegacaoMenu.comando(cena.getContentRoot(), eventos, () -> { });
    }

    int botoes(int indice) { return janelaFocada ? botoes[indice] : 0; }
    int novos(int indice) { return novos[indice]; }
    boolean ligado(int indice) { return comandos[indice].ligado(); }

    @Override
    public void onExit() {
        for (var comando : comandos) comando.fechar();
    }
}
