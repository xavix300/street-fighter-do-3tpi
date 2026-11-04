package pt.aepombal.tpi.ruinas.componentes;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Representação vetorial temporária de um lutador.
 * As poses tornam cada ação legível enquanto os sprites fotográficos não estão recortados.
 */
public final class VistaLutador extends Group {

    private final Group corpo = new Group();
    private static final Map<AcaoCombate, List<Image>> SPRITES = carregarSprites();
    private static final Map<AcaoCombate, List<Image>> ATAQUES_40 = carregarAtaques40();
    private final ImageView sprite = new ImageView();
    private final Line bracoTras = membro();
    private final Line bracoFrente = membro();
    private final Line pernaTras = membro();
    private final Line pernaFrente = membro();
    private final Circle maoTras = new Circle(5);
    private final Circle maoFrente = new Circle(5);
    private int orientacao;

    public VistaLutador(LadoLutador lado) {
        getTransforms().add(new Scale(ComponenteLutador.ESCALA, ComponenteLutador.ESCALA, 0, 0));
        orientacao = lado.orientacao();
        Color principal = lado == LadoLutador.JOGADOR
                ? Color.web("#27D8FF") : Color.web("#FF4E68");
        Color secundario = lado == LadoLutador.JOGADOR
                ? Color.web("#1368CE") : Color.web("#B3123F");

        configurarMembro(bracoTras, secundario);
        configurarMembro(pernaTras, secundario);
        configurarMembro(bracoFrente, principal);
        configurarMembro(pernaFrente, principal);
        maoTras.setFill(secundario);
        maoFrente.setFill(Color.web("#FFE5C7"));

        Polygon tronco = new Polygon(17, 34, 45, 34, 51, 70, 13, 70);
        tronco.setFill(principal);
        tronco.setStroke(Color.web("#07111F"));
        tronco.setStrokeWidth(3);

        Rectangle cinto = new Rectangle(14, 65, 36, 8);
        cinto.setArcWidth(5);
        cinto.setArcHeight(5);
        cinto.setFill(Color.web("#FFD43B"));
        cinto.setStroke(Color.web("#07111F"));
        cinto.setStrokeWidth(2);

        Circle cabeca = new Circle(31, 20, 14, Color.web("#FFE5C7"));
        cabeca.setStroke(Color.web("#07111F"));
        cabeca.setStrokeWidth(3);

        Polygon cabelo = new Polygon(18, 18, 21, 8, 30, 4, 43, 9, 45, 17, 36, 13, 28, 15);
        cabelo.setFill(Color.web("#172337"));

        Rectangle olho = new Rectangle(38, 19, 5, 2);
        olho.setFill(Color.web("#07111F"));
        corpo.getChildren().addAll(pernaTras, bracoTras, tronco, cinto, cabeca, cabelo,
                olho, pernaFrente, bracoFrente, maoTras, maoFrente);
        corpo.setEffect(new DropShadow(8, Color.color(0, 0, 0, 0.65)));

        sprite.setX(-32);
        sprite.setY(0);
        sprite.setFitWidth(128);
        sprite.setFitHeight(112);
        getChildren().addAll(corpo, sprite);
        mostrar(AcaoCombate.IDLE, 0);
    }

    /** Espelha imediatamente a silhueta para ficar virada para o adversário. */
    public void olharPara(int novaOrientacao) {
        orientacao = novaOrientacao < 0 ? -1 : 1;
        corpo.setScaleX(orientacao);
        corpo.setTranslateX(orientacao < 0 ? 64 : 0);
        sprite.setScaleX(orientacao);
    }

    /** Ajusta a silhueta ao estado e ao frame lógico atuais. */
    public void inclinarSalto(double graus) {
        sprite.setRotate(graus);
    }

    public void mostrar(AcaoCombate acao, int frame) {
        mostrar(acao, frame, -1);
    }

    public void mostrar(AcaoCombate acao, int frame, int frameAtaque) {
        boolean usa40 = frameAtaque >= 0 && ATAQUES_40.containsKey(acao);
        List<Image> imagens = usa40 ? ATAQUES_40.get(acao) : SPRITES.get(acao);
        int indice = usa40 ? frameAtaque : frame;
        boolean temSprite = imagens != null && !imagens.isEmpty();
        // Os novos frames baixos já estão alinhados na tela comum.
        sprite.setX(-32);
        sprite.setY(0);
        sprite.setFitWidth(128);
        sprite.setFitHeight(112);
        sprite.setVisible(temSprite);
        corpo.setVisible(!temSprite);
        sprite.setScaleX(orientacao);
        if (temSprite) {
            sprite.setImage(imagens.get(Math.min(Math.max(indice, 0), imagens.size() - 1)));
            return;
        }
        reporPose();

        switch (acao) {
            case WALK_FORWARD, WALK_BACKWARD -> poseAndar(frame);
            case JUMP, JUMP_SIDE -> poseSalto();
            case BLOCK, GUARD -> poseBloqueio(false);
            case LOW_BLOCK, CROUCH -> poseBloqueio(true);
            case LP, MP, HP -> poseSoco(acao == AcaoCombate.HP);
            case LK, MK, HK -> posePontape(acao == AcaoCombate.HK);
            case HIT -> corpo.setRotate(-10 * orientacao);
            case KNOCKOUT -> {
                corpo.setRotate(72 * orientacao);
                corpo.setTranslateY(28);
            }
            default -> { }
        }
    }

