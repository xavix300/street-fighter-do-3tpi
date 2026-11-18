package pt.aepombal.tpi.ruinas.modelo;

import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Valida os doze PNGs reais do novo idle e a sequência usada pelo jogo. */
public final class IdleSpritesTeste {
    public static void main(String[] args) throws Exception {
        var idle = new RepositorioFrames().carregar(Path.of("dados", "frames.csv"))
                .get(AcaoCombate.IDLE);
        verificar(idle.frames().size() == 12, "O idle deve percorrer as doze poses");
        verificar(AcaoCombate.IDLE.repete(), "O idle deve repetir");
        for (int i = 1; i <= 12; i++) {
            try (var stream = IdleSpritesTeste.class.getResourceAsStream(AcaoCombate.IDLE.caminhoFrame(i))) {
                verificar(stream != null, "Falta idle " + i);
                var image = ImageIO.read(stream);
                verificar(image != null && image.getWidth() == 128 && image.getHeight() == 112,
                        "Tela inválida: " + i);
                verificar(image.getColorModel().hasAlpha(), "Falta alpha: " + i);
                int pixels = 0;
                int bottom = -1;
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int alpha = image.getRGB(x, y) >>> 24;
                        if (alpha == 0) continue;
                        pixels++;
                        bottom = Math.max(bottom, y);
                        verificar(x > 15 && x < 112 && y >= 5 && y < 108,
                                "Grelha, número ou recorte fora da área: " + i);
                    }
                }
                verificar(pixels > 1000, "Silhueta incompleta: " + i);
                verificar(bottom == 107, "Pés desalinhados: " + i);
            }
            var frame = idle.frames().get(i - 1);
            verificar(frame.duracaoFrames() == 8, "Cadência inesperada: " + i);
            verificar(frame.caixasDoTipo(TipoCaixa.HIT).isEmpty(), "Idle não pode atacar");
            verificar(!frame.caixasDoTipo(TipoCaixa.PUSH).isEmpty(), "Falta pushbox");
            verificar(!frame.caixasDoTipo(TipoCaixa.HURT).isEmpty(), "Falta hurtbox");
        }
        System.out.println("Idle: 12 PNGs, transparência, alinhamento e ciclo: OK");
    }

    private static void verificar(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
