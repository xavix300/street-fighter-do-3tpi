package pt.aepombal.tpi.ruinas.modelo;

/** Decide a defesa independentemente da animação apresentada. */
public final class RegraDefesa {
    private RegraDefesa() { }

    public static boolean bloqueia(AcaoCombate estado, AcaoCombate ataque,
                                    boolean recuar, boolean baixo, boolean noChao,
                                    boolean guardaDisponivel) {
        if (!guardaDisponivel || !noChao || !ataque.eAtaque()) return false;
        // Durante blockstun a defesa continua até ao fim da reação.
        boolean emBloqueio = estado == AcaoCombate.BLOCK || estado == AcaoCombate.LOW_BLOCK;
        boolean livre = estado == AcaoCombate.IDLE || estado == AcaoCombate.CROUCH || estado == AcaoCombate.STANCE
                || estado == AcaoCombate.WALK_FORWARD || estado == AcaoCombate.WALK_BACKWARD;
        if (!emBloqueio && !(livre && recuar)) return false;
        boolean defesaBaixa = emBloqueio ? estado == AcaoCombate.LOW_BLOCK : baixo;
        // Os ataques normais são médios: podem ser defendidos em pé ou em baixo.
        // Um ataque baixo exige defesa baixa.
        if (ataque.exigeBloqueioAlto()) return !defesaBaixa;
        return !ataque.exigeBloqueioBaixo() || defesaBaixa;
    }
}
