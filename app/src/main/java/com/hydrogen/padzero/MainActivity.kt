
package com.hydrogen.padzero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydrogen.padzero.ui.HydrogenApp
import com.hydrogen.padzero.ui.HydrogenTheme
import com.hydrogen.padzero.ui.HydrogenViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HydrogenTheme {
                Surface(modifier = Modifier) {
                    val vm: HydrogenViewModel = viewModel(factory = HydrogenViewModel.factory(application))
                    HydrogenApp(viewModel = vm)
                }
            }
        }
    }
}
