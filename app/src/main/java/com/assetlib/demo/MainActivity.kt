package com.assetlib.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assetlib.sdk.AssetSource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent { MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xff314937),secondary=Color(0xff984626),background=Color(0xfff6f4ed),surface=Color(0xfff6f4ed),surfaceVariant=Color(0xffe8ebdf),onSurface=Color(0xff222b24))) { TravelApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TravelApp(model: TravelModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    var connection by rememberSaveable { mutableStateOf(false) }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    var savedCoast by rememberSaveable { mutableStateOf(false) }
    var savedRidge by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar={
        TopAppBar(title={ Column { Text("roam.",fontFamily=FontFamily.Serif,fontSize=32.sp); Text("A little further from the everyday",fontSize=11.sp) } },actions={ TextButton(onClick={connection=true}) { Text(if(state.connected) "Connected" else "Connect") } })
    },bottomBar={
        NavigationBar(containerColor=MaterialTheme.colorScheme.surface) {
            NavigationBarItem(selected=!savedOnly,onClick={savedOnly=false},icon={Text("◌",fontSize=23.sp)},label={Text("Explore")})
            NavigationBarItem(selected=savedOnly,onClick={savedOnly=true},icon={Text(if(savedCoast||savedRidge) "♥" else "♡",fontSize=23.sp)},label={Text("Saved")})
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal=22.dp),verticalArrangement=Arrangement.spacedBy(22.dp)) {
            Spacer(Modifier.height(2.dp))
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(if(savedOnly) "YOUR NEXT ESCAPE" else "WEEKENDS, WELL SPENT",fontSize=11.sp,letterSpacing=2.sp,color=MaterialTheme.colorScheme.secondary)
                Text(if(savedOnly) "Keep a place\nin mind." else "Somewhere\nworth slowing down.",fontFamily=FontFamily.Serif,fontSize=38.sp,lineHeight=41.sp)
                Text(if(savedOnly) "The places you’ve saved for another day." else "A coastal morning. A mountain path. Leave a little room for both.",color=Color(0xff626b60),lineHeight=22.sp)
            }
            if(!savedOnly||savedCoast) DestinationCard("A coastal weekend","Quiet coves · two unhurried days",state.coast,R.drawable.coast_hero,savedCoast) { savedCoast=!savedCoast }
            if(!savedOnly||savedRidge) DestinationCard("An alpine escape","Fresh air · a different point of view",state.ridge,R.drawable.ridge_card,savedRidge) { savedRidge=!savedRidge }
            if(savedOnly&&!savedCoast&&!savedRidge) OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(24.dp)) { Text("Your list starts with a place.",fontWeight=FontWeight.SemiBold); Text("Tap Save on a destination to keep it here.",Modifier.padding(top=8.dp)); TextButton(onClick={savedOnly=false}) {Text("Explore places") } } }
            if(!savedOnly) Card(colors=CardDefaults.cardColors(containerColor=Color(0xffe8ebdf))) {
                Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                    ArtworkImage(state.garden,R.drawable.task_garden,"A plant illustration for your weekend ritual",Modifier.width(100.dp).aspectRatio(1.5f).clip(RoundedCornerShape(16.dp)))
                    Column { Text("Make space for small things.",fontFamily=FontFamily.Serif,fontSize=21.sp); Text("One walk. One quiet morning.",fontSize=13.sp,modifier=Modifier.padding(top=6.dp)); SourceLabel(state.garden) }
                }
            }
            HorizontalDivider()
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("ARTWORK BY ASSETLIB",fontSize=11.sp,letterSpacing=1.8.sp,fontWeight=FontWeight.Bold)
                Text(if(state.connected) "Connected · release ${state.sequence}" else "Bundled artwork · ready to explore",fontWeight=FontWeight.Medium)
                Text(state.message,fontSize=13.sp,lineHeight=19.sp,color=Color(0xff626b60))
                state.error?.let { Text(it,color=MaterialTheme.colorScheme.error,fontSize=13.sp) }
                if(state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    if(state.connected) OutlinedButton(onClick=model::refresh,enabled=!state.busy) {Text("Check for updates")}
                    TextButton(onClick={connection=true}) {Text(if(state.connected) "Connection" else "Try your own artwork")}
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
    if(connection) ConnectionSheet(state,model,onClose={connection=false})
}

