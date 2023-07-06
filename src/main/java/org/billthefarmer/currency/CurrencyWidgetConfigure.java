////////////////////////////////////////////////////////////////////////////////
//
//  Currency - An android currency converter.
//
//  Copyright (C) 2016	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.currency;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;

// CurrencyWidgetConfigure
@SuppressWarnings("deprecation")
public class CurrencyWidgetConfigure extends Activity
{
    // On create
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Get preferences
        SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(this);

        int theme = Integer.parseInt(preferences.getString(Main.PREF_THEME, "0"));

        Configuration config = getResources().getConfiguration();
        int night = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (theme)
        {
        case Main.LIGHT:
            setTheme(R.style.DialogLightTheme);
            break;

        case Main.DARK:
            setTheme(R.style.DialogTheme);
            break;

        case Main.SYSTEM:
            switch (night)
            {
            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.DialogLightTheme);
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.DialogTheme);
                break;
            }
            break;
        }

        setContentView(R.layout.config);

        // Find views
        ListView listView = findViewById(R.id.list);
        Button cancel = findViewById(R.id.cancel);

        // Set the listeners
        if (listView != null)
            listView.setOnItemClickListener((parent, view, position, id) ->
        {
            Intent intent = getIntent();
            int appWidgetId =
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                   AppWidgetManager.INVALID_APPWIDGET_ID);
            // Get editor
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(String.valueOf(appWidgetId), position);
            editor.apply();

            Intent broadcast = new Intent
                (AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int appWidgetIds[] = {appWidgetId};
            broadcast.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                               appWidgetIds);
            sendBroadcast(broadcast);

            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, result);
            finish();
        });

        if (cancel != null)
            cancel.setOnClickListener((v) ->
        {
            setResult(RESULT_CANCELED);
            finish();
        });

        // Get saved currency list
        String namesJSON = preferences.getString(Main.PREF_NAMES, null);
        List<String> nameList = new ArrayList<String>();

        // Check saved name list
        if (namesJSON != null)
        {
            try
            {
                // Update name list from JSON array
                JSONArray namesArray = new JSONArray(namesJSON);
                for (int i = 0; !namesArray.isNull(i); i++)
                    nameList.add(namesArray.getString(i));
            }

            catch (Exception e) {}
        }

        // Use the default list
        else
        {
            nameList.addAll(Arrays.asList(Main.CURRENCY_LIST));
        }

        // Populate the lists
        List<Main.Currency> currencyList = new ArrayList<>();
        List<Integer> selectList = new ArrayList<>();
        for (String name: nameList)
        {
            int index = Main.currencyIndex(name);
            currencyList.add(Main.CURRENCIES[index]);
        }

        // Create the adapter
        ChoiceAdapter adapter = new ChoiceAdapter(this, R.layout.choice,
                                                  currencyList, selectList);
        // Set the adapter
        if (listView != null)
            listView.setAdapter(adapter);
    }
}
