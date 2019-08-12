/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.videoTrimmer.interfaces;

import android.net.Uri;

public interface OptiOnTrimVideoListener {

    void onTrimStarted(int startPosition, int endPosition);

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}
