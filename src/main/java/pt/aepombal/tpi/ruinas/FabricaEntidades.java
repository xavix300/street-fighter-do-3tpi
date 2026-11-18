package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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
        var recurso = FabricaEntidades.class.getResource("/assets/textures/arena/oficina.jpg");
        if (recurso == null) throw new IllegalStateException("Fundo da arena n?o encontrado.");
        javafx.scene.image.Image imagem = new javafx.scene.image.Image(recurso.toExternalForm());
        javafx.scene.image.ImageView fundo = new javafx.scene.image.ImageView(imagem);
        // Preenche a arena com um recorte central, sem deformar a fotografia.
        double escala = Math.max(960.0 / imagem.getWidth(), 560.0 / imagem.getHeight());
        double larguraVisivel = 960.0 / escala;
        double alturaVisivel = 560.0 / escala;
        fundo.setViewport(new javafx.geometry.Rectangle2D(
                (imagem.getWidth() - larguraVisivel) / 2,
                (imagem.getHeight() - alturaVisivel) / 2, larguraVisivel, alturaVisivel));
        fundo.setFitWidth(960);
        fundo.setFitHeight(560);
        fundo.setSmooth(true);
        Group arena = new Group(fundo);

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
        VistaLutador vista = new VistaLutador(lado,
                ConfiguracaoPartida.ATUAL.personagem(lado == LadoLutador.JOGADOR ? 0 : 1));

        ComponenteLutador lutador = new ComponenteLutador(lado, gestor, gestor.animacoes(), vista);
        var construtor = FXGL.entityBuilder(data)
                .type(lado == LadoLutador.JOGADOR ? TipoEntidade.JOGADOR : TipoEntidade.OPONENTE)
                .viewWithBBox(limiteColisao)
                .view(vista)
                .collidable()
                .with(lutador);

        if (lado == LadoLutador.OPONENTE && !gestor.doisJogadores()) {
            construtor.with(gestor.dificuldade().criarComportamento(gestor));
        }

        Entity entidade = construtor.build();
        gestor.registar(lado, lutador);
        return entidade;
    }
}