    private static Map<AcaoCombate, List<Image>> carregarSprites() {
        Map<AcaoCombate, List<Image>> sprites = new EnumMap<>(AcaoCombate.class);
        for (AcaoCombate acao : AcaoCombate.values()) {
            List<Image> imagens = new ArrayList<>();
            for (int i = 1; ; i++) {
                String caminho = acao.caminhoFrame(i);
                try (InputStream ficheiro = VistaLutador.class.getResourceAsStream(caminho)) {
                    if (ficheiro == null) break;
                    imagens.add(new Image(ficheiro));
                } catch (java.io.IOException e) {
                    break;
                }
            }
            sprites.put(acao, imagens);
        }
        return sprites;
    }

    private static Map<AcaoCombate, List<Image>> carregarAtaques40() {
        Map<AcaoCombate, List<Image>> sprites = new EnumMap<>(AcaoCombate.class);
        for (AcaoCombate acao : AcaoCombate.values()) {
            if (!acao.eAtaque()) continue;
            List<Image> imagens = new ArrayList<>();
            for (int i = 1; i <= 40; i++) {
                try (InputStream ficheiro = VistaLutador.class.getResourceAsStream(acao.caminhoAtaque40(i))) {
                    if (ficheiro == null) break;
                    Image imagem = new Image(ficheiro);
                    if (imagem.isError()) break;
                    imagens.add(imagem);
                } catch (java.io.IOException e) {
                    break;
                }
            }
            // Nunca carregar uma sequência incompleta durante a preparação dos recursos.
            if (imagens.size() == 40) sprites.put(acao, List.copyOf(imagens));
        }
        return sprites;
    }

    private void reporPose() {
        corpo.setScaleX(orientacao);
        corpo.setRotate(0);
        corpo.setTranslateY(0);
        corpo.setTranslateX(orientacao < 0 ? 64 : 0);
        definir(bracoTras, maoTras, 20, 42, 8, 64);
        definir(bracoFrente, maoFrente, 43, 42, 57, 62);
        definir(pernaTras, null, 24, 69, 17, 103);
        definir(pernaFrente, null, 39, 69, 49, 103);
    }

    private void poseAndar(int frame) {
        int passo = frame % 2 == 0 ? 1 : -1;
        definir(pernaTras, null, 24, 69, 18 + 10 * passo, 103);
        definir(pernaFrente, null, 39, 69, 48 - 10 * passo, 103);
        definir(bracoTras, maoTras, 20, 42, 9 - 5 * passo, 65);
        definir(bracoFrente, maoFrente, 43, 42, 56 + 5 * passo, 64);
        corpo.setTranslateY(frame % 2 == 0 ? 0 : -2);
    }

    private void poseSalto() {
        definir(bracoTras, maoTras, 20, 42, 9, 28);
        definir(bracoFrente, maoFrente, 43, 42, 56, 27);
        definir(pernaTras, null, 24, 69, 29, 89);
        definir(pernaFrente, null, 39, 69, 49, 87);
        corpo.setRotate(-5 * orientacao);
    }

    private void poseBloqueio(boolean baixo) {
        definir(bracoFrente, maoFrente, 43, 42, 58, 30);
        definir(bracoTras, maoTras, 20, 42, 51, 57);
        if (baixo) {
            corpo.setTranslateY(14);
            definir(pernaTras, null, 24, 69, 10, 92);
            definir(pernaFrente, null, 39, 69, 56, 92);
        }
    }

    private void poseSoco(boolean forte) {
        definir(bracoFrente, maoFrente, 43, 42, forte ? 82 : 75, forte ? 37 : 43);
        definir(bracoTras, maoTras, 20, 42, 39, 32);
        corpo.setRotate(forte ? 5 * orientacao : 0);
    }

    private void posePontape(boolean forte) {
        definir(pernaFrente, null, 39, 69, forte ? 84 : 75, forte ? 45 : 62);
        definir(bracoFrente, maoFrente, 43, 42, 54, 28);
        definir(bracoTras, maoTras, 20, 42, 8, 50);
        corpo.setRotate(-5 * orientacao);
    }

    private static Line membro() {
        Line linha = new Line();
        linha.setStrokeLineCap(StrokeLineCap.ROUND);
        linha.setStrokeWidth(10);
        return linha;
    }

    private static void configurarMembro(Line membro, Color cor) {
        membro.setStroke(cor);
    }

    private static void definir(Line membro, Circle extremidade, double inicioX, double inicioY,
                                double fimX, double fimY) {
        membro.setStartX(inicioX);
        membro.setStartY(inicioY);
        membro.setEndX(fimX);
        membro.setEndY(fimY);
        if (extremidade != null) {
            extremidade.setCenterX(fimX);
            extremidade.setCenterY(fimY);
        }
    }
}
