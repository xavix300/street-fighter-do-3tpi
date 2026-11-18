package pt.aepombal.tpi.ruinas;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import pt.aepombal.tpi.ruinas.modelo.Personagem;

/** Testa o painel real; --mostrar mantém uma janela de pré-visualização aberta. */
public final class SeletorPersonagensTeste {
    public static void main(String[] args) {
        boolean mostrar = List.of(args).contains("--mostrar");
        Platform.startup(() -> {
            try {
                StackPane root = new StackPane();
                var fundo = SeletorPersonagensTeste.class.getResource("/assets/textures/menu/principal.png");
                ImageView imagem = new ImageView(new Image(fundo.toExternalForm()));
                imagem.setFitWidth(960);
                imagem.setFitHeight(560);
                root.getChildren().add(imagem);
                Stage stage = new Stage();
                stage.setTitle("TPI Fighter — Seleção de personagens");
                stage.setScene(new Scene(root, 960, 560));
                stage.setResizable(false);
                PainelMenu.personagens(root);
                stage.show();
                root.applyCss();
                root.layout();
                SeletorPersonagens seletor = (SeletorPersonagens) root.lookup("#seletor-personagens");
                verificar(seletor != null && seletor.animacaoAtiva(), "Animação não arrancou");
                Button primeiro = (Button) root.lookup("#personagem-1");
                Button segundo = (Button) root.lookup("#personagem-2");
                var idle1 = primeiro.lookupAll(".image-view").stream().map(ImageView.class::cast).findFirst().orElseThrow();
                var idle2 = segundo.lookupAll(".image-view").stream().map(ImageView.class::cast).findFirst().orElseThrow();
                Image antes1 = idle1.getImage(), antes2 = idle2.getImage();
                seletor.avancarPose();
                verificar(idle1.getImage() != antes1 && idle2.getImage() != antes2, "Os dois idles devem avançar");
                for (int i = 0; i < 11; i++) seletor.avancarPose();
                verificar(idle1.getImage() == antes1 && idle2.getImage() == antes2, "Ciclo de doze poses");
                primeiro.requestFocus();
                NavegacaoMenu.tecla((PainelMenu) seletor.getParent().getParent().getParent(), KeyCode.RIGHT);
                verificar(stage.getScene().getFocusOwner() == segundo, "Navegação para o segundo lutador");
                segundo.fire();
                verificar(ConfiguracaoPartida.ATUAL.personagem(0) == Personagem.LUTADOR_02,
                        "A escolha deve ficar guardada para P1");
                verificar(ConfiguracaoPartida.ATUAL.personagem(1) == Personagem.LUTADOR_01,
                        "Escolher P1 não deve alterar P2/CPU");
                verificar(root.lookupAll(".label").stream().map(Label.class::cast)
                        .anyMatch(l -> l.getText().contains("MP e stance")), "Aviso de imagens provisórias");
                ((Button) root.lookup("#escolher-jogador-2")).fire();
                segundo.fire();
                verificar(ConfiguracaoPartida.ATUAL.personagem(1) == Personagem.LUTADOR_02,
                        "Deve permitir o mesmo personagem nos dois lados");
                primeiro.fire();
                verificar(ConfiguracaoPartida.ATUAL.personagem(0) == Personagem.LUTADOR_02
                        && ConfiguracaoPartida.ATUAL.personagem(1) == Personagem.LUTADOR_01,
                        "A escolha de P2/CPU deve ser independente");
                for (var node : root.lookupAll(".label")) {
                    Label label = (Label) node;
                    verificar(label.getHeight() + 1 >= label.prefHeight(label.getWidth()),
                            "Texto cortado: " + label.getText());
                }
                for (var node : root.lookupAll(".button")) {
                    var bounds = node.localToScene(node.getLayoutBounds());
                    verificar(bounds.getMinY() >= 0 && bounds.getMaxY() <= 560,
                            "Botão fora da janela: " + ((Button) node).getText());
                }
                PainelMenu.fecharAtual(root);
                verificar(!seletor.animacaoAtiva(), "A animação deve parar ao fechar");
                verificar(!PainelMenu.aberto(root), "O painel deve fechar");
                PainelMenu.personagens(root);
                root.applyCss();
                root.layout();
                var snapshot = root.snapshot(null, null);
                BufferedImage png = new BufferedImage(960, 560, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < 560; y++) {
                    for (int x = 0; x < 960; x++) png.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
                }
                Files.createDirectories(Path.of("target"));
                ImageIO.write(png, "png", Path.of("target", "seletor-personagens.png").toFile());
                System.out.println("Personagens: idles, seleção real independente, navegação, layout e fecho: OK");
                if (!mostrar) System.exit(0);
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
