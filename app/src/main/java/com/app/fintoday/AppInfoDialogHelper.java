package com.app.fintoday;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class AppInfoDialogHelper {
    private Context context;

    public AppInfoDialogHelper(Context context) {
        this.context = context;
    }

    public void showAboutDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Sobre o App FinToday")
                .setMessage("FinToday v0.1.4.30\n\n" +
                        "Um aplicativo para gerenciamento de finanças pessoais.\n\n" +
                        "Desenvolvido por: nicovitoriano@gmail.com\n" +
                        "Adaptado para Anabelle\n" +
                   //   "Versão: 1.0\n" +
                        "Maio 2025")
                .setPositiveButton("OK", null)
                .show();
    }

    public void openHelpScreen() {
        new AlertDialog.Builder(context)
                .setTitle("Ajuda")
                .setMessage("Como usar o FinToday:\n\n" +
                        "1. Adicione novas despesas clicando no botão '+'\n" +
                        "2. Visualize seu resumo financeiro no menu\n" +
                        "3. Banco dados em Download/FIN_TODAY\n" +
                        "4. Deve dar permissão de Acesso ao app\n\n" +
                        "Dúvidas? Entre em contato pelo menu 'Contato'")
                .setPositiveButton("OK", null)
                .show();
    }
}