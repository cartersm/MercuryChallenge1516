package edu.rosehulman.mercury.mercurycontroller;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MercuryAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.startup_appwidget);
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
        ComponentName comp = new ComponentName(context.getPackageName(), MercuryAppWidgetProvider.class.getName());
        appWidgetManager.updateAppWidget(comp, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.startup_appwidget);
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        remoteViews.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
