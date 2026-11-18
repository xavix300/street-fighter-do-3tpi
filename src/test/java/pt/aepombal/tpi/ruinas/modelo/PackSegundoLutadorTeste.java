package pt.aepombal.tpi.ruinas.modelo;

import java.util.Properties;
import javax.imageio.ImageIO;

/** Recursos realmente importados: folhas dos lotes 01/02 e previews ainda provisórios. */
public final class PackSegundoLutadorTeste {
    public static void main(String[] args) throws Exception {
        int actions = 0, poses = 0;
        for (var action : AcaoCombate.values()) {
            String folder = "/assets/textures/fighters/fighter_02/" + action.pastaAssets() + "/";
            try (var stream = PackSegundoLutadorTeste.class.getResourceAsStream(folder + "animacao.properties")) {
                if (action == AcaoCombate.IDLE || action == AcaoCombate.STANCE || action == AcaoCombate.MP) {
                    check(stream == null, "Não fabricar uma sequência inexistente: " + action);
                    continue;
                }
                check(stream != null, "Falta movimento: " + action);
                Properties properties = new Properties();
                properties.load(stream);
                int count = Integer.parseInt(properties.getProperty("poses"));
                check(count >= 8 && count <= 24, "Contagem real da sequência inesperada: " + action);
                if (action.eAtaque()) {
                    var plan = new PlanoVisualAtaque(
                            Integer.parseInt(properties.getProperty("preparacao")),
                            Integer.parseInt(properties.getProperty("ativos")),
                            Integer.parseInt(properties.getProperty("recuperacao")));
                    check(plan.quantidade() == count, "Plano visual não corresponde aos PNGs");
                }
                for (int i = 1; i <= count; i++) {
                    try (var png = PackSegundoLutadorTeste.class.getResourceAsStream(
                            folder + action.pastaAssets() + "_%03d.png".formatted(i))) {
                        check(png != null, "Falta pose " + action + "/" + i);
                        var image = ImageIO.read(png);
                        check(image != null && image.getWidth() == 128 && image.getHeight() == 112,
                                "Tela inválida");
                        check(image.getColorModel().hasAlpha(), "Falta alpha");
                        int pixels = 0, bottom = -1;
                        for (int y = 0; y < 112; y++) {
                            for (int x = 0; x < 128; x++) {
                                if (image.getRGB(x, y) >>> 24 == 0) continue;
                                pixels++;
                                bottom = Math.max(bottom, y);
                                check(x >= 8 && x < 120 && y >= 8 && y < 108,
                                        "Recorte fora da tela: " + action + "/" + i);
                            }
                        }
                        check(pixels > 200 && bottom == 107, "Silhueta vazia ou chão desalinhado");
                    }
                }
                actions++;
                poses += count;
            }
        }
        check(actions == 37, "Devem existir 37 movimentos importados, além do idle");
        check(poses == 507, "Contagem do pack misto deve incluir as novas folhas");
        System.out.println("Pack provisório: " + actions + " movimentos, " + poses + " poses reais: OK");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
