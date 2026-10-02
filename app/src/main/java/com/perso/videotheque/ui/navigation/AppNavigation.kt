package com.perso.videotheque.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.perso.videotheque.VideothequeApp
import com.perso.videotheque.ui.edit.EditVideoScreen
import com.perso.videotheque.ui.edit.EditVideoViewModel
import com.perso.videotheque.ui.home.HomeScreen
import com.perso.videotheque.ui.home.HomeViewModel

private const val HOME = "home"
private const val EDIT = "edit?videoId={videoId}"

/**
 * Deux écrans : la liste et l'ajout/modification.
 * Lancée depuis "Partager", l'app ouvre directement l'écran d'ajout puis se ferme
 * après l'enregistrement pour revenir à YouTube.
 */
@Composable
fun AppNavigation(sharedText: String?, onShareFlowClosed: (saved: Boolean) -> Unit) {
    val nav = rememberNavController()
    val fromShare = sharedText != null

    NavHost(
        navController = nav,
        startDestination = if (fromShare) EDIT else HOME,
        enterTransition = { fadeIn(tween(150)) },
        exitTransition = { fadeOut(tween(150)) },
    ) {
        composable(HOME) {
            HomeScreen(
                viewModel = viewModel {
                    HomeViewModel((this[APPLICATION_KEY] as VideothequeApp).container.repository)
                },
                onAddVideo = { nav.navigate("edit") },
                onEditVideo = { id -> nav.navigate("edit?videoId=$id") },
            )
        }
        composable(
            EDIT,
            arguments = listOf(navArgument("videoId") { type = NavType.LongType; defaultValue = -1L }),
        ) { entry ->
            val videoId = entry.arguments?.getLong("videoId")?.takeIf { it > 0 }
            EditVideoScreen(
                viewModel = viewModel {
                    val container = (this[APPLICATION_KEY] as VideothequeApp).container
                    EditVideoViewModel(container.repository, container.metadataFetcher, videoId, sharedText)
                },
                onClose = { saved -> if (fromShare) onShareFlowClosed(saved) else nav.popBackStack() },
            )
        }
    }
}
