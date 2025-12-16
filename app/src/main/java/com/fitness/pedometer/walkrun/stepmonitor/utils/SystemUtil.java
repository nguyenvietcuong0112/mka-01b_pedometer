package com.fitness.pedometer.walkrun.stepmonitor.utils;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.ContextCompat;

import com.fitness.pedometer.walkrun.stepmonitor.notiSpecial.WakeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SystemUtil {
    private static Locale myLocale;

    // Lưu ngôn ngữ đã cài đặt
    public static void saveLocale(Context context, String lang) {
        setPreLanguage(context, lang);
    }

    // Load lại ngôn ngữ đã lưu và thay đổi chúng
    public static void setLocale(Context context) {
        String language = getPreLanguage(context);
        if (language.equals("")) {
            Configuration config = new Configuration();
            Locale locale = Locale.getDefault();
            Locale.setDefault(locale);
            config.locale = locale;

            context.getResources()
                    .updateConfiguration(config, context.getResources().getDisplayMetrics());
        } else {
            changeLang(language, context);
        }
    }

    // method phục vụ cho việc thay đổi ngôn ngữ.
    public static void changeLang(String lang, Context context) {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(context, lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static String getPreLanguage(Context mContext) {
        SharedPreferences preferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE);
        return preferences.getString("KEY_LANGUAGE", "en");
    }

    public static void setPreLanguage(Context context, String language) {
        if (language == null || language == "") {
            return;
        } else {
            SharedPreferences preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("KEY_LANGUAGE", language);
            editor.apply();
        }
    }

    public static String getPath(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public static File fileFromContentUri(Context context, Uri contentUri) throws IOException {
        String abc = "";
        String fileName = "";
        try {
            String fileDevice = getPath(context, contentUri);
            File file = new File(fileDevice);
            abc = file.getName();
        } catch (Exception e) {
            String fileExtension = getFileExtension(context, contentUri);
            try {
                String listUri[] = contentUri.toString().split("/");
                String fileNameEx = listUri[listUri.length - 1];
                Log.e("fileNameEx", fileNameEx);
                String urlDecodedTitle = URLDecoder.decode(fileNameEx, StandardCharsets.UTF_8.toString());
                String listUri2[] = urlDecodedTitle.split("_");
                if (listUri2.length > 1) {
                    for (int i = 0; i < listUri2.length - 1; i++) {
                        abc += listUri2[i];
                        if (i < listUri2.length - 2) {
                            abc += "_";
                        }
                    }
                } else {
                    abc = urlDecodedTitle;
                }
                abc += "." + fileExtension;
            } catch (Exception x) {
                if (fileExtension != null) {
                    abc = abc + fileExtension;
                } else {
                    abc = "";
                }
            }
        }
        fileName = abc;
        File tempFile = new File(context.getCacheDir(), fileName);
        tempFile.createNewFile();
        try {
            FileOutputStream oStream = new FileOutputStream(tempFile);
            InputStream inputStream = context.getContentResolver().openInputStream(contentUri);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                oStream.write(buf, 0, len);
            }
            oStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return tempFile;
        }
        return tempFile;
    }

    private static String getFileExtension(Context context, Uri uri) {
        String fileType = context.getContentResolver().getType(uri);
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private static void startCheckingPermissionNoti(Activity mContext, Class<?> nextActivityClass) {
        Handler handler = new Handler();
        Runnable checkPermissionTask = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(mContext, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            Intent serviceIntent = new Intent(mContext, WakeService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mContext.startForegroundService(serviceIntent);
                            } else {
                                mContext.startService(serviceIntent);
                            }
                        } catch (Exception e) {
                            // Silently handle any exceptions
                        }
                        backToAppNoti(mContext,nextActivityClass);
                    } else {
                        handler.postDelayed(this, 300);
                    }
                }
            }
        };
        handler.postDelayed(checkPermissionTask, 300);
    }
    private static void backToAppNoti(Activity mContext,Class<?> nextActivityClass) {
        Intent intent = new Intent(mContext, nextActivityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
    }

    public static boolean checkPermissionNoty(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            return ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}