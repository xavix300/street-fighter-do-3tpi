package pt.aepombal.tpi.ruinas;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;

import static com.almasb.fxgl.dsl.FXGL.getip;
import static com.almasb.fxgl.dsl.FXGL.getsp;

/** Apresentação do combate, sem alterar as regras ou os valores dos lutadores. */
final class HudCombate {
    private HudCombate() { }

    static Pane criar() {
        Pane hud = new Pane();
        hud.setPrefSize(960, 120);
        hud.setMouseTransparent(true);
        Rectangle sombra = new Rectangle(960, 146);
        sombra.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#071B30", .96)),
                new Stop(.65, Color.web("#071B30", .8)), new Stop(1, Color.TRANSPARENT)));
        hud.getChildren().add(sombra);
        hud.getChildren().addAll(lado(false), lado(true));

        Polygon centro = new Polygon(448, 24, 529, 24, 512, 93, 431, 93);
        centro.setFill(Color.web("#0B2743"));
        centro.setStroke(Color.web(EstiloTpi.LARANJA));
        hud.getChildren().addAll(centro,
                texto("VERSUS", 440, 40, 80, 10, EstiloTpi.LARANJA, TextAlignment.CENTER),
                texto("VS", 440, 76, 80, 31, "#F2F0E8", TextAlignment.CENTER),
                texto("TPI FIGHTER  /  OFICINA", 330, 15, 300, 9, "#ACB6C2", TextAlignment.CENTER));
        Text frames = texto("", 365, 115, 230, 9, "#BBC9D5", TextAlignment.CENTER);
        frames.textProperty().bind(getsp("frameData"));
        hud.getChildren().addAll(frames, EstiloTpi.cantos());
        return hud;
    }

    private static Group lado(boolean cpu) {
        double x = cpu ? 554 : 32;
        String cor = cpu ? EstiloTpi.LARANJA : EstiloTpi.AZUL;
        TextAlignment alinhamento = cpu ? TextAlignment.RIGHT : TextAlignment.LEFT;
        Text nome = texto(cpu ? "CPU" : "JOGADOR 01", x, 36, 374, 19, "#F4F2EC", alinhamento);
        Text detalhe = texto("", x, 19, 374, 9, "#A8B4C1", alinhamento);
        if (cpu) detalhe.textProperty().bind(Bindings.concat("OPONENTE  /  ", getsp("dificuldade")));
        else detalhe.textProperty().bind(getip("pontos").asString("JOGADOR  /  SCORE %06d"));
        IntegerProperty vida = getip(cpu ? "vidaOponente" : "vidaJogador");
        Group barra = barraVida(vida, x, cpu, cor);
        Rectangle trilho = new Rectangle(x, 79, 374, 4);
        trilho.setFill(Color.web("#24303D"));
        Rectangle guarda = new Rectangle(x, 79, 374, 4);
        guarda.setFill(Color.web(cor));
        guarda.widthProperty().bind(getip(cpu ? "guardaOponente" : "guardaJogador").multiply(3.74));
        if (cpu) guarda.xProperty().bind(Bindings.subtract(x + 374, guarda.widthProperty()));
        Text estado = texto("", x, 101, 374, 10, "#CDD5DF", alinhamento);
        estado.textProperty().bind(getsp(cpu ? "acaoOponente" : "acaoJogador"));
        Text valor = texto("", cpu ? x : x + 240, 73, 134, 9, "#DCE4EB",
                cpu ? TextAlignment.LEFT : TextAlignment.RIGHT);
        valor.textProperty().bind(vida.asString("%d / " + ComponenteLutador.VIDA_MAXIMA));
        return new Group(nome, detalhe, barra, trilho, guarda, estado, valor);
    }

    private static Group barraVida(IntegerProperty vida, double x, boolean inversa, String cor) {
        Rectangle moldura = new Rectangle(x - 2, 43, 378, 23);
        moldura.setFill(Color.web("#0A1019"));
        moldura.setStroke(Color.web("#9ACAF0"));
        Rectangle vazio = new Rectangle(x, 45, 374, 19);
        vazio.setFill(Color.web("#35262C"));
        DoubleProperty vidaAnterior = new SimpleDoubleProperty(vida.get());
        Rectangle dano = new Rectangle(x, 45, 374, 19);
        dano.setFill(Color.web("#E8B675"));
        dano.widthProperty().bind(vidaAnterior.multiply(374.0 / ComponenteLutador.VIDA_MAXIMA));
        Timeline atraso = new Timeline();
        vida.addListener((obs, antes, depois) -> {
            atraso.stop();
            if (depois.intValue() >= antes.intValue()) {
                vidaAnterior.set(depois.doubleValue());
            } else {
                atraso.getKeyFrames().setAll(
                        new KeyFrame(Duration.millis(280), new KeyValue(vidaAnterior, vidaAnterior.get())),
                        new KeyFrame(Duration.millis(850), new KeyValue(vidaAnterior, depois.doubleValue())));
                atraso.playFromStart();
            }
        });
        Rectangle atual = new Rectangle(x, 45, 374, 19);
        atual.widthProperty().bind(vida.multiply(374.0 / ComponenteLutador.VIDA_MAXIMA));
        atual.fillProperty().bind(Bindings.createObjectBinding(() ->
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web(vida.get() <= ComponenteLutador.VIDA_MAXIMA * .25 ? "#FF9279" : cor)),
                        new Stop(1, Color.web(vida.get() <= ComponenteLutador.VIDA_MAXIMA * .25 ? "#B33538" : inversa ? "#A9460C" : "#0046A8"))), vida));
        if (inversa) {
            atual.xProperty().bind(Bindings.subtract(x + 374, atual.widthProperty()));
            dano.xProperty().bind(Bindings.subtract(x + 374, dano.widthProperty()));
        }
        Group grupo = new Group(moldura, vazio, dano, atual);
        for (int i = 1; i < 4; i++) {
            Line marca = new Line(x + i * 93.5, 46, x + i * 93.5, 63);
            marca.setStroke(Color.web("#0A1019", .35));
            grupo.getChildren().add(marca);
        }
        return grupo;
    }

    static Pane ajuda() {
        Pane ajuda = new Pane();
        ajuda.setMouseTransparent(true);
        ajuda.setTranslateY(528);
        Rectangle fundo = new Rectangle(960, 32, Color.web("#071B30", .86));
        Text controlos = texto("A / D  MOVER     W  SALTAR     S + TRÁS  DEFENDER     U I O  SOCOS     J K L  PONTAPÉS     ESC  PAUSA",
                24, 20, 912, 10, "#B5C0CC", TextAlignment.CENTER);
        Line linha = new Line(0, 0, 960, 0);
        linha.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(EstiloTpi.AZUL)), new Stop(1, Color.web(EstiloTpi.LARANJA))));
        linha.setStrokeWidth(2);
        ajuda.getChildren().addAll(fundo, linha, controlos);
        return ajuda;
    }

    private static Text texto(String valor, double x, double y, double largura,
                              double tamanho, String cor, TextAlignment alinhamento) {
        Text texto = new Text(x, y, valor);
        texto.setWrappingWidth(largura);
        texto.setTextAlignment(alinhamento);
        texto.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, tamanho));
        texto.setFill(Color.web(cor));
        return texto;
    }
}
