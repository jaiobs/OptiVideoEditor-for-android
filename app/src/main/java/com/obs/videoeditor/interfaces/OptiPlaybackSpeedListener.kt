/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.videoeditor.interfaces

interface OptiPlaybackSpeedListener {
    fun processVideo(playbackSpeed: String, tempo: String)
}