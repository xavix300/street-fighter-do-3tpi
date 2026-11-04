package pt.aepombal.tpi.ruinas;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/** Elementos gráficos partilhados pelo menu, carregamento e combate. */
final class EstiloTpi {
    static final String AZUL = "#0784FF";
    static final String LARANJA = "#FF8A24";
    static final String GELO = "#D9F4FF";
    static final String FUNDO = "#071B30";

    private EstiloTpi() { }

    static Group cantos() {
        Polygon esquerdo = new Polygon(0, 0, 26, 0, 0, 65);
        esquerdo.setFill(Color.web("#BDCEDD", .7));
        Polygon faixa = new Polygon(32, 0, 37, 0, 0, 86, 0, 74);
        faixa.setFill(Color.web(AZUL));
        Polygon direito = new Polygon(960, 560, 914, 560, 960, 480);
        direito.setFill(Color.web("#BDCEDD", .7));
        Polygon faixaDireita = new Polygon(901, 560, 908, 560, 960, 469, 960, 457);
        faixaDireita.setFill(Color.web(LARANJA));
        Group grupo = new Group(esquerdo, faixa, direito, faixaDireita);
        grupo.setMouseTransparent(true);
        return grupo;
    }
}
