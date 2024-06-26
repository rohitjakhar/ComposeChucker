package com.rohitjakhar.composechucker.api

import android.content.Context

/**
 * No-op implementation.
 */
@Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
public class RetentionManager @JvmOverloads constructor(
    context: Context,
    retentionPeriod: Any? = null
) {

    @Synchronized
    public fun doMaintenance() {
        // Empty method for the library-no-op artifact
    }

    public enum class Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }
}