@Composable private fun DestinationCard(title: String,subtitle: String,art: Artwork,fallback: Int,saved: Boolean,onSave: () -> Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        ArtworkImage(art,fallback,title,Modifier.fillMaxWidth().aspectRatio(4f/3f).clip(RoundedCornerShape(24.dp)))
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title,fontFamily=FontFamily.Serif,fontSize=25.sp); Text(subtitle,fontSize=13.sp,color=Color(0xff626b60),modifier=Modifier.padding(top=4.dp)); SourceLabel(art) }
            OutlinedButton(onClick=onSave,contentPadding=PaddingValues(horizontal=14.dp)) { Text(if(saved) "Saved ♥" else "Save ♡") }
        }
    }
}
@Composable private fun ArtworkImage(art: Artwork,fallback: Int,description: String,modifier: Modifier) {
    if(art.bitmap != null) Image(bitmap=art.bitmap.asImageBitmap(),contentDescription=description,modifier=modifier,contentScale=ContentScale.Crop)
    else Image(painter=painterResource(fallback),contentDescription=description,modifier=modifier,contentScale=ContentScale.Crop)
}
@Composable private fun SourceLabel(art: Artwork) {
    val name=when(art.source) { AssetSource.BUNDLE->"Bundled"; AssetSource.CACHE->"Verified cache"; AssetSource.REMOTE->"Downloaded & verified" }
    Text(if(art.sequence==null) name else "$name · r${art.sequence}",fontSize=11.sp,color=Color(0xff626b60),modifier=Modifier.padding(top=7.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun ConnectionSheet(state: TravelState,model: TravelModel,onClose: () -> Unit) {
    val context=LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    var tooLong by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest=onClose,containerColor=MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=32.dp).imePadding(),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("Connect your artwork.",fontFamily=FontFamily.Serif,fontSize=30.sp)
            Text("Create a demo workspace in the console. Copy its public SDK configuration, then paste it here. No admin key or password belongs in this field.",fontSize=14.sp,lineHeight=21.sp)
            TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://assetlib-console.vercel.app")))}) { Text("Open Assetlib console ↗") }
            OutlinedTextField(value=text,onValueChange={tooLong=it.length>8192; if(!tooLong) text=it},label={Text("Public SDK configuration")},placeholder={Text("Paste the complete JSON configuration")},minLines=4,maxLines=7,modifier=Modifier.fillMaxWidth(),enabled=!state.busy,isError=tooLong)
            if(tooLong) Text("Configuration is too long. Use the public JSON from the console.",color=MaterialTheme.colorScheme.error)
            state.error?.let {Text(it,color=MaterialTheme.colorScheme.error,fontSize=13.sp)}
            if(state.connected) Text("Connected · signed release ${state.sequence}",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold)
            if(state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Button(onClick={model.connect(text)},enabled=!state.busy&&text.isNotBlank()&&!tooLong,modifier=Modifier.fillMaxWidth()) { Text("Connect and check release") }
            if(state.connected) OutlinedButton(onClick=model::disconnect,modifier=Modifier.fillMaxWidth()) {Text("Disconnect")}
            Text("The configuration is public and stays on this device. The SDK contacts its HTTPS delivery origin, which can see network request information. Published artwork is public. Disconnect removes the connection; verified cache and rollback protection remain until app data is cleared.",fontSize=12.sp,lineHeight=18.sp,color=Color(0xff626b60))
            TextButton(onClick=onClose,modifier=Modifier.align(Alignment.End)) {Text("Back to exploring")}
        }
    }
}
