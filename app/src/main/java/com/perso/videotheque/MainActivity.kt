package com.perso.videotheque

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.perso.videotheque.ui.navigation.AppNavigation
import com.perso.videotheque.ui.theme.VideothequeTheme

open class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barres système transparentes avec icônes sombres : l'app est toujours en thème clair.
        val bars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)

        // Lien reçu via le menu "Partager" d'Android (ex. depuis YouTube).
        val sharedText = intent
            ?.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            VideothequeTheme {
                AppNavigation(
                    sharedText = sharedText,
                    onShareFlowClosed = { saved ->
                        if (saved) Toast.makeText(this, "Vidéo enregistrée", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                )
            }
        }
    }
}

/**
 * Point d'entrée du menu "Partager" : même écran, mais dans sa propre tâche
 * pour revenir directement à l'app d'origine (YouTube) après l'enregistrement.
 */
class ShareActivity : MainActivity()
