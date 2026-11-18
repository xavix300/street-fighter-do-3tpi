package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/** Usa a arte do menu e sobrepõe áreas interativas aos cinco botões. */
final class MenuPrincipal extends FXGLMenu {
    private final Polygon[] botoes = new Polygon[5];
    private final BrilhoTitulo brilhoTitulo = new BrilhoTitulo();
    private int selecionado;

    MenuPrincipal() {
        super(MenuType.MAIN_MENU);
        var recurso = MenuPrincipal.class.getResource("/assets/textures/menu/principal.png");
        if (recurso == null) throw new IllegalStateException("Imagem do menu em falta.");
        ImageView fundo = new ImageView(new Image(recurso.toExternalForm()));
        fundo.setFitWidth(1672);
        fundo.setFitHeight(941);
        fundo.setSmooth(true);
        Group conteudo = new Group(fundo, brilhoTitulo);
        // Coordenadas da arte original: o desenho e os alvos escalam em conjunto.
        double[][] limites = {
                {87, 410, 502, 410, 469, 479, 55, 479},
                {99, 490, 476, 490, 447, 556, 68, 556},
                {99, 563, 461, 563, 432, 629, 68, 629},
                {99, 636, 448, 636, 418, 701, 68, 701},
                {99, 708, 434, 708, 405, 773, 68, 773}
        };
        String[] nomes = {"Jogar", "Personagens", "Arenas", "Opções", "Sair"};
        for (int i = 0; i < botoes.length; i++) {
            final int indice = i;
            Polygon botao = new Polygon(limites[i]);
            botao.setFill(Color.TRANSPARENT);
            botao.setStroke(Color.TRANSPARENT);
            botao.setStrokeWidth(3);
            botao.setCursor(Cursor.HAND);
            botao.setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
            botao.setAccessibleText(nomes[i]);
            botao.setOnMouseEntered(e -> selecionar(indice));
            botao.setOnMouseClicked(e -> ativar(indice));
            botoes[i] = botao;
            conteudo.getChildren().add(botao);
        }
        conteudo.setScaleX(getAppWidth() / 1672.0);
        conteudo.setScaleY(getAppHeight() / 941.0);
        // Scale usa o centro do Group; compensar para manter a imagem no canto.
        conteudo.setTranslateX((getAppWidth() - 1672) / 2.0);
        conteudo.setTranslateY((getAppHeight() - 941) / 2.0);
        getContentRoot().getChildren().add(conteudo);
        getContentRoot().getChildren().add(EstiloTpi.cantos());
        selecionar(0);
        getInput().addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            if (PainelMenu.aberto(getContentRoot())) {
                if (evento.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    PainelMenu.fecharAtual(getContentRoot());
                    evento.consume();
                }
                return;
            }
            switch (evento.getCode()) {
                case UP -> selecionar((selecionado + 4) % 5);
                case DOWN, TAB -> selecionar((selecionado + 1) % 5);
                case ENTER, SPACE -> ativar(selecionado);
                default -> { return; }
            }
            evento.consume();
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        super.onUpdate(tpf);
        brilhoTitulo.atualizar(tpf);
    }

    void comando(int botoes) {
        if (PainelMenu.comandoAtual(getContentRoot(), botoes)) return;
        if ((botoes & 1) != 0) selecionar((selecionado + 4) % 5);
        else if ((botoes & 2) != 0) selecionar((selecionado + 1) % 5);
        if ((botoes & 0x1000) != 0) ativar(selecionado);
    }

    private void selecionar(int indice) {
        selecionado = indice;
        for (int i = 0; i < botoes.length; i++) {
            botoes[i].setFill(i == indice ? Color.web(EstiloTpi.LARANJA, .18) : Color.TRANSPARENT);
            botoes[i].setStroke(i == indice ? Color.web(EstiloTpi.LARANJA) : Color.TRANSPARENT);
            botoes[i].setEffect(i == indice
                    ? new javafx.scene.effect.DropShadow(16, Color.web(EstiloTpi.LARANJA, .7)) : null);
        }
    }

    private void ativar(int indice) {
        switch (indice) {
            case 0 -> PainelMenu.escolherPartida(getContentRoot(), this::fireNewGame);
            case 1 -> PainelMenu.personagens(getContentRoot());
            case 2 -> PainelMenu.mensagem(getContentRoot(), "ARENAS / OFICINA",
                    "O combate decorre na oficina do campus.\n\nA seleção de outras arenas ainda não está disponível.");
            case 3 -> PainelMenu.definicoes(getContentRoot());
            case 4 -> PainelMenu.confirmar(getContentRoot(), "SAIR DO JOGO",
                    "Queres sair do TPI Fighter?", this::fireExit);
            default -> throw new IllegalArgumentException("Botão desconhecido: " + indice);
        }
    }
}
