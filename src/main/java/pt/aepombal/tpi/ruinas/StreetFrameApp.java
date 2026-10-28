package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.input.UserAction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
import static com.almasb.fxgl.dsl.FXGL.getip;
import static com.almasb.fxgl.dsl.FXGL.getsp;

// [IA] Classe de arranque: só configura FXGL, liga controlos e delega regras no GestorCombate.
/** Aplicação FXGL do jogo de luta baseado em frames. */
public final class StreetFrameApp extends GameApplication {

    private GestorCombate gestor;
    private final ProgressoCombate progresso = new ProgressoCombate();
    private boolean esquerdaPremida;
    private boolean direitaPremida;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Street Frame Fighter");
        settings.setVersion("0.1");
        settings.setWidth(960);
        settings.setHeight(560);
        settings.setMainMenuEnabled(true);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("vidaJogador", 100);
        vars.put("vidaOponente", 100);
        vars.put("guardaJogador", 100);
        vars.put("guardaOponente", 100);
        vars.put("pontos", 0);
        vars.put("acaoJogador", "EM GUARDA");
        vars.put("acaoOponente", "EM GUARDA");
        vars.put("dificuldade", "EQUILIBRADO");
        vars.put("frameData", "FRAME ADVANTAGE  —");
    }

    @Override
    protected void initInput() {
        ligarMovimento("Esquerda", KeyCode.A, false);
        ligarMovimento("Direita", KeyCode.D, true);
        ligarAcao("Saltar", KeyCode.W, () -> gestor.saltarJogador(direcaoSalto()));
        ligarAcao("Bloqueio alto", KeyCode.Q, () -> gestor.bloquearJogador(false));
        ligarAcao("Bloqueio baixo", KeyCode.S, () -> gestor.bloquearJogador(true));
        ligarAcao("Depuração de hitboxes", KeyCode.F3, () -> gestor.alternarDepuracao());
        ligarAtaques();
    }

    @Override
    protected void initGame() {
        gestor = criarGestor();
        getGameWorld().addEntityFactory(new FabricaEntidades(gestor));
        gestor.criarMundo();
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
        if (direitaPremida == esquerdaPremida) {
            gestor.pararJogador();
        } else if (direitaPremida) {
            gestor.moverJogadorParaDireita();
        } else {
            gestor.moverJogadorParaEsquerda();
        }
    }

    private int direcaoSalto() {
        return (direitaPremida ? 1 : 0) - (esquerdaPremida ? 1 : 0);
    }

    private Pane criarHudArcade() {
        Pane hud = new Pane();
        hud.setPrefSize(960, 122);

        Rectangle fundo = new Rectangle(960, 122);
        fundo.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#030714", 0.97)),
                new Stop(1, Color.web("#111B38", 0.88))));
        Line bordo = new Line(0, 121, 960, 121);
        bordo.setStroke(Color.web("#F5B82E"));
        bordo.setStrokeWidth(3);

        Text titulo = texto("STREET FRAME FIGHTER", 365, 18, 17, Color.web("#FFE777"), true);
        titulo.setStroke(Color.web("#8E2D36"));
        titulo.setStrokeWidth(1.2);

        Text nomeJogador = texto("PLAYER 1", 30, 31, 18, Color.WHITE, true);
        Text nomeCpu = texto("CPU", 884, 31, 18, Color.WHITE, true);

        Group vidaJogador = criarBarraVida(getip("vidaJogador"), 30, 39, false);
        Group vidaCpu = criarBarraVida(getip("vidaOponente"), 572, 39, true);
        Group guardaJogador = criarBarraGuarda(getip("guardaJogador"), 30, 75, false);
        Group guardaCpu = criarBarraGuarda(getip("guardaOponente"), 572, 75, true);
        Group estadoJogador = criarIndicadorAcao("acaoJogador", 30, 90, false, Color.web("#27D8FF"));
        Group estadoCpu = criarIndicadorAcao("acaoOponente", 690, 90, true, Color.web("#FF4E68"));

        Polygon losango = new Polygon(480, 25, 518, 56, 480, 87, 442, 56);
        losango.setFill(Color.web("#F4B52C"));
        losango.setStroke(Color.web("#FFF1A6"));
        losango.setStrokeWidth(3);
        losango.setEffect(new DropShadow(12, Color.web("#FF5A36", 0.75)));
        Polygon centro = new Polygon(480, 31, 510, 56, 480, 81, 450, 56);
        centro.setFill(Color.web("#172040"));
        Text vs = texto("VS", 462, 65, 24, Color.WHITE, true);
        vs.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.ITALIC, 24));

        Text pontos = texto("", 250, 30, 11, Color.web("#FFD75A"), true);
        pontos.textProperty().bind(getip("pontos").asString("SCORE  %06d"));

        Text nivel = texto("", 704, 30, 10, Color.web("#FF9CAF"), true);
        nivel.textProperty().bind(Bindings.concat("IA  ", getsp("dificuldade")));

        Text frameData = texto("", 393, 108, 11, Color.web("#8FF7FF"), true);
        frameData.setWrappingWidth(174);
        frameData.setTextAlignment(TextAlignment.CENTER);
        frameData.textProperty().bind(getsp("frameData"));

        hud.getChildren().addAll(fundo, bordo, titulo, nomeJogador, nomeCpu,
                vidaJogador, vidaCpu, guardaJogador, guardaCpu, estadoJogador, estadoCpu,
                losango, centro, vs, pontos, nivel, frameData);
        return hud;
    }

    private Group criarBarraVida(IntegerProperty vida, double x, double y, boolean inversa) {
        final double largura = 358;
        final double larguraInterior = largura - 10;

        Rectangle moldura = new Rectangle(x, y, largura, 33);
        moldura.setArcWidth(8);
        moldura.setArcHeight(8);
        moldura.setFill(Color.web("#050914"));
        moldura.setStroke(Color.web("#F4C04B"));
        moldura.setStrokeWidth(3);
        moldura.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.8)));

        Rectangle dano = new Rectangle(x + 5, y + 5, larguraInterior, 23);
        dano.setFill(Color.web("#5D132A"));
        dano.setArcWidth(5);
        dano.setArcHeight(5);

        Rectangle preenchimento = new Rectangle(x + 5, y + 5, 0, 23);
        preenchimento.setArcWidth(5);
        preenchimento.setArcHeight(5);
        preenchimento.widthProperty().bind(vida.multiply(larguraInterior / 100.0));
        preenchimento.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#FFF7A0")),
                new Stop(0.45, Color.web("#FFD83D")),
                new Stop(1, Color.web("#F78A1D"))));

        Rectangle brilho = new Rectangle(x + 5, y + 7, 0, 5);
        brilho.setFill(Color.color(1, 1, 1, 0.42));
        brilho.widthProperty().bind(preenchimento.widthProperty());

        if (inversa) {
            preenchimento.xProperty().bind(Bindings.subtract(x + 5 + larguraInterior,
                    preenchimento.widthProperty()));
            brilho.xProperty().bind(Bindings.subtract(x + 5 + larguraInterior,
                    brilho.widthProperty()));
        }

        Group marcas = new Group();
        for (int i = 1; i < 10; i++) {
            double marcaX = x + 5 + i * larguraInterior / 10.0;
            Line marca = new Line(marcaX, y + 7, marcaX, y + 26);
            marca.setStroke(Color.color(0.05, 0.07, 0.12, 0.28));
            marca.setStrokeWidth(1);
            marcas.getChildren().add(marca);
        }

        Text numero = texto("", inversa ? x + 307 : x + 13, y + 25, 17, Color.WHITE, true);
        numero.textProperty().bind(vida.asString("%03d"));
        numero.setStroke(Color.web("#4A2510"));
        numero.setStrokeWidth(0.7);

        return new Group(moldura, dano, preenchimento, brilho, marcas, numero);
    }

    private Group criarBarraGuarda(IntegerProperty guarda, double x, double y, boolean inversa) {
        final double largura = 358;
        final double larguraInterior = largura - 4;

        Rectangle fundo = new Rectangle(x, y, largura, 11);
        fundo.setArcWidth(7);
        fundo.setArcHeight(7);
        fundo.setFill(Color.web("#11162A"));
        fundo.setStroke(Color.web("#56627F"));

        Rectangle preenchimento = new Rectangle(x + 2, y + 2, 0, 7);
        preenchimento.setArcWidth(5);
        preenchimento.setArcHeight(5);
        preenchimento.widthProperty().bind(guarda.multiply(larguraInterior / 100.0));
        preenchimento.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#835DFF")),
                new Stop(0.55, Color.web("#28D7F2")),
                new Stop(1, Color.web("#B8FFFF"))));
        if (inversa) {
            preenchimento.xProperty().bind(Bindings.subtract(x + 2 + larguraInterior,
                    preenchimento.widthProperty()));
        }

        Text valor = texto("", inversa ? x + 286 : x + 8, y + 9, 8, Color.WHITE, true);
        valor.setWrappingWidth(64);
        valor.setTextAlignment(inversa ? TextAlignment.RIGHT : TextAlignment.LEFT);
        valor.textProperty().bind(guarda.asString("BLOCK %03d"));
        return new Group(fundo, preenchimento, valor);
    }

    private Group criarIndicadorAcao(String variavel, double x, double y,
                                      boolean direita, Color cor) {
        Rectangle fundo = new Rectangle(x, y, 240, 23);
        fundo.setArcWidth(14);
        fundo.setArcHeight(14);
        fundo.setFill(Color.web("#071020", 0.9));
        fundo.setStroke(cor);
        fundo.setStrokeWidth(1.4);

        Text estado = texto("", x + 12, y + 17, 11, cor, true);
        estado.setWrappingWidth(216);
        estado.setTextAlignment(direita ? TextAlignment.RIGHT : TextAlignment.LEFT);
        estado.textProperty().bind(getsp(variavel));
        return new Group(fundo, estado);
    }

    private Pane criarAjudaControlos() {
        Pane ajuda = new Pane();
        ajuda.setPrefSize(960, 54);
        ajuda.setTranslateY(506);

        Rectangle fundo = new Rectangle(960, 54, Color.web("#030713", 0.94));
        Line topo = new Line(0, 1, 960, 1);
        topo.setStroke(Color.web("#27D8FF", 0.72));
        topo.setStrokeWidth(2);

        Text linha1 = texto("A  ESQUERDA     D  DIREITA     RECUAR  =  BLOCK     W / A+W / D+W  SALTO",
                105, 23, 12, Color.web("#F7D95B"), true);
        Text linha2 = texto("U I O  SOCOS     J K L  PONTAPÉS     F3  MOSTRAR CAIXAS     ESC  PAUSA",
                166, 43, 11, Color.web("#D9E8FF"), false);
        ajuda.getChildren().addAll(fundo, topo, linha1, linha2);
        return ajuda;
    }

    private Text texto(String conteudo, double x, double y, double tamanho,
                       Color cor, boolean negrito) {
        Text texto = new Text(x, y, conteudo);
        texto.setFill(cor);
        texto.setFont(Font.font("Arial", negrito ? FontWeight.EXTRA_BOLD : FontWeight.NORMAL, tamanho));
        return texto;
    }

    /** Ponto de entrada para executar pelo Maven ou pelo IntelliJ. */
    public static void main(String[] args) {
        launch(args);
    }
}
