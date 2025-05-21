package com.app.fintoday.utils;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class DrawerUtil {
    public static void setupDrawer(AppCompatActivity activity, DrawerLayout drawerLayout,
                                   NavigationView navigationView, int openDrawerDesc, int closeDrawerDesc) {
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
            if (activity instanceof DrawerItemSelectedListener) {
                return ((DrawerItemSelectedListener) activity).onDrawerItemSelected(item);
            }
            return false;
        });
    }

    public interface DrawerItemSelectedListener {
        boolean onDrawerItemSelected(android.view.MenuItem item);
    }
}