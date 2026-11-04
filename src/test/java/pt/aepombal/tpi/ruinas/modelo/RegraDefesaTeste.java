package pt.aepombal.tpi.ruinas.modelo;

/** Teste executável sem dependências adicionais: java -ea ...RegraDefesaTeste. */
public final class RegraDefesaTeste {
    public static void main(String[] args) {
        for (AcaoCombate ataque : new AcaoCombate[]{AcaoCombate.LP, AcaoCombate.MP,
                AcaoCombate.HP, AcaoCombate.LK, AcaoCombate.MK, AcaoCombate.HK}) {
            verificar(RegraDefesa.bloqueia(AcaoCombate.WALK_BACKWARD, ataque, true, false, true, true));
            verificar(RegraDefesa.bloqueia(AcaoCombate.LOW_BLOCK, ataque, true, true, true, true));
            verificar(!RegraDefesa.bloqueia(AcaoCombate.IDLE, ataque, false, false, true, true));
            verificar(!RegraDefesa.bloqueia(AcaoCombate.LP, ataque, true, false, true, true));
            verificar(!RegraDefesa.bloqueia(AcaoCombate.WALK_BACKWARD, ataque, true, false, false, true));
            verificar(!RegraDefesa.bloqueia(AcaoCombate.WALK_BACKWARD, ataque, true, false, true, false));
        }
        System.out.println("Regras de defesa: OK");
    }

    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Regra de defesa falhou");
    }
}
