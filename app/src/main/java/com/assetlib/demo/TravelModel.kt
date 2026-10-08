package com.assetlib.demo

import android.app.Application
import android.graphics.Bitmap
import android.util.AtomicFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.assetlib.sdk.*
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Artwork(val bitmap: Bitmap? = null, val source: AssetSource = AssetSource.BUNDLE, val sequence: Long? = null)
data class TravelState(val connected: Boolean = false, val busy: Boolean = false, val sequence: Long = 0, val message: String = "Explore with the artwork bundled in this app.", val error: String? = null, val coast: Artwork = Artwork(), val ridge: Artwork = Artwork(), val garden: Artwork = Artwork())

class TravelModel(application: Application) : AndroidViewModel(application) {
    private val mutable = MutableStateFlow(TravelState())
    val state = mutable.asStateFlow()
    private val configFile = AtomicFile(File(application.noBackupFilesDir,"assetlib-public-config.json"))
    private var client: AssetClient? = null
    private var operation: Job? = null
    @Volatile private var generation = 0
    private val configGuard = Any()
    init {
        operation=viewModelScope.launch {
            val saved=withContext(Dispatchers.IO) { runCatching { configFile.readFully().toString(Charsets.UTF_8) }.getOrNull() }
            if(generation != 0) return@launch
            if(saved != null) runCatching { PublicConfig.parse(saved) }.onSuccess { config ->
                client=AndroidAssets.client(getApplication(),config)
                mutable.value=mutable.value.copy(connected=true,busy=true,message="Loading verified artwork on this device.")
                client!!.initialize(); loadArtwork(client!!,generation)
            }.onFailure { mutable.value=mutable.value.copy(error="Saved configuration is invalid. Disconnect, then paste a fresh public configuration.",connected=true) }
        }
    }
    fun connect(text: String) {
        val config=runCatching { PublicConfig.parse(text) }.getOrElse { mutable.value=mutable.value.copy(error=it.message ?: "Paste the public JSON configuration from the console."); return }
        operation?.cancel(); client=null; val token=++generation
        operation=viewModelScope.launch {
            mutable.value=TravelState(busy=true,message="Verifying the published release.")
            val next=AndroidAssets.client(getApplication(),config)
            val result=next.refresh()
            if(token != generation) return@launch
            if(result.error != null) { mutable.value=mutable.value.copy(busy=false,error=result.error); return@launch }
            try {
                withContext(Dispatchers.IO) {
                    synchronized(configGuard) {
                        if(token != generation) throw CancellationException()
                        val stream=configFile.startWrite()
                        try { stream.write(config.toJson().toByteArray()); configFile.finishWrite(stream) }
                        catch(e: Exception) { configFile.failWrite(stream); throw e }
                    }
                }
                if(token != generation) return@launch
                client=next; mutable.value=mutable.value.copy(connected=true)
                loadArtwork(next,token)
            } catch(e: Exception) { if(e is CancellationException) throw e; if(token == generation) mutable.value=TravelState(error=e.message ?: "Could not save configuration.") }
        }
    }
    fun refresh() {
        val current=client ?: return; if(mutable.value.busy) return
        val token=generation
        operation=viewModelScope.launch {
            mutable.value=mutable.value.copy(busy=true,error=null,message="Checking for a signed release.")
            val result=current.refresh(); loadArtwork(current,token,result.error)
        }
    }
    private suspend fun loadArtwork(c: AssetClient,token: Int,error: String? = null) {
        suspend fun image(ref: AssetRef): Artwork { val a=c.resolve(ref); return Artwork(AndroidAssets.bitmap(a),a.source,a.sequence) }
        val coast=image(AppAssets.Travel.coast)
        if(token != generation) return
        mutable.value=mutable.value.copy(coast=coast,sequence=c.status.value.sequence)
        val ridge=image(AppAssets.Travel.ridge)
        if(token != generation) return
        mutable.value=mutable.value.copy(ridge=ridge)
        val garden=image(AppAssets.Tasks.garden)
        if(token != generation) return
        mutable.value=mutable.value.copy(connected=true,busy=false,garden=garden,message="Your app keeps its layout. Artwork follows the published release.",error=error ?: c.status.value.lastError)
    }
    fun disconnect() {
        operation?.cancel(); ++generation; client=null
        synchronized(configGuard) { configFile.delete() } // Public connection only. Durable release protection is retained.
        mutable.value=TravelState(message="Disconnected. Showing the bundled artwork.")
    }
}
