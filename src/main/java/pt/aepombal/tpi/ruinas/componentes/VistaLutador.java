package pt.aepombal.tpi.ruinas.componentes;

import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import pt.aepombal.tpi.ruinas.modelo.AcaoCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;

/**
 * Representação vetorial temporária de um lutador.
 * As poses tornam cada ação legível enquanto os sprites fotográficos não estão recortados.
 */
public final class VistaLutador extends Group {

    private final Group corpo = new Group();
    private final Line bracoTras = membro();
    private final Line bracoFrente = membro();
    private final Line pernaTras = membro();
    private final Line pernaFrente = membro();
    private final Circle maoTras = new Circle(5);
    private final Circle maoFrente = new Circle(5);
    private int orientacao;

    public VistaLutador(LadoLutador lado) {
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

        getChildren().add(corpo);
        mostrar(AcaoCombate.IDLE, 0);
    }

    /** Espelha imediatamente a silhueta para ficar virada para o adversário. */
    public void olharPara(int novaOrientacao) {
        orientacao = novaOrientacao < 0 ? -1 : 1;
        corpo.setScaleX(orientacao);
        corpo.setTranslateX(orientacao < 0 ? ComponenteLutador.LARGURA : 0);
    }

    /** Ajusta a silhueta ao estado e ao frame lógico atuais. */
    public void mostrar(AcaoCombate acao, int frame) {
        reporPose();

        switch (acao) {
            case WALK_FORWARD, WALK_BACKWARD -> poseAndar(frame);
            case JUMP -> poseSalto();
            case BLOCK, GUARD -> poseBloqueio(false);
            case LOW_BLOCK -> poseBloqueio(true);
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

    private void reporPose() {
        corpo.setScaleX(orientacao);
        corpo.setRotate(0);
        corpo.setTranslateY(0);
        corpo.setTranslateX(orientacao < 0 ? ComponenteLutador.LARGURA : 0);
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
