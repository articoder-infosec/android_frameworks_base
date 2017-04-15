/*
 * Copyright (c) 2017 Project Substratum
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * Also add information on how to contact you by electronic and paper mail.
 *
 */

package com.android.systemui.qs.tiles;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSDetailItemsList;
import com.android.systemui.qs.QSDetailItems.Item;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.ResourceIcon;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ThemeTile extends QSTile<QSTile.State> {
    private static final String TAG = ThemeTile.class.getSimpleName();

    private static final String PROFILES_DIR = Environment.getExternalStorageDirectory()
            + File.separator
            + "substratum"
            + File.separator
            + "profiles";

    private static final String EXTRA_PROFILE_URI = "profile_uri";
    private static final String EXTRA_PROFILE_NAME = "profile_name";
    private static final String SUBSTRATUM_PACKAGE = "projekt.substratum";
    private static final String PROFILE_SERVICE = "projekt.substratum.services.ProfileChangeService";

    public ThemeTile(Host host) {
        super(host);
    }

    @Override
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override
    protected void handleClick() {
        showDetail(true);
    }
    
    @Override
    public DetailAdapter getDetailAdapter() {
        return new ThemesDetailAdapter();
    }

    @Override
    protected void handleUpdateState(QSTile.State state, Object arg) {
        state.icon = ResourceIcon.get(R.drawable.ic_qs_themes_on);
        state.label = mContext.getString(R.string.quick_settings_substratum_profiles);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public Intent getLongClickIntent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void setListening(boolean listening) {
        // TODO Auto-generated method stub

    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_substratum_profiles);
    }

    @Override
    public boolean isAvailable() {
        return isSubstratumInstalled(mContext);
    }

    static boolean isSubstratumInstalled(Context context) {
        return isPackageInstalled(context, SUBSTRATUM_PACKAGE, true);
    }

    // This method determines whether a specified package is installed (enabled OR disabled)
    static boolean isPackageInstalled(Context context, String package_name, boolean enabled) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(package_name, 0);
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(package_name, PackageManager.GET_ACTIVITIES);
            if (enabled) {
                return ai.enabled;
            }
            // if package doesn't exist, an Exception will be thrown, so return true in every case
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected List<File> getProfileFiles() {
        File profileDir = new File(PROFILES_DIR);
        if (!profileDir.exists() || !profileDir.isDirectory()) {
            Log.i(TAG, "profile directory does not exist or is not a directory");
            return null;
        }
        List<File> files = Arrays.asList(profileDir.listFiles());
        if (files.isEmpty()) {
            Log.i(TAG, "profile directory is empty");
            return null;
        }
        Collections.sort(files);
        return files;
    }

    private final class ThemesDetailAdapter implements DetailAdapter,
            AdapterView.OnItemClickListener {

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.quick_settings_substratum_profiles);
        }

        @Override
        public Boolean getToggleState() {
            return null;
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            List<Item> items = new ArrayList<>();
            QSDetailItemsList itemsList = QSDetailItemsList.convertOrInflate(context, convertView, parent);
            QSDetailItemsList.QSDetailListAdapter adapter = new QSDetailItemsList.QSDetailListAdapter(context, items);
            adapter.setBiggerHeight(false);
            ListView listView = itemsList.getListView();
            listView.setDivider(null);
            listView.setOnItemClickListener(this);
            listView.setAdapter(adapter);
            Item item;
            List<File> files = getProfileFiles();
            // TODO: any situation where we don't produce a list, show an
            // alternate view (some kind of user hint perhaps)
            if (files != null) {
                for (File f : files) {
                    item = new Item();
                    item.line1 = f.getName();
                    item.overlay = mContext.getDrawable(R.drawable.ic_qs_themes_on);
                    item.tag = f.toURI().toString();
                    items.add(item);
                }
            }
            adapter.notifyDataSetChanged();
            return itemsList;
        }

        @Override
        public Intent getSettingsIntent() {
            return null;
        }

        @Override
        public void setToggleState(boolean state) {
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.OMNI_SETTINGS;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Item selectedItem = (Item) parent.getItemAtPosition(position);
            Intent intent = new Intent();
            intent.setClassName(SUBSTRATUM_PACKAGE, PROFILE_SERVICE);
            intent.putExtra(EXTRA_PROFILE_URI, (String)selectedItem.tag);
            intent.putExtra(EXTRA_PROFILE_NAME, selectedItem.line1);
            Log.i(TAG, "Selected " + selectedItem.line1 + " with URI " + (String)selectedItem.tag);
            Toast.makeText(mContext, "You selected profile " + selectedItem.line1,
                    Toast.LENGTH_SHORT).show();
            // mContext.startServiceAsUser(intent, UserHandle.CURRENT);
        }
    }
}
