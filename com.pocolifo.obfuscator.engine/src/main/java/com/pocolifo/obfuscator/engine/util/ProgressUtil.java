package com.pocolifo.obfuscator.engine.util;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ProgressUtil {
    public static ProgressBar bar(String name, long max) {
        if (Logging.enable) {
            return new ProgressBarBuilder()
                    .setStyle(ProgressBarStyle.ASCII)
                    .setTaskName(name)
                    .setInitialMax(max)
                    .setUpdateIntervalMillis(50)
                    .build();
        } else {
            return new EmptyProgressBar();
        }
    }

    public static class EmptyProgressBar extends ProgressBar {
        @SuppressWarnings("deprecation")
        public EmptyProgressBar() {
            super("", 0, 1, false, null, ProgressBarStyle.ASCII, "", 1, false, null, ChronoUnit.SECONDS, 0L, Duration.ZERO);
        }
    }

}
