package de.thomba.andropicsort

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import de.thomba.andropicsort.ui.MainScreen
import de.thomba.andropicsort.ui.MainViewModel
import de.thomba.andropicsort.ui.theme.AndroidPicSortTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidPicSortTheme {
                Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
                    val sourceLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocumentTree(),
                    ) { uri ->
                        uri?.let {
                            persistPermissions(it)
                            viewModel.onSourceSelected(it)
                        }
                    }

                    val targetLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocumentTree(),
                    ) { uri ->
                        uri?.let {
                            persistPermissions(it)
                            viewModel.onTargetSelected(it)
                        }
                    }

                    MainScreen(
                        viewModel = viewModel,
                        onPickSource = { sourceLauncher.launch(null) },
                        onPickTarget = { targetLauncher.launch(null) },
                    )
                }
            }
        }
    }

    private fun persistPermissions(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: SecurityException) {
            // Some providers may reject persistable permission; app still works for current session.
        }
    }
}

