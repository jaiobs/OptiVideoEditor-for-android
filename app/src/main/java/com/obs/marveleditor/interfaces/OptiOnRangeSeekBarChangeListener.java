
/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.interfaces;

import com.obs.marveleditor.utils.OptiCustomRangeSeekBar;

public interface OptiOnRangeSeekBarChangeListener {
    void onCreate(OptiCustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeek(OptiCustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStart(OptiCustomRangeSeekBar CustomRangeSeekBar, int index, float value);

    void onSeekStop(OptiCustomRangeSeekBar CustomRangeSeekBar, int index, float value);
}
