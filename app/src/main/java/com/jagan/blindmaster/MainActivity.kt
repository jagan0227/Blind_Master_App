package com.jagan.blindmaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jagan.blindmaster.ml.SsdMobilenetV11Metadata1
import com.jagan.blindmaster.ui.theme.BlindMasterTheme
import com.jagan.blindmaster.ui.theme.BlueDark
import com.jagan.blindmaster.ui.theme.LightBlue
import com.jagan.blindmaster.ui.theme.LightBlueDark
import com.jagan.blindmaster.ui.theme.LightDarkBlue
import com.jagan.blindmaster.ui.theme.LightWhite
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.Locale


class MainActivity : ComponentActivity() {

    private var textToSpeech: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BlindMasterTheme {
                // A surface container using the 'background' color from the theme
                val context = LocalContext.current
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Restart your app", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please Give Camera Permission", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlueDark
                ) {

                    var muteStatue by rememberSaveable { mutableStateOf(true) }

                    val bitMapImageSet = remember {
                        mutableStateListOf<Bitmap?>(
                            null,
                            null,
                            null,
                            null,
                        )
                    }
                    val lenOfFrame = remember { mutableStateOf(0) }
                    val bitMapValue = remember { mutableStateOf<Bitmap?>(null) }
                    val listOfObject = remember {
                        mutableStateListOf(
                            "Hello User.....",
                            "Welcome to Blind Master.....",
                            "Starting Engine.....",
                            "Engine Started.....",
                            "Detecting the object....."
                        )
                    }

                    val listState = rememberLazyListState()
                    val labels = FileUtil.loadLabels(this, "labels.txt")
                    val scope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .height(50.dp)
                                .background(LightDarkBlue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Icon(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(start = 15.dp, top = 4.dp)
                                    .size(30.dp)
                            )

                            Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 30.sp,
                                            fontFamily = FontFamily(
                                                Font(R.font.nothingfont)
                                            )
                                        )
                                    ) {
                                        append("B")
                                    }
                                    append("lind ")

                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 30.sp, fontFamily = FontFamily(
                                                Font(R.font.nothingfont)
                                            )
                                        )
                                    ) {
                                        append("M")
                                    }
                                    append("aster")
                                },
                                color = LightWhite,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .weight(0.9f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .height(340.dp)
                                .width(255.dp)
                                .background(LightDarkBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            bitMapImageSet[1]?.asImageBitmap()
                                ?.let {
                                    Image(
                                        bitmap = it,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                        }

                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onFrameCaptured = {
                                    // Process the bitmap here
                                    bitMapValue.value = it
                                    bitMapImageSet[lenOfFrame.value++] = it
                                    if (lenOfFrame.value >= 4) lenOfFrame.value = 0

                                    val model = SsdMobilenetV11Metadata1.newInstance(context)
                                    val imageProcessor =
                                        ImageProcessor.Builder().add(
                                            ResizeOp(
                                                300,
                                                300,
                                                ResizeOp.ResizeMethod.BILINEAR
                                            )
                                        ).build()

                                    var image = TensorImage.fromBitmap(it)
                                    image = imageProcessor.process(image)

                                    val outputs = model.process(image)
                                    //val locations = outputs.locationsAsTensorBuffer.floatArray
                                    val classes = outputs.classesAsTensorBuffer.floatArray
                                    val scores = outputs.scoresAsTensorBuffer.floatArray
                                    //val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer

                                    scores.forEachIndexed { index, fl ->
                                        val l = labels[classes[index].toInt()].toString()
                                        if (fl > 0.50f) {

                                            val idx = listOfObject.lastIndexOf(l)
                                            val mod = idx < listOfObject.size - 5
                                            if (idx == -1 || mod) {
                                                listOfObject.add(l)

                                                if(muteStatue) {
                                                    val txt = "object $l is detected"
                                                    textToSpeech(txt)
                                                }

                                                scope.launch {
                                                    listState.scrollToItem(listOfObject.size)
                                                }
                                            }

                                            if (listOfObject.size > 100) listOfObject.clear()

                                        }
                                    }
                                }
                            )
                        } else {
                            Button(
                                onClick = { requestPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Give Permission", color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = 15.dp, bottom = 15.dp, start = 15.dp, end = 15.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .fillMaxWidth()
                                .height(100.dp)
                                .horizontalScroll(rememberScrollState())
                                .background(LightDarkBlue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            bitMapImageSet[0]?.asImageBitmap()
                                ?.let {
                                    Image(
                                        bitmap = it,
                                        modifier = Modifier.size(80.dp),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            Color.Green,
                                            blendMode = BlendMode.Darken
                                        )
                                    )
                                }
                            bitMapImageSet[1]?.asImageBitmap()
                                ?.let {
                                    Image(
                                        bitmap = it,
                                        modifier = Modifier.size(80.dp),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                            setToSaturation(
                                                0f
                                            )
                                        })
                                    )
                                }
                            bitMapImageSet[2]?.asImageBitmap()
                                ?.let {
                                    Image(
                                        bitmap = it,
                                        modifier = Modifier.size(80.dp),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            Color.Green,
                                            blendMode = BlendMode.Darken
                                        )
                                    )
                                }
                            bitMapImageSet[3]?.asImageBitmap()
                                ?.let {
                                    Image(
                                        bitmap = it,
                                        modifier = Modifier.size(80.dp),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                            setToSaturation(
                                                0f
                                            )
                                        })

                                    )
                                }
                        }

                        Column(
                            Modifier
                                .width(350.dp)
                                .height(300.dp)
                                .padding(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(LightDarkBlue),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = ".......... CONSOLE ..........",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily(
                                    Font(R.font.nothingfont)
                                ),
                                modifier = Modifier.padding(10.dp)
                            )

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White)
                            )

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            )
                            {
                                items(listOfObject.size) {
                                    Text(
                                        text = "ln[$it] : ${listOfObject[it]}",
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "© 2K23 - Jagan",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily(
                                Font(R.font.nothingfont)
                            )
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Box(modifier = Modifier.padding(bottom = 8.dp, end = 20.dp)) {
                        FloatingActionButton(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.BottomEnd),
                            onClick = {
                                muteStatue = !muteStatue
                                textToSpeech(
                                    if (muteStatue) "Sound is turned on" else "Sound is turned off",
                                )
                            },
                            containerColor = LightBlue
                        ) {
                            Icon(
                                painter = painterResource(id = if (muteStatue) R.drawable.unmutevolume else R.drawable.mutevolume),
                                contentDescription = "mute or un mute button",
                                tint = Color.White
                            )
                        }
                    }

                }
            }
        }
    }
    private fun textToSpeech(message: String) {
        val context = this@MainActivity
        textToSpeech = TextToSpeech(
            context
        ) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech?.let { txtToSpeech ->
                    txtToSpeech.language = Locale.US
                    txtToSpeech.setSpeechRate(1.0f)
                    txtToSpeech.speak(
                        message,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        textToSpeech("Welcome to Blind Master.....")
    }

}

