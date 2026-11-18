package pt.aepombal.tpi.ruinas;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.SpawnData;
import javafx.scene.image.ImageView;
import pt.aepombal.tpi.ruinas.componentes.ComponenteLutador;
import pt.aepombal.tpi.ruinas.componentes.VistaLutador;
import pt.aepombal.tpi.ruinas.logica.GestorCombate;
import pt.aepombal.tpi.ruinas.modelo.*;

import java.nio.file.Path;
import java.util.Map;

/** Motor real: escolha na fábrica, combate com placeholders e troca sem reiniciar. */
public final class PersonagensCombateTeste extends GameApplication {
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(960);
        settings.setHeight(560);
        settings.setTitle("Verificação de personagens");
        settings.setMainMenuEnabled(false);
        settings.setCloseConfirmation(false);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        new StreetFrameApp().initGameVars(vars);
    }

    @Override
    protected void initGame() {
        try {
            ConfiguracaoPartida.ATUAL.selecionarPersonagem(0, Personagem.LUTADOR_02);
            ConfiguracaoPartida.ATUAL.selecionarPersonagem(1, Personagem.LUTADOR_01);
            var dados = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
            var gestor = new GestorCombate(dados, Path.of("target", "unused-character-records.csv"),
                    new ProgressoCombate(), true);
            var factory = new FabricaEntidades(gestor);
            var p1 = factory.criarLutador(new SpawnData(300, ComponenteLutador.CHAO_Y)
                    .put("lado", LadoLutador.JOGADOR)).getComponent(ComponenteLutador.class);
            var p2 = factory.criarLutador(new SpawnData(380, ComponenteLutador.CHAO_Y)
                    .put("lado", LadoLutador.OPONENTE)).getComponent(ComponenteLutador.class);
            check(p1.personagem() == Personagem.LUTADOR_02 && p2.personagem() == Personagem.LUTADOR_01,
                    "A fábrica deve usar as escolhas dos dois lados");
            p1.atacar(AcaoCombate.LP);
            p2.atacar(AcaoCombate.LP);
            for (int i = 0; i < 4; i++) gestor.atualizar(1.0 / 60);
            check(p1.vida() == 9640 && p2.vida() == 9640, "Personagem provisória também combate");
            int vida = p1.vida();
            var acao = p1.acao();
            var frame = p1.frameAtual();
            double x = p1.getEntity().getX(), y = p1.getEntity().getY();
            gestor.trocarPersonagem(LadoLutador.JOGADOR, Personagem.LUTADOR_01);
            gestor.trocarPersonagem(LadoLutador.OPONENTE, Personagem.LUTADOR_02);
            check(p1.personagem() == Personagem.LUTADOR_01 && p2.personagem() == Personagem.LUTADOR_02,
                    "Troca nos dois lados");
            check(p1.vida() == vida && p1.acao() == acao && p1.frameAtual() == frame
                    && p1.getEntity().getX() == x && p1.getEntity().getY() == y,
                    "Trocar não pode reiniciar a vida, estado, frame ou posição");

            var vista = new VistaLutador(LadoLutador.JOGADOR, Personagem.LUTADOR_02);
            var image = (ImageView) vista.getChildren().getLast();
            vista.mostrar(AcaoCombate.IDLE, 0);
            var idle = image.getImage();
            var original = new VistaLutador(LadoLutador.JOGADOR, Personagem.LUTADOR_01);
            var originalImage = (ImageView) original.getChildren().getLast();
            for (var action : AcaoCombate.values()) {
                original.mostrar(action, 0, 0);
                vista.mostrar(action, 0, 0);
                check(image.getImage() != originalImage.getImage(), "Não pode usar imagens do outro lutador: " + action);
                if (action != AcaoCombate.IDLE && action != AcaoCombate.STANCE) {
                    check(image.getImage() != idle, "Deve usar o movimento próprio disponível: " + action);
                }
                if (action.eAtaque()) {
                    vista.mostrarAtaque(dados.get(action), 0, 0);
                    check(image.getImage() != originalImage.getImage(), "Ataque não pode mudar a identidade");
                }
            }
            vista.mostrar(AcaoCombate.GUARD, 3);
            var placeholder = image.getImage();
            vista.mostrarAtaque(dados.get(AcaoCombate.MP), 0, 0);
            check(image.getImage() == placeholder, "MP em falta deve usar a guarda própria");
            vista.mostrar(AcaoCombate.STANCE, 0);
            check(image.getImage() == idle, "Stance em falta deve usar o idle próprio");
            vista.mostrarMovimento(dados.get(AcaoCombate.LOW_BLOCK), 0, 0);
            var lowStart = image.getImage();
            vista.mostrarMovimento(dados.get(AcaoCombate.LOW_BLOCK), 0, 4);
            check(image.getImage() != lowStart, "Animação visual deve avançar mesmo com um único frame lógico");
            vista.olharPara(-1);
            vista.definirPersonagem(Personagem.LUTADOR_01);
            check(image.getImage() != placeholder && image.getScaleX() == -1, "Troca preserva orientação");
            System.out.println("REAL FXGL PASS: seleção, fábrica, 37 movimentos próprios, MP/stance provisórios e troca sem reset");
            System.exit(0);
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) { launch(args); }
}
