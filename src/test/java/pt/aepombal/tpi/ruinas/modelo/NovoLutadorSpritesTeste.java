package pt.aepombal.tpi.ruinas.modelo;

import javax.imageio.ImageIO;

/** Valida os recursos preparados do segundo lutador, sem o adicionar ao combate. */
public final class NovoLutadorSpritesTeste {
    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 12; i++) {
            String path = "/assets/textures/fighters/fighter_02/idle/idle_%03d.png".formatted(i);
            try (var stream = NovoLutadorSpritesTeste.class.getResourceAsStream(path)) {
                verificar(stream != null, "Falta PNG: " + path);
                var image = ImageIO.read(stream);
                verificar(image != null && image.getWidth() == 128 && image.getHeight() == 112,
                        "Dimensões inválidas: " + path);
                verificar(image.getColorModel().hasAlpha(), "Falta transparência: " + path);
                int count = 0, bottom = -1;
                for (int y = 0; y < 112; y++) {
                    for (int x = 0; x < 128; x++) {
                        if (image.getRGB(x, y) >>> 24 == 0) continue;
                        count++;
                        bottom = Math.max(bottom, y);
                        verificar(x > 15 && x < 112 && y >= 5 && y < 108,
                                "Artefactos fora da área: " + path);
                    }
                }
                verificar(count > 900, "Silhueta incompleta: " + path);
                verificar(bottom == 107, "Chão desalinhado: " + path);
            }
        }
        System.out.println("Segundo lutador: 12 poses de idle preparadas e separadas: OK");
    }

    private static void verificar(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
