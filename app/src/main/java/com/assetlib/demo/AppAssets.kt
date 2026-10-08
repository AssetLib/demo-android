// Generated offline from the checked-in catalog. Do not edit.
package com.assetlib.demo

import com.assetlib.sdk.AssetRef
import com.assetlib.sdk.AssetAccessibility

object AppAssets {
    object Tasks {
        val garden = AssetRef("tasks.garden", 600, 400, bundledAccessibility = AssetAccessibility("en", mapOf("en" to "A small garden of green plants")))
    }
    object Travel {
        val coast = AssetRef("travel.coast", 1200, 900, bundledAccessibility = AssetAccessibility("en", mapOf("en" to "A coastal landscape with blue water and cliffs")))
        val ridge = AssetRef("travel.ridge", 1200, 900, bundledAccessibility = AssetAccessibility("en", mapOf("en" to "An alpine landscape with mountains and a lake")))
    }
}
