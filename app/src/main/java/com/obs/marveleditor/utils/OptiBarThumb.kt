/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Solutions Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.obs.marveleditor.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.obs.marveleditor.R
import java.util.*


class OptiBarThumb private constructor() {

    var index: Int = 0
        private set
    var `val`: Float = 0.toFloat()
    var pos: Float = 0.toFloat()
    var bitmap: Bitmap? =null
        private set(bitmap) {
            field = bitmap
            widthBitmap = bitmap?.width ?: 24
            heightBitmap = bitmap?.height ?: 24
        }
    var widthBitmap: Int = 0
        private set
    private var heightBitmap: Int = 0

    var lastTouchX: Float = 0.toFloat()

    init {
        `val` = 0f
        pos = 0f
    }

    companion object {

        val LEFT = 0
        val RIGHT = 1

        fun initThumbs(resources: Resources): List<OptiBarThumb> {

            val barThumbs = Vector<OptiBarThumb>()

            for (i in 0..1) {
                val th = OptiBarThumb()
                th.index = i
                if (i == 0) {
                    val resImageLeft = R.drawable.ic_video_cutline
                    th.bitmap = (BitmapFactory.decodeResource(resources, resImageLeft))
                } else {
                    val resImageRight = R.drawable.ic_video_cutline
                    th.bitmap  = (BitmapFactory.decodeResource(resources, resImageRight))
                }
                barThumbs.add(th)
            }

            return barThumbs
        }

        fun getWidthBitmap(barThumbs: List<OptiBarThumb>): Int {
            return barThumbs[0].widthBitmap
        }

        fun getHeightBitmap(barThumbs: List<OptiBarThumb>): Int {
            return barThumbs[0].heightBitmap
        }
    }
}
