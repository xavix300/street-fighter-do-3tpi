package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/** Pausa com a mesma linguagem gráfica do menu principal. */
final class MenuPausa extends FXGLMenu {
    MenuPausa() {
        super(MenuType.GAME_MENU);
        Rectangle fundo = new Rectangle(960, 560, Color.web(EstiloTpi.FUNDO, .94));
        Text titulo = new Text(305, 160, "TPI FIGHTER");
        titulo.setFill(Color.WHITE);
        titulo.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.ITALIC, 48));
        Text pausa = new Text(439, 199, "PAUSA");
        pausa.setFill(Color.web(EstiloTpi.GELO));
        pausa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        VBox botoes = new VBox(14);
        botoes.setTranslateX(320);
        botoes.setTranslateY(220);
        botoes.getChildren().addAll(botao("CONTINUAR", this::fireResume),
                botao("DEFINIÇÕES", () -> PainelMenu.definicoes(getContentRoot())),
                botao("MENU PRINCIPAL", () -> PainelMenu.confirmar(getContentRoot(), "VOLTAR AO MENU",
                        "O combate atual será terminado. Queres continuar?", this::fireExitToMainMenu)),
                botao("SAIR", () -> PainelMenu.confirmar(getContentRoot(), "SAIR DO JOGO",
                        "Queres sair do TPI Fighter?", this::fireExit)));
        getContentRoot().getChildren().addAll(fundo, titulo, pausa, botoes, EstiloTpi.cantos());
    }

    private Button botao(String nome, Runnable acao) {
        Button botao = PainelMenu.botao(nome, acao);
        botao.setPrefSize(320, 48);
        return botao;
    }
}
