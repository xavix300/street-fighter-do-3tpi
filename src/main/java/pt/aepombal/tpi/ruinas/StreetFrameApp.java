package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.DadosCombateException;
import pt.aepombal.tpi.ruinas.modelo.RepositorioFrames;
import pt.aepombal.tpi.ruinas.modelo.ProgressoCombate;

import java.nio.file.Path;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.addUINode;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;

// [IA] Classe de arranque: só configura FXGL, liga controlos e delega regras no GestorCombate.
/** Aplicação FXGL do jogo de luta baseado em frames. */
public final class StreetFrameApp extends GameApplication {

    private GestorCombate gestor;
    private final ProgressoCombate progresso = new ProgressoCombate();
    private boolean esquerdaPremida;
    private boolean direitaPremida;
    private boolean baixoPremido;
    private boolean cimaPremida;
    private int botoesComando;
    private final ComandoWindows comando = new ComandoWindows();

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("TPI Fighter");
        settings.setVersion("0.1");
        settings.setWidth(960);
        settings.setHeight(560);
        settings.setMainMenuEnabled(true);
        settings.setCloseConfirmation(false);
        settings.setFullScreenAllowed(true);
        settings.setSceneFactory(new com.almasb.fxgl.app.scene.SceneFactory() {
            @Override
            public com.almasb.fxgl.app.scene.FXGLMenu newMainMenu() {
                return new MenuPrincipal();
            }

            @Override
            public com.almasb.fxgl.app.scene.FXGLMenu newGameMenu() {
                return new MenuPausa();
            }

            @Override
            public com.almasb.fxgl.app.scene.LoadingScene newLoadingScene() {
                return new CarregamentoTpi();
            }
        });
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("vidaJogador", ComponenteLutador.VIDA_MAXIMA);
        vars.put("vidaOponente", ComponenteLutador.VIDA_MAXIMA);
        vars.put("guardaJogador", 100);
        vars.put("guardaOponente", 100);
        vars.put("pontos", 0);
        vars.put("acaoJogador", "EM GUARDA");
        vars.put("acaoOponente", "EM GUARDA");
        vars.put("dificuldade", "EQUILIBRADO");
        vars.put("frameData", "FRAME ADVANTAGE  —");
        vars.put("tipoAcerto", "");
    }

    @Override
    protected void initInput() {
        ligarMovimento("Esquerda", KeyCode.A, false);
        ligarMovimento("Direita", KeyCode.D, true);
        getInput().addAction(new UserAction("Saltar") {
            @Override protected void onActionBegin() { cimaPremida = true; atualizarSaltoPremido(); }
            @Override protected void onActionEnd() { cimaPremida = false; }
        }, KeyCode.W);
        ligarAcao("Provocação", KeyCode.T, () -> gestor.provocarJogador());
        getInput().addAction(new UserAction("Baixo") {
            @Override protected void onActionBegin() { baixoPremido = true; atualizarBaixo(); }
            @Override protected void onActionEnd() { baixoPremido = false; atualizarBaixo(); }
        }, KeyCode.S);
        // Também recebe as libertações diretamente, incluindo combinações de teclas.
        getInput().addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, evento -> {
            switch (evento.getCode()) {
                case A -> { esquerdaPremida = false; atualizarMovimentoPremido(); }
                case D -> { direitaPremida = false; atualizarMovimentoPremido(); }
                case S -> { baixoPremido = false; atualizarBaixo(); }
                case W -> cimaPremida = false;
                default -> { }
            }
        });
        ligarAcao("Depuração de hitboxes", KeyCode.F3, () -> gestor.alternarDepuracao());
        ligarAcao("Caixas (alternativa)", KeyCode.H, () -> gestor.alternarDepuracao());
        ligarAtaques();
    }

    @Override
    protected void initGame() {
        gestor = criarGestor();
        getGameWorld().addEntityFactory(new FabricaEntidades(gestor));
        gestor.criarMundo();
        esquerdaPremida = direitaPremida = baixoPremido = cimaPremida = false;
        comando.iniciar();
        botoesComando = 0;
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gestor == null) return;
        int atual = comando.botoes();
        int novos = atual & ~botoesComando;
        boolean mudou = atual != botoesComando;
        botoesComando = atual;
        if (mudou) {
            atualizarMovimentoPremido();
            atualizarBaixo();
        }
        atualizarSaltoPremido();
        if ((novos & 0x4000) != 0) gestor.atacarJogador(AcaoCombate.LP); // X
        if ((novos & 0x8000) != 0) gestor.atacarJogador(AcaoCombate.MP); // Y
        if ((novos & 0x0200) != 0) gestor.atacarJogador(AcaoCombate.HP); // RB
        if ((novos & 0x1000) != 0) gestor.atacarJogador(AcaoCombate.LK); // A
        if ((novos & 0x2000) != 0) gestor.atacarJogador(AcaoCombate.MK); // B
        if ((novos & 0x0100) != 0) gestor.atacarJogador(AcaoCombate.HK); // LB
        if ((novos & 0x0020) != 0) gestor.provocarJogador(); // View/Back
        gestor.atualizar(tpf);
    }

    private void atualizarBaixo() {
        if (gestor != null) gestor.baixoJogador(baixoPremido || (botoesComando & 2) != 0);
    }

    private void atualizarSaltoPremido() {
        if (gestor == null) return;
        boolean cima = cimaPremida || (botoesComando & 1) != 0;
        boolean baixo = baixoPremido || (botoesComando & 2) != 0;
        // Cima+baixo neutraliza; manter cima repete o salto assim que a recuperação acaba.
        if (cima && !baixo) gestor.saltarJogador(direcaoSalto());
    }

    @Override
    protected void initUI() {
        addUINode(criarHudArcade());
        addUINode(criarAjudaControlos());
    }

    private GestorCombate criarGestor() {
        try {
            var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
            return new GestorCombate(animacoes, Path.of("dados", "recordes.csv"), progresso);
        } catch (DadosCombateException e) {
            throw new IllegalStateException("Não foi possível iniciar os dados de combate.", e);
        }
    }

    private void ligarMovimento(String nome, KeyCode tecla, boolean paraDireita) {
        getInput().addAction(new UserAction(nome) {
            @Override protected void onActionBegin() {
                if (paraDireita) {
                    direitaPremida = true;
                } else {
                    esquerdaPremida = true;
                }
                atualizarMovimentoPremido();
            }

            @Override protected void onActionEnd() {
                if (paraDireita) {
                    direitaPremida = false;
                } else {
                    esquerdaPremida = false;
                }
                atualizarMovimentoPremido();
            }
        }, tecla);
    }

    private void ligarAcao(String nome, KeyCode tecla, Runnable acao) {
        getInput().addAction(new UserAction(nome) {
            @Override protected void onActionBegin() {
                acao.run();
            }
        }, tecla);
    }

    private void ligarAtaques() {
        ligarAcao("LP", KeyCode.U, () -> gestor.atacarJogador(AcaoCombate.LP));
        ligarAcao("MP", KeyCode.I, () -> gestor.atacarJogador(AcaoCombate.MP));
        ligarAcao("HP", KeyCode.O, () -> gestor.atacarJogador(AcaoCombate.HP));
        ligarAcao("LK", KeyCode.J, () -> gestor.atacarJogador(AcaoCombate.LK));
        ligarAcao("MK", KeyCode.K, () -> gestor.atacarJogador(AcaoCombate.MK));
        ligarAcao("HK", KeyCode.L, () -> gestor.atacarJogador(AcaoCombate.HK));
    }

    private void atualizarMovimentoPremido() {
        if (gestor == null) return;
        boolean esquerda = esquerdaPremida || (botoesComando & 4) != 0;
        boolean direita = direitaPremida || (botoesComando & 8) != 0;
        if (direita == esquerda) {
            gestor.pararJogador();
        } else if (direita) {
            gestor.moverJogadorParaDireita();
        } else {
            gestor.moverJogadorParaEsquerda();
        }
    }

    private int direcaoSalto() {
        return ((direitaPremida || (botoesComando & 8) != 0) ? 1 : 0)
                - ((esquerdaPremida || (botoesComando & 4) != 0) ? 1 : 0);
    }

    private Pane criarHudArcade() {
        return HudCombate.criar();
    }

    private Pane criarAjudaControlos() {
        return HudCombate.ajuda();
    }

    /** Ponto de entrada para executar pelo Maven ou pelo IntelliJ. */
    public static void main(String[] args) {
        launch(args);
    }
}
