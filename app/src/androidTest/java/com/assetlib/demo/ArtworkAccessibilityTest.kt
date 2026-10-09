package com.assetlib.demo

import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.assetlib.sdk.AssetAccessibility
import com.assetlib.sdk.AssetSource
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkAccessibilityTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private fun nodes(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> = listOf(node) +
        (0 until node.childCount).flatMap { index -> node.getChild(index)?.let(::nodes) ?: emptyList() }
    private fun awaitTree(predicate: (List<AccessibilityNodeInfo>) -> Boolean): List<AccessibilityNodeInfo> {
        val deadline = SystemClock.uptimeMillis() + 5000
        var tree = emptyList<AccessibilityNodeInfo>()
        do {
            tree = instrumentation.uiAutomation.rootInActiveWindow?.let(::nodes) ?: emptyList()
            if (predicate(tree)) return tree
            SystemClock.sleep(50)
        } while (SystemClock.uptimeMillis() < deadline)
        fail("Expected artwork semantics in native tree: " + tree.joinToString { "${it.className}: ${it.contentDescription} / ${it.text}" })
        return tree
    }
    @Test fun descriptionsFollowDisplayedPixelsAndDecorationStaysHidden() {
        val automation = instrumentation.uiAutomation
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags and AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS.inv()
        }
        val bitmap = Bitmap.createBitmap(12, 9, Bitmap.Config.ARGB_8888)
        val metadata = AssetAccessibility("en", mapOf("en" to "Published mountain landscape"))
        val artwork = mutableStateOf(Artwork(bitmap, AssetSource.REMOTE, accessibility = metadata))
        try {
            ActivityScenario.launch(AccessibilityTestActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.setContent {
                        MaterialTheme {
                            Column {
                                ArtworkImage(artwork.value, AppAssets.Travel.coast, R.drawable.coast_hero, Modifier.size(120.dp, 90.dp)) {}
                                ArtworkImage(artwork.value, AppAssets.Tasks.garden, R.drawable.task_garden, Modifier.size(120.dp, 80.dp), decorative = true) {}
                                Text("A coastal weekend")
                                Button(onClick = {}) { Text("Save destination") }
                            }
                        }
                    }
                }
                var tree = awaitTree { nodes -> nodes.any { it.contentDescription?.toString() == "Published mountain landscape" } }
                assertEquals(1, tree.count { it.className.toString() == "android.widget.ImageView" })
                tree = awaitTree { nodes -> nodes.any { it.isClickable && this.nodes(it).any { child -> child.text?.toString() == "Save destination" } } }
                // A later decode failure uses bundled pixels even if stale remote metadata exists.
                scenario.onActivity { artwork.value = Artwork(source = AssetSource.REMOTE, accessibility = metadata) }
                tree = awaitTree { nodes -> nodes.any { it.contentDescription?.toString() == "An illustrated seaside house with trees and a sailboat" } }
                assertFalse(tree.any { it.contentDescription?.toString() == "Published mountain landscape" })
                // The app treats undescribed imagery as decorative alongside its persistent title.
                scenario.onActivity { artwork.value = Artwork(bitmap, AssetSource.REMOTE) }
                tree = awaitTree { nodes -> nodes.any { it.text?.toString() == "A coastal weekend" } && nodes.none { it.className.toString() == "android.widget.ImageView" } }
                assertFalse(tree.any { it.contentDescription?.toString() == "An illustrated seaside house with trees and a sailboat" })
            }
        } finally { bitmap.recycle() }
    }
}
