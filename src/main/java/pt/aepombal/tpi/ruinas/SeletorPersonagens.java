package pt.aepombal.tpi.ruinas;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pt.aepombal.tpi.ruinas.modelo.Personagem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Escolha independente por jogador, com idle animado e indicação de imagens provisórias. */
final class SeletorPersonagens extends VBox {
    private final List<List<Image>> poses = List.of(
            carregar(Personagem.LUTADOR_01),
            carregar(Personagem.LUTADOR_02));
    private final ImageView[] imagens = {new ImageView(), new ImageView()};
    private final Button[] cartoes = new Button[2];
    private final Button[] jogadores = new Button[2];
    private final ConfiguracaoPartida config;
    private final Runnable aoAlterar;
    private final Label detalhe = new Label();
    private final Timeline animacao = new Timeline(new KeyFrame(Duration.seconds(8.0 / 60),
            evento -> avancarPose()));
    private int pose;
    private int jogador;

    SeletorPersonagens() {
        this(ConfiguracaoPartida.ATUAL, () -> { });
    }

    SeletorPersonagens(ConfiguracaoPartida config, Runnable aoAlterar) {
        super(8);
        this.config = config;
        this.aoAlterar = aoAlterar;
        setId("seletor-personagens");
        HBox alvos = new HBox(10);
        for (int i = 0; i < jogadores.length; i++) {
            final int indice = i;
            Button alvo = new Button();
            alvo.setId("escolher-jogador-" + (i + 1));
            alvo.setMinHeight(32);
            alvo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(alvo, Priority.ALWAYS);
            alvo.setOnAction(evento -> {
                jogador = indice;
                atualizarEscolhas();
            });
            jogadores[i] = alvo;
            alvos.getChildren().add(alvo);
        }
        HBox lista = new HBox(18);
        lista.setAlignment(Pos.CENTER);
        for (int i = 0; i < 2; i++) {
            final int indice = i;
            ImageView imagem = imagens[i];
            imagem.setImage(poses.get(i).getFirst());
            imagem.setFitWidth(198);
            imagem.setFitHeight(173.25);
            imagem.setPreserveRatio(true);
            imagem.setSmooth(true);
            imagem.setMouseTransparent(true);
            StackPane palco = new StackPane(imagem);
            palco.setMinHeight(173.25);
            palco.setPrefHeight(173.25);
            palco.setMaxHeight(173.25);
            palco.setStyle("-fx-background-color: linear-gradient(to bottom, #122d47, #071321); "
                    + "-fx-background-radius: 5; -fx-border-color: #234762; -fx-border-radius: 5;");
            Label nome = texto("LUTADOR " + String.format("%02d", i + 1), 18, "white");
            Label estado = texto(i == 0 ? "ANIMAÇÕES EXISTENTES" : "ANIMAÇÕES PROVISÓRIAS",
                    11, i == 0 ? "#7BE5C1" : "#FFB66D");
            VBox conteudo = new VBox(5, palco, nome, estado);
            conteudo.setAlignment(Pos.CENTER);
            conteudo.setMouseTransparent(true);
            Button cartao = new Button();
            cartao.setId("personagem-" + (i + 1));
            cartao.setAccessibleText("Lutador " + (i + 1)
                    + (i == 0 ? ", disponível" : ", disponível com animações de baixa resolução"));
            cartao.setGraphic(conteudo);
            cartao.setMinSize(300, 244);
            cartao.setPrefSize(350, 244);
            cartao.setMaxSize(Double.MAX_VALUE, 244);
            HBox.setHgrow(cartao, Priority.ALWAYS);
            cartao.setOnAction(evento -> {
                config.selecionarPersonagem(jogador, Personagem.values()[indice]);
                atualizarEscolhas();
                aoAlterar.run();
            });
            cartao.focusedProperty().addListener((o, anterior, foco) -> {
                if (foco) destacar(indice);
            });
            cartoes[i] = cartao;
            lista.getChildren().add(cartao);
        }
        detalhe.setMinHeight(22);
        detalhe.setStyle("-fx-text-fill: #D9F4FF; -fx-font-size: 13px;");
        Label aviso = texto("Escolhe P1 ou P2/CPU e confirma uma personagem com Enter, A/Cruz ou clique.", 11, "#9DC8EC");
        getChildren().addAll(alvos, lista, detalhe, aviso);
        atualizarEscolhas();
        animacao.setCycleCount(Animation.INDEFINITE);
        // Ao fechar o painel, não deixar um Timeline infinito a reter os sprites.
        sceneProperty().addListener((o, anterior, scene) -> {
            if (scene == null) animacao.stop();
            else animacao.play();
        });
    }

    void focarPrimeiro() {
        cartoes[config.personagem(jogador).ordinal()].requestFocus();
    }

    void avancarPose() {
        pose = (pose + 1) % 12;
        for (int i = 0; i < imagens.length; i++) imagens[i].setImage(poses.get(i).get(pose));
    }

    boolean animacaoAtiva() {
        return animacao.getStatus() == Animation.Status.RUNNING;
    }

    private void atualizarEscolhas() {
        for (int i = 0; i < jogadores.length; i++) {
            jogadores[i].setText((i == 0 ? "P1" : config.versus() ? "P2" : "CPU")
                    + " · " + config.personagem(i).nome().toUpperCase());
            jogadores[i].setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-padding: 5 12; "
                    + "-fx-background-color: " + (i == jogador ? "#9A450C" : "#15334e") + "; "
                    + "-fx-border-color: " + (i == jogador ? EstiloTpi.LARANJA : "#356184") + ";");
        }
        destacar(config.personagem(jogador).ordinal());
    }

    private void destacar(int indice) {
        for (int i = 0; i < cartoes.length; i++) {
            cartoes[i].setStyle("-fx-background-color: " + (i == indice ? "#123451" : "#091c2e")
                    + "; -fx-border-color: " + (i == indice ? EstiloTpi.LARANJA : "#356184")
                    + "; -fx-border-width: 2; -fx-background-radius: 7; -fx-border-radius: 7; "
                    + "-fx-padding: 10; -fx-cursor: hand;");
        }
        detalhe.setText(indice == 0
                ? "Lutador 01 · confirma para usar neste jogador."
                : "Lutador 02 · 15 folhas próprias; MP e stance ainda provisórios.");
    }

    private static Label texto(String valor, int tamanho, String cor) {
        Label label = new Label(valor);
        label.setStyle("-fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: "
                + tamanho + "px; -fx-text-fill: " + cor + ";");
        return label;
    }

    private static List<Image> carregar(Personagem personagem) {
        List<Image> resultado = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String caminho = personagem.caminhoIdle(i);
            try (var stream = SeletorPersonagens.class.getResourceAsStream(caminho)) {
                if (stream == null) throw new IllegalStateException("Falta idle: " + caminho);
                Image image = new Image(stream);
                if (image.isError()) throw new IllegalStateException("PNG inválido: " + caminho);
                resultado.add(image);
            } catch (IOException e) {
                throw new IllegalStateException("Não foi possível ler " + caminho, e);
            }
        }
        return List.copyOf(resultado);
    }
}
