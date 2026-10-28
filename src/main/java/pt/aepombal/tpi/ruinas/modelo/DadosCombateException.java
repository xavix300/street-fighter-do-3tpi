package pt.aepombal.tpi.ruinas.modelo;

// [IA] Exceção específica para dados externos de combate inválidos.
/** Lançada quando o ficheiro de frames não pode ser lido ou tem dados inválidos. */
public class DadosCombateException extends Exception {

    public DadosCombateException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    public DadosCombateException(String mensagem) {
        super(mensagem);
    }
}
