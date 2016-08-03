package org.edx.mobile.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;

/**
 * Utility class for interacting with an app store, or the Play Store specifically.
 */
public final class AppStoreUtils {
    // Make this class non-instantiable
    private AppStoreUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param context A Context to query the applications info.
     *
     * @return Whether there is any app store or web browser registered on the device.
     */
    public static boolean hasAppStore(@NonNull final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        if (intent.resolveActivity(packageManager) != null) return true;
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                BuildConfig.APPLICATION_ID));
        return intent.resolveActivity(packageManager) != null;
    }

    /**
     * Open an app store to display the app, or a web browser displaying it's web page if no app
     * store is available.
     *
     * @param context A Context for starting the new Activity.
     */
    public static void openAppInAppStore(@NonNull final Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" +
                            BuildConfig.APPLICATION_ID));
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                // There is not app store or web browser registered on the device. Show a
                // toast message to that effect.
                Toast.makeText(context, R.string.app_version_upgrade_app_store_unavailable,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Generic click listener that opens an app store to display the app. This is created as a
     * convenience, because this utility seems to be mostly invoked from a click listener.
     */
    public static final View.OnClickListener OPEN_APP_IN_APP_STORE_CLICK_LISTENER =
            new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    openAppInAppStore(v.getContext());
                }
            };
}
