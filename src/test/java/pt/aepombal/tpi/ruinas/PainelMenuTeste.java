package pt.aepombal.tpi.ruinas;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/** Executa CSS/layout reais para detetar texto cortado no painel Jogar. */
public final class PainelMenuTeste {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                var root = new StackPane();
                new Scene(root, 960, 560);
                root.resize(960, 560);
                PainelMenu.escolherPartida(root, () -> { });
                for (int mode = 0; mode < 2; mode++) {
                    root.applyCss();
                    root.layout();
                    for (var node : root.lookupAll(".button")) {
                        var button = (Button) node;
                        var text = new Text(button.getText());
                        text.setFont(button.getFont());
                        double contentHeight = button.getHeight() - button.getInsets().getTop()
                                - button.getInsets().getBottom();
                        verificar(contentHeight + 1 >= text.getLayoutBounds().getHeight(),
                                "Texto cortado: " + button.getText() + ", espaço=" + contentHeight
                                        + ", texto=" + text.getLayoutBounds().getHeight());
                        if (button.getGraphic() instanceof Label icon) {
                            verificar(contentHeight + 1 >= icon.prefHeight(-1), "Ícone cortado");
                        }
                    }
                    for (var node : root.lookupAll(".label")) {
                        var label = (Label) node;
                        verificar(label.getHeight() + 1 >= label.prefHeight(label.getWidth()),
                                "Label cortada: " + label.getText());
                    }
                    root.lookupAll(".button").stream().map(Button.class::cast)
                            .filter(b -> b.getText().startsWith("MODO:")).findFirst().orElseThrow().fire();
                }
                System.out.println("Painel Jogar: texto e ícones sem corte, 1P e 2P: OK");
                System.exit(0);
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void verificar(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
