package com.example.write


import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.model.Diary
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.util.model.Mood
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun WriteScreen(
    uiState: UiState,
    moodName: () -> String,
    onBackPressed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onTitleChanged:(String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    galleryState: GalleryState,
    pagerState: PagerState,
    onSaveClicked: (Diary) -> Unit,
    onImageSelect: (Uri) -> Unit,
    onImageDeleteClicked: (GalleryImage) -> Unit,
    onDateTimeUpdated: (ZonedDateTime) -> Unit,
) {
    var selectedGalleryImage by remember {
        mutableStateOf<GalleryImage?>(null)
    }
    // update the Mood when selecting an existing Diary
    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                onBackPressed = onBackPressed,
                selectedDiary = uiState.selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                moodName = moodName,
                onDateTimeUpdated = onDateTimeUpdated
            )
        },
        content = { paddingValues ->
            WriteContent(
                uiState = uiState,
                pagerState = pagerState,
                title = uiState.title,
                galleryState = galleryState,
                onTitleChanged = onTitleChanged,
                desc = uiState.description,
                onDescChanged = onDescriptionChanged,
                paddingValues = paddingValues,
                onSaveClicked = onSaveClicked,
                onImageSelect = onImageSelect,
                onImageClicked = {
                    selectedGalleryImage = it
                }
            )
            AnimatedVisibility(visible = selectedGalleryImage != null) {
                Dialog(onDismissRequest = { selectedGalleryImage = null }) {
                    if (selectedGalleryImage != null) {
                            ZoomableImage(
                                onNextClicked = {
                                        val index = galleryState.images.indexOf(selectedGalleryImage)
                                        Log.d("Check", "$index")
                                        if (index < galleryState.images.size - 1) {
                                            selectedGalleryImage = galleryState.images[index + 1]
                                            Log.d("Check", "${index + 1}")
                                    }
                                },
                                onPreviousClicked = {
                                        val index = galleryState.images.indexOf(selectedGalleryImage)
                                        Log.d("Check", "$index")
                                        if (index > 0) {
                                            selectedGalleryImage = galleryState.images[index - 1]
                                            Log.d("Check", "${index - 1}")
                                        }
                                },
                                selectedGalleryImage = selectedGalleryImage!!,
                                onDeleteClicked = {
                                    if (selectedGalleryImage != null) {
                                        onImageDeleteClicked(selectedGalleryImage!!)
                                        selectedGalleryImage = null
                                    }
                                },
                                onCloseClicked = { selectedGalleryImage = null })
                        }
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures {_, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Icon")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                Text(text = "Delete")
            }
        }
        Row (
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box (
                contentAlignment = Alignment.Center
            ){
                Surface(
                    modifier = Modifier
                        .clickable {
                            onPreviousClicked()
                        }
                        .size(Icons.Default.Close.defaultHeight),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {}
                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Next Icon")
            }
            Box (
                contentAlignment = Alignment.Center
            ){
                Surface(
                    modifier = Modifier
                        .clickable { onNextClicked() }
                        .size(Icons.Default.Close.defaultHeight),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {}
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Previous Icon")
            }
        }
    }
}