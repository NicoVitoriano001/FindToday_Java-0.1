package com.app.fintoday.utils;

import android.view.MotionEvent;
import android.view.View;
/**
 * Classe utilitária para configurar movimento arrastável em FloatingActionButtons.
 * Permite que FABs sejam movidos pela tela mantendo sua funcionalidade de clique.
 */
public class FabMovementUtil {
    public static void setupFabMovement(com.google.android.material.floatingactionbutton.FloatingActionButton fab) {
        if (fab == null || fab.getParent() == null) {
            return;
        }
        fab.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = (int) fab.getX();
                        initialY = (int) fab.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int x = initialX + (int) (event.getRawX() - initialTouchX);
                        int y = initialY + (int) (event.getRawY() - initialTouchY);

                        // Limitar os movimentos para dentro da tela
                        int maxX = ((View) fab.getParent()).getWidth() - fab.getWidth();
                        int maxY = ((View) fab.getParent()).getHeight() - fab.getHeight();

                        x = Math.min(Math.max(x, 0), maxX);
                        y = Math.min(Math.max(y, 0), maxY);

                        fab.setX(x);
                        fab.setY(y);
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Verificar se foi um clique ou arrasto
                        if (Math.abs(event.getRawX() - initialTouchX) < 10 &&
                                Math.abs(event.getRawY() - initialTouchY) < 10) {
                            fab.performClick();
                            return true;
                        }
                        return true;
                }
                return false;
            }
        });
    }
}