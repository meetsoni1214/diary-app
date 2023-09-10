package com.example.diary.navigation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diary.model.GalleryImage
import com.example.diary.model.Mood
import com.example.diary.presentation.component.DisplayAlertDialog
import com.example.diary.presentation.screens.auth.AuthenticationScreen
import com.example.diary.presentation.screens.auth.AuthenticationViewModel
import com.example.diary.presentation.screens.home.HomeScreen
import com.example.diary.presentation.screens.home.HomeViewModel
import com.example.diary.presentation.screens.write.WriteScreen
import com.example.diary.presentation.screens.write.WriteViewModel
import com.example.diary.util.Constants.APP_ID
import com.example.diary.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diary.model.RequestState
import com.example.diary.model.rememberGalleryState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun SetupNavGraph(
    startDestination: String,
    onDataLoaded: () -> Unit,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            onDataLoaded = onDataLoaded,
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            navigateToWriteArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            }
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {

    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticatedState by viewModel.authenticated
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }
        AuthenticationScreen(
            authenticatedState = authenticatedState,
            loadingState = loadingState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            messageBarState = messageBarState,
            oneTapState = oneTapState,
            onSuccessfulFirebaseSignIn = {tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            onDialogDismissed = {message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Home.route) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val viewModel: HomeViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val context = LocalContext.current
        var signOutDialogOpened by remember {
            mutableStateOf(false)
        }
        var fromDate by remember {
            mutableStateOf(ZonedDateTime.now())
        }
        var toDate by remember {
            mutableStateOf(ZonedDateTime.now())
        }
        var deleteAllDialogOpened by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }
        HomeScreen(
            diaries = diaries,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onDeleteAllClicked = {
               deleteAllDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            drawerState = drawerState,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWriteArgs = navigateToWriteArgs,
            dateIsSelected = viewModel.dateIsSelected,
            onDateReset = {
                viewModel.getDiaries()
            },
            onFromDateSelected = {
                fromDate = it
            },
            onToDateSelected = {
                toDate = it
                viewModel.getDiaries(fromDate = fromDate, toDate = toDate)
            }
        )
        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google Account? ",
            dialogOpened = signOutDialogOpened,
            onYesClicked = {
                scope.launch (Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }
                }
            },
            onCloseDialog = { signOutDialogOpened = false }
        )
        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all you diaries? ",
            dialogOpened = deleteAllDialogOpened,
            onYesClicked = {
                           viewModel.deleteAllDiaries(
                               onSuccess = {
                                           Toast.makeText(
                                               context,
                                               "All Diaries Deleted.",
                                               Toast.LENGTH_SHORT
                                           ).show()
                                   scope.launch {
                                       drawerState.close()
                                   }
                               },
                               onError = {
                                   Toast.makeText(
                                       context,
                                       if (it.message == "No Internet Connection!") "We need an Internet Connection for this operation" else it.message,
                                       Toast.LENGTH_SHORT
                                   ).show()
                                   scope.launch {
                                       drawerState.close()
                                   }
                               }
                           )
            },
            onCloseDialog = { deleteAllDialogOpened = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed:() -> Unit,
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
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
        val pageNumber by remember { derivedStateOf { pagerState.currentPage}}
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