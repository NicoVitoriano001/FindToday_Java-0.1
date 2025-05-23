package com.app.fintoday.utils;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;

import com.app.fintoday.R;
import com.app.fintoday.ui.MainActivity;
import com.app.fintoday.ui.NewFinActivity;
import com.app.fintoday.ui.ResumoDespGrafActivity;
import com.google.android.material.navigation.NavigationView;

public class DrawerUtil {
    public interface DrawerActions {
        void onBackupRequested();
        void onRestoreRequested();
        void onSyncRequested();
    }

    public static void setupDrawer(AppCompatActivity activity, DrawerLayout drawerLayout,
                                   NavigationView navigationView, int openDrawerDesc, int closeDrawerDesc,
                                   DrawerActions drawerActions, AppInfoDialogHelper appInfoHelper) {
        // Configuração do ActionBarDrawerToggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                openDrawerDesc,
                closeDrawerDesc);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configura o listener de clique nos itens do menu
        navigationView.setNavigationItemSelectedListener(item -> {
            handleDrawerItemClick(activity, drawerLayout, item, drawerActions, appInfoHelper);
            return true;
        });
    }

    private static void handleDrawerItemClick(AppCompatActivity activity, DrawerLayout drawerLayout,
                                              android.view.MenuItem item, DrawerActions drawerActions,
                                              AppInfoDialogHelper appInfoHelper) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startNewActivity(activity, MainActivity.class);
                break;
            case R.id.nav_resumo_desp_graf:
                startNewActivity(activity, ResumoDespGrafActivity.class);
                break;
            case R.id.nav_nova_desp:
                startNewActivity(activity, NewFinActivity.class);
                break;
            case R.id.nav_fazer_bkp:
                if (drawerActions != null) {
                    drawerActions.onBackupRequested();
                }
                break;
            case R.id.nav_restoreDB:
                if (drawerActions != null) {
                    drawerActions.onRestoreRequested();
                }
                break;
            case R.id.nav_sync_firebase:
                if (drawerActions != null) {
                    drawerActions.onSyncRequested();
                }
                break;
            case R.id.nav_about:
                if (appInfoHelper != null) {
                    appInfoHelper.showAboutDialog();
                }
                break;
            case R.id.nav_help:
                if (appInfoHelper != null) {
                    appInfoHelper.openHelpScreen();
                }
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private static void startNewActivity(AppCompatActivity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}