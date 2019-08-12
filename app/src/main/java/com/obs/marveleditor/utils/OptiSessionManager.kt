/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class OptiSessionManager {

    private lateinit var preferences: SharedPreferences

    public fun setFirstTime(activity: Activity, isFirstTime: Boolean) {
        preferences = activity.getSharedPreferences("storage_permission", Context.MODE_PRIVATE)
        preferences.edit().putBoolean("isFirstTime", isFirstTime).apply()
    }

    public fun isFirstTime(activity: Activity): Boolean {
        preferences = activity.getSharedPreferences("storage_permission", Context.MODE_PRIVATE)
        return preferences.getBoolean("isFirstTime", true)
    }
}