package pt.aepombal.tpi.ruinas.logica;

/** Teste executável sem dependências adicionais. */
public final class ReacaoContactoTeste {
    public static void main(String[] args) {
        verificar(false, 100, 200, 150);
        verificar(true, 100, 200, 90);
        verificar(false, 200, 100, -150);
        verificar(true, 200, 100, -90);
        verificar(false, 100, 100, 150);
        verificar(true, 100, 100, 90);
        System.out.println("Reações de contacto: OK");
    }

    private static void verificar(boolean bloqueado, double atacanteX, double defensorX,
                                  double velocidadeEsperada) {
        ReacaoContacto reacao = ReacaoContacto.calcular(bloqueado, atacanteX, defensorX);
        if (reacao.pausaFrames() != 5 || reacao.recuoFrames() != 9
                || reacao.velocidadeRecuo() != velocidadeEsperada) {
            throw new AssertionError("Reação de contacto incorreta: " + reacao);
        }
    }
}
