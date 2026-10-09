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
        assertEquals("An illustrated seaside house with trees and a sailboat",AppAssets.Travel.coast.bundledAccessibility?.localizedDescription("en-US"))
    }
    @Test fun emptyAndInvalidConnectionStayRejected() {
        for(input in listOf("","{}","null","[]","{\"manifestUrl\":\"https://example.com/api/auth\"}")) {
            assertTrue(runCatching { PublicConfig.parse(input) }.isFailure)
        }
    }
    @Test fun savedConfigurationsOnEitherConsoleHostStillParse() {
        // The SDK's public test key. Configurations saved before the console moved name the legacy host.
        val org="11111111-1111-4111-8111-111111111111"; val app="22222222-2222-4222-8222-222222222222"
        val pem="-----BEGIN PUBLIC KEY-----\\nMCowBQYDK2VwAyEAA6EHv/POEL4dcN0Y50vAmWfk1jCbpQ1fHdyGZBJVMbg=\\n-----END PUBLIC KEY-----\\n"
        for(host in listOf("assetlib-console.vercel.app","console.assetlib.dev")) {
            for(path in listOf("/api/delivery/$org/$app/manifest","/api/delivery/$org/$app/environments/production/manifest")) {
                val saved="{\"schemaVersion\":1,\"orgId\":\"$org\",\"appId\":\"$app\",\"environment\":\"production\",\"manifestUrl\":\"https://$host$path\",\"pinnedPublicKey\":\"$pem\",\"keyId\":\"f0ff50dacf109dea\"}"
                val config=PublicConfig.parse(saved)
                assertEquals("https://$host$path",config.manifestUrl)
                assertEquals(config,PublicConfig.parse(config.toJson()))
            }
        }
    }
}
