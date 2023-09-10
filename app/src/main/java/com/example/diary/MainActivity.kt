package com.example.diary

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.diary.data.database.ImageToDeleteDao
import com.example.diary.data.database.ImagesToUploadDao
import com.example.diary.data.database.entity.ImageToDelete
import com.example.diary.navigation.Screen
import com.example.diary.navigation.SetupNavGraph
import com.example.diary.ui.theme.DiaryTheme
import com.example.diary.util.Constants.APP_ID
import com.example.diary.util.retryDeletingImageFromFirebase
import com.example.diary.util.retryUploadingImageToFirebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var imagesToUploadDao: ImagesToUploadDao
    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao
    private var keepSplashOpened = true
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        FirebaseApp.initializeApp(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DiaryTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )
            }
        }
        cleanupCheck(
            scope = lifecycleScope,
            imageToDeleteDao = imageToDeleteDao,
            imagesToUploadDao = imagesToUploadDao)
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imagesToUploadDao: ImagesToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
) {
    scope.launch(Dispatchers.IO){
        val result = imagesToUploadDao.getAllImages()
        result.forEach { imageToUpload ->
            retryUploadingImageToFirebase(
                imageToUpload = imageToUpload,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imagesToUploadDao.cleanupImage(imageToUpload.id)
                    }
                }
            )
        }
        val result2 = imageToDeleteDao.getAllImages()
        result2.forEach { imageToDelete ->
            retryDeletingImageFromFirebase(
                imageToDelete = imageToDelete,
                onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToDeleteDao.cleanupImage(imageId = imageToDelete.id)
                    }
                }
            )
        }
    }
}

private fun getStartDestination():String {
    val user = App.create(APP_ID).currentUser
    return  if (user != null && user.loggedIn) Screen.Home.route else Screen.Authentication.route
}
