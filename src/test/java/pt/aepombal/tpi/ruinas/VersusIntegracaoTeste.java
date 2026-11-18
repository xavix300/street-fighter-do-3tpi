package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.EngineService;
import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.componentes.ComportamentoOponente;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;
import java.util.Map;

/** Abre brevemente o jogo real e testa ecrã inteiro, menu, versus e pausa. */
public final class VersusIntegracaoTeste extends GameApplication {
    private final StreetFrameApp jogo = new StreetFrameApp();

    protected void initSettings(GameSettings s) {
        jogo.initSettings(s);
        s.setTitle("Verificação fullscreen / versus");
        s.addEngineService(Verificacao.class);
    }
    protected void initGameVars(Map<String, Object> vars) { jogo.initGameVars(vars); }
    protected void initInput() { jogo.initInput(); }
    protected void initGame() { jogo.initGame(); }
    protected void initUI() { jogo.initUI(); }
    protected void onUpdate(double tpf) { jogo.onUpdate(tpf); }

    public static final class Verificacao extends EngineService {
        private int fase;
        private double tempo;
        private double total;
        private ComponenteLutador p1, p2;
        private double x1, x2;
        private boolean dialogoConfirmado;

        @Override
        public void onUpdate(double tpf) {
            try {
                tempo += tpf;
                total += tpf;
                verificar(total < 25, "Timeout na fase " + fase);
                var cena = FXGL.getSceneService().getCurrentScene();
                if (tempo < .15) return;
                switch (fase) {
                    case 0 -> {
                        if (!(cena instanceof MenuPrincipal menu)) return;
                        verificar(((Stage) cena.getRoot().getScene().getWindow()).isFullScreen(), "Arranque em fullscreen");
                        menu.comando(0x1000); // Cruz / A: abrir Jogar.
                        verificar(PainelMenu.aberto(menu.getContentRoot()), "Comando abre configuração");
                        proxima();
                    }
                    case 1 -> {
                        MenuPrincipal menu = (MenuPrincipal) cena;
                        menu.comando(0x1000); // O foco inicial está em MODO.
                        verificar(ConfiguracaoPartida.ATUAL.versus(), "Comando escolhe versus");
                        menu.comando(2); // P1.
                        menu.comando(2); // P2.
                        menu.comando(2); // Iniciar.
                        menu.comando(0x1000);
                        proxima();
                    }
                    case 2 -> {
                        if (cena != FXGL.getSceneService().getGameScene()) return;
                        var entidades = FXGL.getGameWorld().getEntitiesByComponent(ComponenteLutador.class);
                        verificar(entidades.size() == 2, "Dois lutadores");
                        for (var entidade : entidades) {
                            verificar(!entidade.hasComponent(ComportamentoOponente.class), "Sem IA no versus");
                            var lutador = entidade.getComponent(ComponenteLutador.class);
                            if (lutador.lado() == LadoLutador.JOGADOR) p1 = lutador;
                            else p2 = lutador;
                        }
                        x1 = p1.getEntity().getX();
                        x2 = p2.getEntity().getX();
                        premir(KeyCode.D);
                        premir(KeyCode.LEFT);
                        proxima();
                    }
                    case 3 -> {
                        verificar(p1.getEntity().getX() > x1, "P1 move com D");
                        verificar(p2.getEntity().getX() < x2, "P2 move com seta");
                        libertar(KeyCode.D);
                        libertar(KeyCode.LEFT);
                        premir(KeyCode.U);
                        premir(KeyCode.NUMPAD7);
                        proxima();
                    }
                    case 4 -> {
                        verificar(p1.acao() == AcaoCombate.LP && p2.acao() == AcaoCombate.LP, "Ataques independentes P1/P2");
                        libertar(KeyCode.U);
                        libertar(KeyCode.NUMPAD7);
                        FXGL.getGameController().gotoGameMenu();
                        proxima();
                    }
                    case 5 -> {
                        verificar(cena instanceof MenuPausa, "Menu de pausa");
                        verificar(((Stage) cena.getRoot().getScene().getWindow()).isFullScreen(), "Pausa mantém fullscreen");
                        ((MenuPausa) cena).comando(0x1000); // Continuar.
                        proxima();
                    }
                    case 6 -> {
                        verificar(cena == FXGL.getSceneService().getGameScene(), "Comando retoma combate");
                        FXGL.getGameController().gotoMainMenu();
                        proxima();
                    }
                    case 7 -> {
                        if (!(cena instanceof MenuPrincipal menu)) return;
                        menu.comando(2); menu.comando(2); menu.comando(2);
                        menu.comando(0x1000); // Opções.
                        verificar(PainelMenu.aberto(menu.getContentRoot()), "Comando abre opções");
                        proxima();
                    }
                    case 8 -> {
                        MenuPrincipal menu = (MenuPrincipal) cena;
                        menu.comando(2); // Do Voltar para Controlos.
                        menu.comando(8); // Vídeo.
                        menu.comando(0x1000);
                        menu.comando(2); // Áudio.
                        menu.comando(2); // Ecrã inteiro.
                        proxima(); // A mudança de fullscreen pode provocar um pulse aninhado.
                        menu.comando(0x1000);
                    }
                    case 9 -> {
                        verificar(!((Stage) cena.getRoot().getScene().getWindow()).isFullScreen(), "Comando muda opção de vídeo");
                        proxima();
                        MenuPrincipal menu = (MenuPrincipal) cena;
                        menu.comando(0x1000);
                    }
                    case 10 -> {
                        verificar(((Stage) cena.getRoot().getScene().getWindow()).isFullScreen(), "Comando repõe fullscreen");
                        MenuPrincipal menu = (MenuPrincipal) cena;
                        menu.comando(1); // Áudio.
                        menu.comando(0x1000);
                        menu.comando(2); // Slider da música.
                        double antes = FXGL.getSettings().getGlobalMusicVolume();
                        menu.comando(4);
                        verificar(FXGL.getSettings().getGlobalMusicVolume() < antes, "Comando ajusta áudio");
                        menu.comando(0x2000);
                        verificar(!PainelMenu.aberto(menu.getContentRoot()), "Círculo / B fecha painel");
                        proxima();
                        FXGL.showMessage("Verificação de confirmação com comando", () -> dialogoConfirmado = true);
                    }
                    case 11 -> {
                        verificar(!(cena instanceof MenuPrincipal), "Diálogo é a cena ativa");
                        NavegacaoMenu.comando(cena.getContentRoot(), 0x1000, () -> { });
                        // Se ainda não tinha foco, a primeira confirmação só seleciona.
                        if (!dialogoConfirmado) NavegacaoMenu.comando(cena.getContentRoot(), 0x1000, () -> { });
                        verificar(dialogoConfirmado, "Comando confirma diálogo de fim de ronda");
                        System.out.println("REAL FXGL PASS: fullscreen, configuração por comando, dois teclados, sem IA, ataques P1/P2, pausa, opções vídeo/áudio e diálogos");
                        System.exit(0);
                    }
                    default -> throw new AssertionError("Fase inválida");
                }
            } catch (Throwable erro) {
                erro.printStackTrace();
                System.exit(1);
            }
        }

        private void proxima() { fase++; tempo = 0; }
        private static void premir(KeyCode tecla) {
            FXGL.getInput().fireEventViaHandlers(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", tecla, false, false, false, false));
        }
        private static void libertar(KeyCode tecla) {
            FXGL.getInput().fireEventViaFilters(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", tecla, false, false, false, false));
        }
        private static void verificar(boolean condicao, String mensagem) {
            if (!condicao) throw new AssertionError(mensagem);
        }
    }

    public static void main(String[] args) { launch(args); }
}
