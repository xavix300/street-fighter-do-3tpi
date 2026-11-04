package pt.aepombal.tpi.ruinas;

import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;

/** Brilho suave sobre os traços do logótipo, nas coordenadas da arte original. */
final class BrilhoTitulo extends Group {
    private double tempo;

    BrilhoTitulo() {
        setMouseTransparent(true);
        getChildren().addAll(
                traco(1047, 100, 1218, 76, 1122, 140, 1050, 177, "#169CFF", 5),
                traco(671, 216, 514, 292, 489, 302, 654, 266, "#E1F4FF", 4),
                traco(750, 339, 875, 304, 1026, 276, 1133, 278, EstiloTpi.LARANJA, 6));
        atualizar(0);
    }

    void atualizar(double tpf) {
        tempo = (tempo + tpf) % 3.2;
        // Ciclo lento e contínuo, sem deslocar os traços nem criar flashes.
        double pulso = (1 - Math.cos(tempo * Math.PI * 2 / 3.2)) / 2;
        setOpacity(.12 + .63 * pulso);
    }

    private Path traco(double x, double y, double c1x, double c1y,
                       double c2x, double c2y, double fimX, double fimY,
                       String cor, double espessura) {
        Path linha = new Path(new MoveTo(x, y),
                new CubicCurveTo(c1x, c1y, c2x, c2y, fimX, fimY));
        linha.setFill(null);
        linha.setStroke(Color.web(cor));
        linha.setStrokeWidth(espessura);
        linha.setStrokeLineCap(StrokeLineCap.ROUND);
        linha.setEffect(new DropShadow(22, Color.web(cor)));
        return linha;
    }
}
