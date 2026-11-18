package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;

/** Verifica os PNGs de movimento/defesa e o mapeamento real no CSV. */
public final class MovimentoSpritesTeste {
    public static void main(String[] args) throws Exception {
        var animations = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"));
        var counts = Map.of(AcaoCombate.WALK_FORWARD, 16, AcaoCombate.WALK_BACKWARD, 16,
                AcaoCombate.GUARD, 5, AcaoCombate.BLOCK, 5, AcaoCombate.CROUCH, 6);
        for (var entry : counts.entrySet()) {
            var action = entry.getKey();
            var animation = animations.get(action);
            verificar(animation.frames().size() == entry.getValue(), "Contagem: " + action);
            for (int i = 1; i <= entry.getValue(); i++) {
                try (var stream = MovimentoSpritesTeste.class.getResourceAsStream(action.caminhoFrame(i))) {
                    verificar(stream != null, "Falta PNG: " + action + "/" + i);
                    var image = ImageIO.read(stream);
                    verificar(image != null && image.getWidth() == 128 && image.getHeight() == 112,
                            "Tela: " + action + "/" + i);
                    verificar(image.getColorModel().hasAlpha(), "Falta alpha");
                    int count = 0, bottom = -1, top = 112;
                    for (int y = 0; y < 112; y++) {
                        for (int x = 0; x < 128; x++) {
                            if (image.getRGB(x, y) >>> 24 == 0) continue;
                            count++;
                            bottom = Math.max(bottom, y);
                            top = Math.min(top, y);
                            verificar(x > 10 && x < 118 && y > 4 && y < 108,
                                    "Artefacto fora da silhueta: " + action + "/" + i);
                        }
                    }
                    verificar(count > 900, "Silhueta incompleta: " + action + "/" + i);
                    verificar(bottom == 107, "Chão desalinhado: " + action + "/" + i);
                    if (action == AcaoCombate.CROUCH) {
                        verificar(top >= 30, "O ciclo agachado não deve voltar a ficar em pé");
                    }
                }
                var frame = animation.frames().get(i - 1);
                verificar(frame.caixasDoTipo(TipoCaixa.HIT).isEmpty(), "Movimento não deve atacar");
                verificar(!frame.caixasDoTipo(TipoCaixa.PUSH).isEmpty(), "Falta pushbox");
                verificar(!frame.caixasDoTipo(TipoCaixa.HURT).isEmpty(), "Falta hurtbox");
            }
            if (action == AcaoCombate.WALK_FORWARD || action == AcaoCombate.WALK_BACKWARD) {
                verificar(animation.frames().stream().mapToInt(FrameCombate::duracaoFrames).sum() == 80,
                        "A duração do ciclo de andar deve ser preservada");
            }
        }
        System.out.println("Sprites: avanço 16, recuo 16, guarda/bloqueio 5, agachamento 6: OK");
    }

    private static void verificar(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
