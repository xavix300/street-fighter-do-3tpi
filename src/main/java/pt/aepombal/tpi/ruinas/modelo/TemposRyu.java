package pt.aepombal.tpi.ruinas.modelo;

import java.util.ArrayList;
import java.util.List;

/** Normais de SF6: https://www.streetfighter.com/6/character/ryu/frame (consulta 2026-11-04).
 * Arranque inclui o primeiro frame ativo: um LP de 4f tem apenas 3f sem hitbox.
 * As fotografias e caixas locais são preservadas, mas não determinam a velocidade.
 */
public record TemposRyu(int arranque, int ativos, int recuperacao,
                        int vantagemHit, int vantagemBlock, int extraFalha) {
    public static TemposRyu de(AcaoCombate acao) {
        return switch (acao) {
            case LP -> new TemposRyu(4, 3, 7, 4, -1, 0);
            case LK -> new TemposRyu(5, 3, 11, 2, -4, 0);
            case MP -> new TemposRyu(6, 4, 11, 7, -1, 2);
            case MK -> new TemposRyu(9, 3, 18, 4, -4, 0);
            case HP -> new TemposRyu(10, 5, 18, 4, -2, 0);
            case HK -> new TemposRyu(12, 4, 20, 9, 1, 0);
            case CROUCH_LP -> new TemposRyu(4, 2, 9, 4, -1, 0);
            case CROUCH_LK -> new TemposRyu(5, 2, 10, 3, -1, 0);
            case CROUCH_MP -> new TemposRyu(6, 4, 13, 5, 0, 2);
            case CROUCH_MK -> new TemposRyu(8, 3, 19, 1, -6, 0);
            case CROUCH_HP, ANTI_AIR_MP, ANTI_AIR_CROUCH_MP -> new TemposRyu(9, 6, 22, 1, -7, 0);
            // O sweep derruba no original: não existe vantagem de hit numérica equivalente.
            case CROUCH_HK -> new TemposRyu(9, 3, 23, 0, -12, 0);
            // Recuperação aérea não é um tempo fixo: termina ao aterrar, com 3f de landing.
            case AIR_LP -> new TemposRyu(4, 10, 0, 0, 0, 0);
            case AIR_LK -> new TemposRyu(6, 10, 0, 0, 0, 0);
            case AIR_MP -> new TemposRyu(8, 5, 0, 0, 0, 0);
            case AIR_MK -> new TemposRyu(7, 6, 0, 0, 0, 0);
            case AIR_HP -> new TemposRyu(9, 6, 0, 0, 0, 0);
            case AIR_HK -> new TemposRyu(10, 8, 0, 0, 0, 0);
            default -> throw new IllegalArgumentException("Não é ataque: " + acao);
        };
    }

    /** Redistribui as poses por fase sem esticar o relógio lógico nem antecipar o impacto. */
    public List<FrameCombate> aplicar(AcaoCombate acao, List<FrameCombate> originais) {
        int primeiro = -1, ultimo = -1;
        for (int i = 0; i < originais.size(); i++) {
            if (originais.get(i).temHitbox()) {
                if (primeiro < 0) primeiro = i;
                ultimo = i;
            }
        }
        if (primeiro <= 0 || ultimo == originais.size() - 1) {
            throw new IllegalArgumentException("Ataque sem fases completas: " + acao);
        }
        List<FrameCombate> resultado = new ArrayList<>();
        fase(resultado, originais.subList(0, primeiro), arranque - 1, acao);
        fase(resultado, originais.subList(primeiro, ultimo + 1), ativos, acao);
        // No ar estas últimas poses são apenas visuais; a aterragem controla a recuperação.
        fase(resultado, originais.subList(ultimo + 1, originais.size()),
                acao.eAtaqueAereo() ? originais.size() - ultimo - 1 : recuperacao, acao);
        return List.copyOf(resultado);
    }

    private void fase(List<FrameCombate> destino, List<FrameCombate> poses, int duracao,
                      AcaoCombate acao) {
        if (duracao < poses.size()) throw new IllegalArgumentException("Demasiadas poses: " + acao);
        for (int i = 0; i < poses.size(); i++) {
            int tempo = (i + 1) * duracao / poses.size() - i * duracao / poses.size();
            List<CaixaFrame> caixas = poses.get(i).caixas().stream().map(caixa -> {
                if (caixa.tipo() != TipoCaixa.HIT || acao.eAtaqueAereo()) return caixa;
                int hit = acao == AcaoCombate.CROUCH_HK ? caixa.hitStun()
                        : ativos + recuperacao - 1 + vantagemHit;
                return new CaixaFrame(caixa.tipo(), caixa.identificador(), caixa.caixa(), caixa.dano(),
                        hit, ativos + recuperacao - 1 + vantagemBlock);
            }).toList();
            destino.add(new FrameCombate(tempo, caixas));
        }
    }
}
