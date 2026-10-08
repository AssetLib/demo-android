package com.assetlib.demo

import com.assetlib.sdk.PublicConfig
import org.junit.Assert.*
import org.junit.Test

class CatalogTest {
    @Test fun generatedContractsMatchSeededWorkspace() {
        assertEquals("travel.coast",AppAssets.Travel.coast.key)
        assertEquals("travel.ridge",AppAssets.Travel.ridge.key)
        assertEquals("tasks.garden",AppAssets.Tasks.garden.key)
        assertEquals(1200,AppAssets.Travel.coast.width)
        assertEquals(900,AppAssets.Travel.ridge.height)
        assertEquals(600,AppAssets.Tasks.garden.width)
        assertEquals(400,AppAssets.Tasks.garden.height)
        assertEquals("A coastal landscape with blue water and cliffs",AppAssets.Travel.coast.bundledAccessibility?.localizedDescription("en-US"))
    }
    @Test fun emptyAndInvalidConnectionStayRejected() {
        for(input in listOf("","{}","null","[]","{\"manifestUrl\":\"https://example.com/api/auth\"}")) {
            assertTrue(runCatching { PublicConfig.parse(input) }.isFailure)
        }
    }
}
