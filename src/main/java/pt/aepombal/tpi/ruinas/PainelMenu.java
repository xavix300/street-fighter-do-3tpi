package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Painéis do menu com a mesma paleta azul e navegação por teclado. */
final class PainelMenu extends StackPane {
    private final VBox corpo = new VBox(14);
    private final Runnable aoFechar;
    private static final String CSS_BOTAO = "-fx-shape: 'M 20 0 L 320 0 L 300 64 L 0 64 Z'; "
            + "-fx-scale-shape: true; -fx-background-radius: 0; -fx-border-width: 1.5; "
            + "-fx-background-insets: 0; -fx-border-insets: 0; "
            + "-fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 16px; "
            + "-fx-font-weight: 900; -fx-padding: 13 25; -fx-graphic-text-gap: 13; "
            + "-fx-alignment: center-left; -fx-cursor: hand; ";

    private PainelMenu(String titulo, Runnable aoFechar) {
        this.aoFechar = aoFechar;
        setPrefSize(960, 560);
        setMinSize(960, 560);
        setMaxSize(960, 560);
        setStyle("-fx-background-color: rgba(3, 19, 35, 0.58); -fx-font-family: 'Segoe UI'; "
                + "-fx-accent: #FF8A24; -fx-focus-color: #FFB66D; -fx-faint-focus-color: transparent;");
        VBox cartao = new VBox(18);
        cartao.setPadding(new Insets(24, 48, 22, 48));
        cartao.setMaxSize(820, 480);
        cartao.setStyle("-fx-shape: 'M 28 0 L 820 0 L 792 480 L 0 480 Z'; "
                + "-fx-background-color: linear-gradient(to bottom right, rgba(8,33,58,0.96), rgba(4,18,33,0.94)); "
                + "-fx-border-color: linear-gradient(to bottom right, #0784FF, #BBDFFF, #FF8A24); "
                + "-fx-border-width: 2; -fx-effect: dropshadow(gaussian, #0084FF66, 22, 0.15, 0, 0);");
        Label marca = label("TPI FIGHTER  /  CAMPUS", 11, EstiloTpi.LARANJA);
        Label cabecalho = label(titulo, 28, "white");
        cabecalho.setStyle("-fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 30px; "
                + "-fx-font-weight: 900; -fx-font-style: italic;");
        javafx.scene.shape.Polygon sublinhado = new javafx.scene.shape.Polygon(0, 0, 315, 0, 303, 4, 0, 4);
        sublinhado.setFill(new javafx.scene.paint.LinearGradient(0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web(EstiloTpi.AZUL)),
                new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web(EstiloTpi.LARANJA))));
        corpo.setFillWidth(true);
        VBox.setVgrow(corpo, Priority.ALWAYS);
        Button voltar = botao("‹  VOLTAR", this::fechar);
        cartao.getChildren().addAll(marca, cabecalho, sublinhado, corpo, voltar);
        getChildren().addAll(cartao, EstiloTpi.cantos());
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == KeyCode.ESCAPE) {
                fechar();
                evento.consume();
            } else if (NavegacaoMenu.tecla(this, evento.getCode())) {
                evento.consume();
            }
        });
        Platform.runLater(voltar::requestFocus);
    }

    static void mensagem(Pane destino, String titulo, String mensagem) {
        PainelMenu painel = new PainelMenu(titulo, () -> destino.requestFocus());
        painel.corpo.getChildren().add(label(mensagem, 17, "#D9F4FF"));
        destino.getChildren().add(painel);
    }

    static void personagens(Pane destino) {
        personagens(destino, () -> { });
    }

    static void personagens(Pane destino, Runnable aoAlterar) {
        PainelMenu painel = new PainelMenu("PERSONAGENS", () -> destino.requestFocus());
        VBox cartao = (VBox) painel.getChildren().getFirst();
        cartao.setSpacing(6);
        cartao.setPadding(new Insets(12, 36, 12, 36));
        cartao.setMaxSize(860, 520);
        SeletorPersonagens seletor = new SeletorPersonagens(ConfiguracaoPartida.ATUAL, aoAlterar);
        painel.corpo.getChildren().add(seletor);
        destino.getChildren().add(painel);
        Platform.runLater(seletor::focarPrimeiro);
    }

    static void confirmar(Pane destino, String titulo, String mensagem, Runnable acao) {
        PainelMenu painel = new PainelMenu(titulo, () -> destino.requestFocus());
        painel.corpo.getChildren().addAll(label(mensagem, 17, "#D9F4FF"),
                botao("CONFIRMAR", () -> { painel.fechar(); acao.run(); }));
        destino.getChildren().add(painel);
    }

    static void definicoes(Pane destino) {
        PainelMenu painel = new PainelMenu("DEFINIÇÕES", () -> destino.requestFocus());
        HBox separadores = new HBox(10);
        VBox conteudo = new VBox(12);
        Button[] tabs = new Button[3];
        String[] nomes = {"CONTROLOS", "VÍDEO", "ÁUDIO"};
        for (int i = 0; i < tabs.length; i++) {
            final int indice = i;
            tabs[i] = botao(nomes[i], () -> painel.selecionar(indice, tabs, conteudo));
            tabs[i].setPrefWidth(220);
            separadores.getChildren().add(tabs[i]);
        }
        painel.corpo.getChildren().addAll(separadores, conteudo);
        painel.selecionar(0, tabs, conteudo);
        destino.getChildren().add(painel);
    }

    static void escolherPartida(Pane destino, Runnable iniciar) {
        PainelMenu painel = new PainelMenu("JOGAR", () -> destino.requestFocus());
        VBox cartao = (VBox) painel.getChildren().getFirst();
        cartao.setSpacing(8);
        cartao.setPadding(new Insets(16, 36, 16, 36));
        cartao.setMaxSize(860, 520);
        ConfiguracaoPartida config = ConfiguracaoPartida.ATUAL;
        Button modo = botao("", () -> { });
        Button p1 = botao("", () -> { });
        Button p2 = botao("", () -> { });
        Label ajuda = label("", 13, "#9DC8EC");
        Runnable atualizar = () -> {
            modo.setText(config.versus() ? "MODO: 2 JOGADORES — VERSUS LOCAL" : "MODO: 1 JOGADOR — CONTRA CPU");
            p1.setText("P1: " + config.descricao(0));
            p2.setText("P2: " + (config.versus() ? config.descricao(1) : "CPU"));
            p1.setDisable(!config.versus());
            p2.setDisable(!config.versus());
            ajuda.setText(config.versus()
                    ? "Seleciona cada linha para mudar a fonte. Teclado 2: setas, Num 789 / 456 e Num 0.\n"
                        + "Dois teclados usam teclas diferentes. Ativa Num Lock. Comandos: A/Cruz confirma, B/Círculo volta."
                    : "WASD + UIO / JKL ou Comando 1. Start/Options abre a pausa.\n"
                        + "Seleciona MODO para jogar contra outra pessoa.");
        };
        modo.setOnAction(e -> { config.alternarModo(); atualizar.run(); });
        p1.setOnAction(e -> { config.seguinteFonte(0); atualizar.run(); });
        p2.setOnAction(e -> { config.seguinteFonte(1); atualizar.run(); });
        Button jogar = botao("INICIAR COMBATE", () -> {
            if (config.versus()) {
                ServicoControlos comandos = FXGL.getService(ServicoControlos.class);
                for (int i = 0; i < 2; i++) {
                    var fonte = config.fonte(i);
                    int indice = fonte == ConfiguracaoPartida.Fonte.COMANDO_1 ? 0 : 1;
                    if ((fonte == ConfiguracaoPartida.Fonte.COMANDO_1
                            || fonte == ConfiguracaoPartida.Fonte.COMANDO_2) && !comandos.ligado(indice)) {
                        ajuda.setText("Liga o " + fonte.nome + " ou escolhe um teclado antes de iniciar.");
                        return;
                    }
                }
            }
            painel.fechar();
            iniciar.run();
        });
        for (Button botao : new Button[]{modo, p1, p2, jogar}) {
            botao.getStyleClass().add("botao-compacto");
            botao.setMinHeight(40);
            botao.setPrefHeight(40);
            botao.setMaxHeight(40);
            pintarBotao(botao);
        }
        painel.corpo.setSpacing(8);
        painel.corpo.getChildren().addAll(modo, p1, p2, ajuda, jogar);
        atualizar.run();
        destino.getChildren().add(painel);
        Platform.runLater(modo::requestFocus);
    }

    private void selecionar(int indice, Button[] tabs, VBox conteudo) {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setUserData(i == indice);
            pintarBotao(tabs[i]);
        }
        conteudo.getChildren().clear();
        switch (indice) {
            case 0 -> {
                conteudo.getChildren().addAll(
                        linha("A / D", "Mover · recuar para defender"),
                        linha("W  /  S", "Saltar / agachar · S + recuar: defesa baixa"),
                        linha("U I O  /  J K L", "Socos / pontapés (leve, médio, forte)"),
                        linha("T  /  F3 ou H  /  ESC", "Provocação / caixas de combate / pausa"),
                        label("Xbox: direcional · X/Y/RB socos · A/B/LB pontapés.", 12, "#9DC8EC"),
                         label("PS5: direcional · Quadrado/Triângulo/R1 socos · Cruz/Círculo/L1 pontapés.", 12, "#9DC8EC"));
            }
            case 1 -> {
                CheckBox ecra = new CheckBox("Ecrã inteiro");
                ecra.setStyle("-fx-text-fill: white; -fx-font-size: 17px;");
                if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
                    ecra.setSelected(stage.isFullScreen());
                }
                ecra.setOnAction(e -> {
                    if (getScene().getWindow() instanceof Stage stage) stage.setFullScreen(ecra.isSelected());
                });
                conteudo.getChildren().addAll(label("MODO DE APRESENTAÇÃO", 12, "#8CCFFF"), ecra,
                        linha("960 × 560", "Resolução base do jogo"),
                        label("Em ecrã inteiro, o jogo adapta a imagem ao tamanho do ecrã.", 15, "#D9F4FF"));
            }
            case 2 -> {
                var settings = FXGL.getSettings();
                conteudo.getChildren().addAll(
                        volume("Música", settings.getGlobalMusicVolume(), settings::setGlobalMusicVolume),
                        volume("Efeitos sonoros", settings.getGlobalSoundVolume(), settings::setGlobalSoundVolume),
                        label("As alterações são aplicadas imediatamente ao áudio do jogo.", 13, "#9DC8EC"));
            }
            default -> throw new IllegalArgumentException("Separador desconhecido");
        }
    }

    private static Node volume(String nome, double inicial, java.util.function.DoubleConsumer alterar) {
        Slider slider = new Slider(0, 1, inicial);
        slider.setBlockIncrement(.05);
        slider.setPrefWidth(460);
        Label valor = label("", 15, "white");
        valor.textProperty().bind(slider.valueProperty().multiply(100).asString("%.0f %%"));
        slider.valueProperty().addListener((o, antes, depois) -> alterar.accept(depois.doubleValue()));
        HBox barra = new HBox(18, slider, valor);
        barra.setAlignment(Pos.CENTER_LEFT);
        return new VBox(8, label(nome, 16, "#D9F4FF"), barra);
    }

    private static Node linha(String teclas, String descricao) {
        Label chave = label(teclas, 13, "#8CCFFF");
        chave.setMinWidth(190);
        return new HBox(12, chave, label(descricao, 14, "#D9F4FF"));
    }

    private static Label label(String texto, int tamanho, String cor) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + cor + "; -fx-font-size: " + tamanho + "px;");
        return label;
    }

    static Button botao(String texto, Runnable acao) {
        Button botao = new Button(texto);
        String simbolo = texto.contains("CONTROLOS") ? "✥" : texto.contains("VÍDEO") ? "▣"
                : texto.contains("ÁUDIO") ? "♫" : texto.contains("VOLTAR") ? "◀" : "▶";
        Label icone = label(simbolo, 20, "white");
        icone.setMinWidth(22);
        botao.setGraphic(icone);
        botao.setMinHeight(48);
        botao.setMinWidth(200);
        pintarBotao(botao);
        botao.hoverProperty().addListener((o, a, b) -> pintarBotao(botao));
        botao.focusedProperty().addListener((o, a, b) -> pintarBotao(botao));
        botao.setOnAction(e -> acao.run());
        return botao;
    }

    private static void pintarBotao(Button botao) {
        boolean ativo = Boolean.TRUE.equals(botao.getUserData()) || botao.isHover() || botao.isFocused();
        // O padding inline prevalece sobre setPadding: manter a variante compacta
        // também quando foco/hover voltam a aplicar o estilo.
        String compacto = botao.getStyleClass().contains("botao-compacto") ? "-fx-padding: 6 20; " : "";
        botao.setStyle(CSS_BOTAO + compacto + (ativo
                ? "-fx-background-color: linear-gradient(to bottom, #F6943B, #B94A0B, #DC6314); "
                    + "-fx-border-color: #FFD1A1; -fx-border-width: 2; "
                    + "-fx-effect: dropshadow(gaussian, #FF8A2499, 13, 0.25, 0, 0);"
                : "-fx-background-color: linear-gradient(to bottom, rgba(18,43,65,0.96), rgba(3,19,34,0.92)); "
                    + "-fx-border-color: #9EB6C9; -fx-effect: dropshadow(gaussian, #00132588, 5, 0, 0, 2);"));
    }

    static boolean aberto(Pane destino) {
        return destino.getChildren().stream().anyMatch(PainelMenu.class::isInstance);
    }

    static void fecharAtual(Pane destino) {
        destino.getChildren().stream().filter(PainelMenu.class::isInstance)
                .map(PainelMenu.class::cast).findFirst().ifPresent(PainelMenu::fechar);
    }

    static boolean comandoAtual(Pane destino, int botoes) {
        var painel = destino.getChildren().stream().filter(PainelMenu.class::isInstance)
                .map(PainelMenu.class::cast).findFirst();
        painel.ifPresent(p -> NavegacaoMenu.comando(p, botoes, p::fechar));
        return painel.isPresent();
    }

    private void fechar() {
        if (getParent() instanceof Pane pai) pai.getChildren().remove(this);
        aoFechar.run();
    }
}
