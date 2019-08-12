/*
 *
 *  Created by Optisol on Aug 2019.
 *  Copyright © 2019 Optisol Business Solutions pvt ltd. All rights reserved.
 *
 */

package com.obs.marveleditor.videoTrimmer.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provide operations for
 * UiThread tasks.
 */
public final class OptiUiThreadExecutor {

    private static final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Runnable callback = msg.getCallback();
            if (callback != null) {
                callback.run();
                decrementToken((Token) msg.obj);
            } else {
                super.handleMessage(msg);
            }
        }
    };

    private static final Map<String, Token> TOKENS = new HashMap<>();

    private OptiUiThreadExecutor() {
        // should not be instantiated
    }

    /**
     * Store a new task in the map for providing cancellation. This method is
     * used by AndroidAnnotations and not intended to be called by clients.
     *
     * @param id    the identifier of the task
     * @param task  the task itself
     * @param delay the delay or zero to run immediately
     */
    public static void runTask(String id, Runnable task, long delay) {
        if ("".equals(id)) {
            HANDLER.postDelayed(task, delay);
            return;
        }
        long time = SystemClock.uptimeMillis() + delay;
        HANDLER.postAtTime(task, nextToken(id), time);
    }

    private static Token nextToken(String id) {
        synchronized (TOKENS) {
            Token token = TOKENS.get(id);
            if (token == null) {
                token = new Token(id);
                TOKENS.put(id, token);
            }
            token.runnablesCount++;
            return token;
        }
    }

    private static void decrementToken(Token token) {
        synchronized (TOKENS) {
            if (--token.runnablesCount == 0) {
                String id = token.id;
                Token old = TOKENS.remove(id);
                if (old != token) {
                    // a runnable finished after cancelling, we just removed a
                    // wrong token, lets put it back
                    TOKENS.put(id, old);
                }
            }
        }
    }

    /**
     * Cancel all tasks having the specified <code>id</code>.
     *
     * @param id the cancellation identifier
     */
    public static void cancelAll(String id) {
        Token token;
        synchronized (TOKENS) {
            token = TOKENS.remove(id);
        }
        if (token == null) {
            // nothing to cancel
            return;
        }
        HANDLER.removeCallbacksAndMessages(token);
    }

    private static final class Token {
        int runnablesCount = 0;
        final String id;

        private Token(String id) {
            this.id = id;
        }
    }

}
