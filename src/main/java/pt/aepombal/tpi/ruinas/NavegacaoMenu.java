package pt.aepombal.tpi.ruinas;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

/** Navegação explícita, sem depender de eventos de teclado simulados. */
final class NavegacaoMenu {
    private NavegacaoMenu() { }

    static void comando(Pane raiz, int botoes, Runnable voltar) {
        if ((botoes & (0x2000 | 0x20 | 0x10)) != 0) {
            voltar.run(); // B / Círculo, Back / Create ou Start / Options.
            return;
        }
        if ((botoes & 1) != 0) tecla(raiz, KeyCode.UP);
        else if ((botoes & 2) != 0) tecla(raiz, KeyCode.DOWN);
        else if ((botoes & 4) != 0) tecla(raiz, KeyCode.LEFT);
        else if ((botoes & 8) != 0) tecla(raiz, KeyCode.RIGHT);
        if ((botoes & 0x1000) != 0) tecla(raiz, KeyCode.ENTER); // A / Cruz.
    }

    static boolean tecla(Pane raiz, KeyCode tecla) {
        List<Node> controlos = new ArrayList<>();
        recolher(raiz, controlos);
        if (controlos.isEmpty()) return false;
        Node foco = raiz.getScene() == null ? null : raiz.getScene().getFocusOwner();
        int atual = controlos.indexOf(foco);
        switch (tecla) {
            case UP, DOWN, TAB -> {
                int passo = tecla == KeyCode.UP ? -1 : 1;
                int indice = atual < 0 ? 0 : Math.floorMod(atual + passo, controlos.size());
                controlos.get(indice).requestFocus();
            }
            case LEFT, RIGHT -> {
                if (foco instanceof Slider slider) {
                    slider.setValue(slider.getValue() + (tecla == KeyCode.LEFT ? -1 : 1)
                            * slider.getBlockIncrement());
                } else {
                    int passo = tecla == KeyCode.LEFT ? -1 : 1;
                    controlos.get(atual < 0 ? 0 : Math.floorMod(atual + passo, controlos.size())).requestFocus();
                }
            }
            case ENTER, SPACE -> {
                if (atual < 0) {
                    // Primeiro realça; nunca confirma uma ação destrutiva sem seleção.
                    controlos.getFirst().requestFocus();
                } else if (foco instanceof ButtonBase botao) botao.fire();
            }
            default -> { return false; }
        }
        return true;
    }

    private static void recolher(Node node, List<Node> resultado) {
        if (!node.isVisible() || node.isDisabled()) return;
        if (node instanceof ButtonBase || node instanceof Slider) {
            resultado.add(node);
            return;
        }
        if (node instanceof Parent parent) {
            for (Node filho : parent.getChildrenUnmodifiable()) recolher(filho, resultado);
        }
    }
}
