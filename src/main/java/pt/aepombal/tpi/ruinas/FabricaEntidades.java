package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.Group;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.componentes.VistaLutador;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.LadoLutador;

// [IA] Fábrica concentrada para criar a arena e os dois lutadores do jogo de luta.
/** Cria as entidades visuais da arena. As texturas entram depois da lógica de combate. */
public final class FabricaEntidades implements EntityFactory {

    private final GestorCombate gestor;

    public FabricaEntidades(GestorCombate gestor) {
        this.gestor = gestor;
    }

    @Spawns("arena")
    public Entity criarArena(SpawnData data) {
        Rectangle fundo = new Rectangle(960, 560);
        fundo.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#081126")),
                new Stop(0.58, Color.web("#24305D")),
                new Stop(1, Color.web("#6B244B"))));

        Circle sol = new Circle(770, 235, 118, Color.web("#FF7A45", 0.42));
        Circle brilho = new Circle(770, 235, 82, Color.web("#FFD166", 0.18));

        Group cidade = new Group();
        double[] alturas = {135, 180, 105, 225, 155, 195, 120, 240, 165, 205, 125, 188};
        for (int i = 0; i < alturas.length; i++) {
            double largura = 82;
            double x = i * 80.0;
            double y = 502 - alturas[i];
            Rectangle predio = new Rectangle(x, y, largura, alturas[i]);
            predio.setFill(Color.web("#0A1021", 0.92));
            cidade.getChildren().add(predio);
            for (double janelaY = y + 22; janelaY < 480; janelaY += 28) {
                Rectangle janela = new Rectangle(x + 15, janelaY, 11, 5);
                janela.setFill(Color.web(i % 3 == 0 ? "#2AE8FF" : "#FFCF4A", 0.45));
                cidade.getChildren().add(janela);
            }
        }

        Polygon plataforma = new Polygon(0, 475, 960, 475, 960, 560, 0, 560);
        plataforma.setFill(Color.web("#11182C"));
        plataforma.setStroke(Color.web("#FFB82E"));
        plataforma.setStrokeWidth(3);

        Group grelha = new Group();
        for (int x = -80; x <= 1040; x += 80) {
            Line linha = new Line(480, 475, x, 560);
            linha.setStroke(Color.web("#28D7F2", 0.2));
            grelha.getChildren().add(linha);
        }
        for (int y = 490; y <= 560; y += 16) {
            Line linha = new Line(0, y, 960, y);
            linha.setStroke(Color.web("#28D7F2", 0.18));
            grelha.getChildren().add(linha);
        }

        Rectangle chao = new Rectangle(960, 58, Color.web("#141C32", 0.92));
        chao.setTranslateY(502);
        Line linhaChao = new Line(0, 502, 960, 502);
        linhaChao.setStroke(Color.web("#FFD23F"));
        linhaChao.setStrokeWidth(4);

        Group arena = new Group(fundo, sol, brilho, cidade, plataforma, grelha, chao, linhaChao);

        return FXGL.entityBuilder(data)
                .type(TipoEntidade.ARENA)
                .view(arena)
                .build();
    }

    @Spawns("lutador")
    public Entity criarLutador(SpawnData data) {
        LadoLutador lado = data.get("lado");
        Rectangle limiteColisao = new Rectangle(ComponenteLutador.LARGURA,
                ComponenteLutador.ALTURA, Color.TRANSPARENT);
        VistaLutador vista = new VistaLutador(lado);

        ComponenteLutador lutador = new ComponenteLutador(lado, gestor, gestor.animacoes(), vista);
        var construtor = FXGL.entityBuilder(data)
                .type(lado == LadoLutador.JOGADOR ? TipoEntidade.JOGADOR : TipoEntidade.OPONENTE)
                .viewWithBBox(limiteColisao)
                .view(vista)
                .collidable()
                .with(lutador);

        if (lado == LadoLutador.OPONENTE) {
            construtor.with(gestor.dificuldade().criarComportamento(gestor));
        }

        Entity entidade = construtor.build();
        gestor.registar(lado, lutador);
        return entidade;
    }
}
