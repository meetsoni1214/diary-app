package com.example.write.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.util.Constants
import com.example.util.Screen
import com.example.util.model.Mood
import com.example.write.WriteScreen
import com.example.write.WriteViewModel
import com.google.accompanist.pager.ExperimentalPagerApi


@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed:() -> Unit,
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = Constants.WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = hiltViewModel()
        val galleryState = viewModel.galleryState
        val uiState = viewModel.uiState
        val context = LocalContext.current
        val pagerState = com.google.accompanist.pager.rememberPagerState()
        val pageNumber by remember { derivedStateOf { pagerState.currentPage} }
        LaunchedEffect(key1 = Unit) {
            Log.d("SelectedDiary", "${uiState.selectedDiaryId}")
        }
        WriteScreen(
            onTitleChanged = {viewModel.setTitle(title = it)},
            onDescriptionChanged = {viewModel.setDescription(description = it)},
            uiState = uiState,
            moodName = { Mood.values()[pageNumber].name},
            pagerState = pagerState,
            onBackPressed = onBackPressed,
            galleryState = galleryState,
            onImageSelect = {uri ->
                val type = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
                Log.d("WriteViewModel", "URI: $uri")
                viewModel.addImage(
                    image = uri,
                    imageType = type
                )
            },
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Deleted Successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    })},
            onDateTimeUpdated = {viewModel.updateDateTime(it)},
            onImageDeleteClicked = {
                galleryState.removeImage(it)
            },
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = { onBackPressed()},
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
        )
    }
}