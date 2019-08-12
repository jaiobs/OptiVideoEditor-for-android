/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class OptiCommonMethods {

    private static String tagName = OptiCommonMethods.class.getSimpleName();

    //write intent data into file
    public static File writeIntoFile(Context context, Intent data, File file) {

        AssetFileDescriptor videoAsset = null;
        try {
            videoAsset = context.getContentResolver().openAssetFileDescriptor(data.getData(), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileInputStream in;
        try {
            in = videoAsset.createInputStream();

            OutputStream out = null;
            out = new FileOutputStream(file);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    //copy file from one source file to destination file
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    //get video duration in seconds
    public static long convertDurationInSec(long duration) {
        return (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    //get video duration in minutes
    public static long convertDurationInMin(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "min: " + minutes);

        if (minutes > 0) {
            return minutes;
        } else {
            return 0;
        }
    }

    //get video duration in minutes & seconds
    public static String convertDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "min: " + minutes);

        if (minutes > 0) {
            return minutes + "";
        } else {
            return "00:" + (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        }
    }

    //get video duration based on uri
    public static int getMediaDuration(Context context, Uri uriOfFile) {
        MediaPlayer mp = MediaPlayer.create(context, uriOfFile);
        return mp.getDuration();
    }

    //get file extension based on file path
    public static String getFileExtension(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf("."));
        Log.v(tagName, "extension: " + extension);
        return extension;
    }
}
