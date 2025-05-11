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
                .setTitle("Sobre o App")
                .setMessage("FinToday v1.0\n\n" +
                        "Um aplicativo para gerenciamento de finanças pessoais.\n\n" +
                        "Desenvolvido por: [Seu Nome]\n" +
                        "Versão: 1.0\n" +
                        "Ano: 2023")
                .setPositiveButton("OK", null)
                .show();
    }

    public void openHelpScreen() {
        new AlertDialog.Builder(context)
                .setTitle("Ajuda")
                .setMessage("Como usar o FinToday:\n\n" +
                        "1. Adicione novas despesas clicando no botão '+'\n" +
                        "2. Visualize seu resumo financeiro no menu\n" +
                        "3. Faça backup regularmente para proteger seus dados\n\n" +
                        "Dúvidas? Entre em contato pelo menu 'Contato'")
                .setPositiveButton("OK", null)
                .show();
    }
}