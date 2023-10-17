package com.example.home.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.HomeScreen
import com.example.home.HomeViewModel
import com.example.ui.component.DisplayAlertDialog
import com.example.util.Constants.APP_ID
import com.example.util.Screen
import com.example.util.model.RequestState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
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
            mutableStateOf(java.time.ZonedDateTime.now())
        }
        var toDate by remember {
            mutableStateOf(java.time.ZonedDateTime.now())
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
                scope.launch (kotlinx.coroutines.Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
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
                        android.widget.Toast.makeText(
                            context,
                            "All Diaries Deleted.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        android.widget.Toast.makeText(
                            context,
                            if (it.message == "No Internet Connection!") "We need an Internet Connection for this operation" else it.message,
                            android.widget.Toast.LENGTH_SHORT
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
