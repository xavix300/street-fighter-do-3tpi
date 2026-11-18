package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.DadosCombateException;
import pt.aepombal.tpi.ruinas.modelo.RepositorioFrames;
import pt.aepombal.tpi.ruinas.modelo.ProgressoCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;

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
    private static StreetFrameApp instancia;
    private final EntradasPartida entradas = new EntradasPartida();
    private final int[] anteriores = new int[2];
    private boolean sincronizar = true;

    public StreetFrameApp() { instancia = this; }
    static StreetFrameApp instancia() { return instancia; }

    void aplicarPersonagens() {
        if (gestor == null) return;
        gestor.trocarPersonagem(LadoLutador.JOGADOR, ConfiguracaoPartida.ATUAL.personagem(0));
        gestor.trocarPersonagem(LadoLutador.OPONENTE, ConfiguracaoPartida.ATUAL.personagem(1));
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("TPI Fighter");
        settings.setVersion("0.1");
        settings.setWidth(960);
        settings.setHeight(560);
        settings.setMainMenuEnabled(true);
        settings.setCloseConfirmation(false);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.addEngineService(ServicoControlos.class);
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
        getInput().addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, evento -> {
            if (entradas.premir(evento.getCode()) && gestor != null
                    && (evento.getCode() == KeyCode.F3 || evento.getCode() == KeyCode.H)) {
                gestor.alternarDepuracao();
            }
        });
        getInput().addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, evento -> {
            entradas.libertar(evento.getCode());
        });
    }

    @Override
    protected void initGame() {
        gestor = criarGestor();
        getGameWorld().addEntityFactory(new FabricaEntidades(gestor));
        gestor.criarMundo();
        limparTeclado();
    }

    @Override
    protected void onUpdate(double tpf) {
        if (gestor == null) return;
        var comandos = com.almasb.fxgl.dsl.FXGL.getService(ServicoControlos.class);
        var config = ConfiguracaoPartida.ATUAL;
        for (int i = 0; i < (gestor.doisJogadores() ? 2 : 1); i++) {
            int atual = entradas.botoes(config, i, comandos.botoes(0), comandos.botoes(1));
            int novos = sincronizar ? 0 : atual & ~anteriores[i];
            anteriores[i] = atual;
            if ((novos & 0x10) != 0) {
                com.almasb.fxgl.dsl.FXGL.getGameController().gotoGameMenu();
                return;
            }
            aplicarEntrada(i == 0 ? LadoLutador.JOGADOR : LadoLutador.OPONENTE, atual, novos);
        }
        sincronizar = false;
        gestor.atualizar(tpf);
    }

    private void aplicarEntrada(LadoLutador lado, int botoes, int novos) {
        gestor.moverHumano(lado, EntradasPartida.direcao(botoes));
        gestor.baixoHumano(lado, (botoes & 2) != 0);
        if (EntradasPartida.saltar(botoes)) gestor.saltarHumano(lado, EntradasPartida.direcao(botoes));
        if ((novos & 0x4000) != 0) gestor.atacarHumano(lado, AcaoCombate.LP);
        if ((novos & 0x8000) != 0) gestor.atacarHumano(lado, AcaoCombate.MP);
        if ((novos & 0x0200) != 0) gestor.atacarHumano(lado, AcaoCombate.HP);
        if ((novos & 0x1000) != 0) gestor.atacarHumano(lado, AcaoCombate.LK);
        if ((novos & 0x2000) != 0) gestor.atacarHumano(lado, AcaoCombate.MK);
        if ((novos & 0x0100) != 0) gestor.atacarHumano(lado, AcaoCombate.HK);
        if ((novos & 0x0020) != 0) gestor.provocarHumano(lado);
    }

    void limparTeclado() {
        entradas.limpar();
        sincronizar = true;
    }

    @Override
    protected void initUI() {
        addUINode(criarHudArcade());
        addUINode(criarAjudaControlos());
    }

    private GestorCombate criarGestor() {
        try {
            var animacoes = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
            return new GestorCombate(animacoes, Path.of("dados", "recordes.csv"), progresso,
                    ConfiguracaoPartida.ATUAL.versus());
        } catch (DadosCombateException e) {
            throw new IllegalStateException("Não foi possível iniciar os dados de combate.", e);
        }
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
