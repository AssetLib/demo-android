package com.assetlib.demo

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.assetlib.sdk.*
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HostedArtworkTest {
    @Test fun hostedSignatureNativeDecodeAndIndependentOfflineRestart() = runBlocking {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val raw=runCatching { instrumentation.context.assets.open("assetlib-hosted-config.json").use { it.readBytes().toString(Charsets.UTF_8) } }.getOrNull()
        assumeTrue("Set ASSETLIB_PUBLIC_CONFIG_FILE to a public JSON file to run the read-only hosted test.",raw != null)
        val config=PublicConfig.parse(raw!!)
        val context=instrumentation.targetContext
        val live=AndroidAssets.client(context,config)
        val refreshed=live.refresh()
        assertNull(refreshed.error); assertTrue(refreshed.sequence > 0)
        for(ref in listOf(AppAssets.Travel.coast,AppAssets.Travel.ridge,AppAssets.Tasks.garden)) {
            val resolved=live.resolve(ref)
            assertNotEquals(AssetSource.BUNDLE,resolved.source)
            assertEquals("A historical fallback must not count as the current release",refreshed.sequence,resolved.sequence)
            assertTrue(resolved.mime in listOf("image/png","image/webp"))
            val bitmap=AndroidAssets.bitmap(resolved)!!
            assertEquals(resolved.pixelWidth,bitmap.width)
            assertEquals(resolved.pixelHeight,bitmap.height)
            Log.i("AssetlibHostedSmoke","${ref.key}: sequence=${resolved.sequence}, mime=${resolved.mime}, pixels=${bitmap.width}x${bitmap.height}, sha256=${resolved.sha256}")
            assertTrue(bitmap.width > 0 && bitmap.height > 0); bitmap.recycle()
        }
        // Same default Android namespace, independent client with networking explicitly disabled.
        val disk=FileAssetStorage(File(context.noBackupFilesDir,"assetlib"),File(context.cacheDir,"assetlib"),config)
        val offline=AssetClient(config,disk,AssetTransport { _,_ -> error("Test network is disabled") })
        assertEquals(refreshed.sequence,offline.initialize().sequence)
        assertNotNull(offline.refresh().error)
        val cached=offline.resolve(AppAssets.Travel.coast)
        assertEquals(AssetSource.CACHE,cached.source)
        assertEquals(refreshed.sequence,cached.sequence)
        val bitmap=AndroidAssets.bitmap(cached)!!
        assertTrue(bitmap.width > 0); bitmap.recycle()
        // Retain verified state; never remove an existing app namespace's replay protection.
    }
}
