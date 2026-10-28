package pt.aepombal.tpi.ruinas.modelo;

/** Resultado efetivo de um contacto, usado para combos e frame advantage. */
public record ResultadoImpacto(
        boolean bloqueado,
        boolean quebraGuarda,
        int stunFrames,
        int danoAplicado) {
}
