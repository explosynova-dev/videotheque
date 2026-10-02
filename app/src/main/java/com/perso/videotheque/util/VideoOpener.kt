package com.perso.videotheque.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.perso.videotheque.domain.VideoLinks

/** Ouvre la vidéo dans l'app YouTube si elle est installée, sinon dans le navigateur. */
fun openVideo(context: Context, url: String) {
    val uri = Uri.parse(url)
    val intents = buildList {
        if (VideoLinks.youTubeId(url) != null) {
            add(Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.youtube"))
        }
        add(Intent(Intent.ACTION_VIEW, uri))
    }
    for (intent in intents) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
            // On essaie l'option suivante.
        }
    }
    Toast.makeText(context, "Aucune application pour ouvrir ce lien", Toast.LENGTH_SHORT).show()
}
