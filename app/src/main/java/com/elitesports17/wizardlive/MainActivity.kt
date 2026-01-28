package com.elitesports17.wizardlive

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.elitesports17.wizardlive.ui.navigation.AppNavHost
import com.elitesports17.wizardlive.ui.theme.WizardLiveTheme
import com.elitesports17.wizardlive.ui.util.UserSession

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Theme_WizardLive)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WizardLiveTheme {

                val context = LocalContext.current

                // 🔥 CLAVE ABSOLUTA: cachear token en memoria
                LaunchedEffect(Unit) {
                    val token = UserSession.getToken(context)
                    UserSession.setCachedToken(token)

                    android.util.Log.d(
                        "SESSION",
                        "🔐 Token cargado en memoria = ${token != null}"
                    )
                }

                AppNavHost()
            }
        }
    }
}

/* ================= PREVIEW ================= */

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WizardLiveTheme {
        Greeting("Android")
    }
}
