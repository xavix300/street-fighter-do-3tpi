package pt.aepombal.tpi.ruinas.modelo;

import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;

/** As dez folhas independentes substituem apenas as sequências correspondentes. */
public final class LotePrimeiroSpritesTeste {
    public static void main(String[] args) throws Exception {
        var counts = Map.of(
                "idle", 12, "guard", 12, "walk_forward", 16, "walk_backward", 16,
                "crouch", 12, "block", 12, "low_block", 12,
                "jump", 24, "jump_side", 24, "lp", 20);
        int total = 0;
        for (var entry : counts.entrySet()) {
            String name = entry.getKey();
            String folder = "/assets/textures/fighters/fighter_02/" + name + "/";
            if (!name.equals("idle")) {
                Properties properties = new Properties();
                try (var stream = LotePrimeiroSpritesTeste.class.getResourceAsStream(folder + "animacao.properties")) {
                    check(stream != null, "Falta configuração: " + name);
                    properties.load(stream);
                }
                check(Integer.parseInt(properties.getProperty("poses")) == entry.getValue(),
                        "Número de poses não corresponde à folha: " + name);
                if (name.equals("lp")) {
                    check(properties.getProperty("preparacao").equals("8")
                            && properties.getProperty("ativos").equals("2")
                            && properties.getProperty("recuperacao").equals("10"),
                            "LP deve usar as poses 9–10 no contacto");
                }
            }
            for (int i = 1; i <= entry.getValue(); i++) {
                try (var stream = LotePrimeiroSpritesTeste.class.getResourceAsStream(
                        folder + name + "_%03d.png".formatted(i))) {
                    check(stream != null, "Falta pose: " + name + "/" + i);
                    var image = ImageIO.read(stream);
                    check(image != null && image.getWidth() == 128 && image.getHeight() == 112,
                            "Dimensões incompatíveis com o jogo");
                    check(image.getColorModel().hasAlpha(), "Falta transparência: " + name);
                    for (int y = 0; y < 112; y++) {
                        check(image.getRGB(0, y) >>> 24 == 0 && image.getRGB(127, y) >>> 24 == 0,
                                "Fundo residual na borda: " + name);
                    }
                    total++;
                }
            }
            check(LotePrimeiroSpritesTeste.class.getResource(folder + name + "_%03d.png".formatted(entry.getValue() + 1)) == null,
                    "Pose antiga extra: " + name);
        }
        check(total == 160, "O lote deve conter 160 poses reais");
        System.out.println("Lote 01: 10 folhas, 160 poses transparentes: OK");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
