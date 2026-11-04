package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/** Mantém a identidade do campus na passagem do menu para a oficina. */
final class CarregamentoTpi extends LoadingScene {
    CarregamentoTpi() {
        Rectangle fundo = new Rectangle(960, 560, Color.web(EstiloTpi.FUNDO));
        Text titulo = new Text(0, 238, "TPI FIGHTER");
        titulo.setWrappingWidth(960);
        titulo.setTextAlignment(TextAlignment.CENTER);
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.ITALIC, 58));
        Rectangle linha = new Rectangle(370, 263, 220, 4);
        linha.setFill(Color.web(EstiloTpi.LARANJA));
        Text arena = new Text(0, 309, "CAMPUS  /  OFICINA");
        arena.setWrappingWidth(960);
        arena.setTextAlignment(TextAlignment.CENTER);
        arena.setFill(Color.web(EstiloTpi.GELO));
        arena.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Text estado = new Text(0, 350, "A PREPARAR O COMBATE...");
        estado.setWrappingWidth(960);
        estado.setTextAlignment(TextAlignment.CENTER);
        estado.setFill(Color.web("#92ACC5"));
        estado.setFont(Font.font("Segoe UI", 11));
        getContentRoot().getChildren().setAll(fundo, titulo, linha, arena, estado, EstiloTpi.cantos());
    }
}
