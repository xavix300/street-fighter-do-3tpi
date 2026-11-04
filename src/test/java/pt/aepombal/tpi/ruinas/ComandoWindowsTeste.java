package pt.aepombal.tpi.ruinas;

/** Testes executáveis sem comando físico: java ...ComandoWindowsTeste. */
public final class ComandoWindowsTeste {
    public static void main(String[] args) {
        int[] ligado = {2};
        int[] botoes = {0xC320};
        short[] analogico = {0, 0};
        ComandoWindows comando = new ComandoWindows((indice, estado) -> {
            if (indice != ligado[0]) return 1167; // ERROR_DEVICE_NOT_CONNECTED
            estado.clear(16);
            estado.setShort(4, (short) botoes[0]);
            estado.setShort(8, analogico[0]);
            estado.setShort(10, analogico[1]);
            return 0;
        });
        verificar(comando.botoes() == 0);
        comando.iniciar();
        comando.iniciar(); // Inicialização idempotente.
        verificar(comando.botoes() == 0xC320); // Inclui Y, sem extensão de sinal.
        botoes[0] = 0;
        analogico[0] = -16000;
        analogico[1] = 16000;
        verificar(comando.botoes() == 0);
        analogico[0] = -16001;
        analogico[1] = 16001;
        verificar(comando.botoes() == 5); // Salto diagonal para a esquerda.
        analogico[0] = 16001;
        analogico[1] = -16001;
        verificar(comando.botoes() == 10);
        botoes[0] = 4;
        verificar(comando.botoes() == 14); // D-pad preservado com analógico.
        ligado[0] = -1;
        verificar(comando.botoes() == 0); // Sem direções presas ao desligar.
        ligado[0] = 3;
        analogico[0] = analogico[1] = 0;
        verificar(comando.botoes() == 4); // Volta a detetar noutra posição.
        comando.fechar();
        verificar(comando.botoes() == 0);
        comando.iniciar();
        verificar(comando.botoes() == 4);
        comando.fechar();
        // Exercita também o carregamento real da DLL (ou o fallback fora do Windows).
        ComandoWindows nativo = new ComandoWindows();
        nativo.iniciar();
        nativo.botoes();
        nativo.fechar();
        System.out.println("Comando XInput: OK");
    }

    private static void verificar(boolean condicao) {
        if (!condicao) throw new AssertionError("Leitura do comando incorreta");
    }
}
