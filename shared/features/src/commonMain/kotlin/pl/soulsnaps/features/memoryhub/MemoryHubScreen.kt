package pl.soulsnaps.features.memoryhub

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import pl.soulsnaps.features.capturemoment.navigateToCaptureMoment
import pl.soulsnaps.features.memoryhub.gallery.MomentsGalleryRoute
import pl.soulsnaps.features.memoryhub.gallery.momentsGalleryScreen
import pl.soulsnaps.features.memoryhub.gallery.navigateToMomentsGallery
import pl.soulsnaps.features.memoryhub.map.MemoryMapRoute
import pl.soulsnaps.features.memoryhub.map.memoryMapScreen
import pl.soulsnaps.features.memoryhub.map.navigateToMemoryMap
import pl.soulsnaps.features.memoryhub.timeline.TimelineRoute
import pl.soulsnaps.features.memoryhub.timeline.navigateToTimeline
import pl.soulsnaps.features.memoryhub.timeline.timelineScreen
import kotlin.reflect.KClass

@Composable
internal fun MemoryHubRoute(onMemoryClick: (Int) -> Unit) {
    MemoryHubScreen(onMemoryClick)
}

@Composable
private fun MemoryHubScreen(onMemoryDetailsClick: (Int) -> Unit,) {
    val insets = WindowInsets.systemBars.asPaddingValues()
    val navController = rememberNavController()
    val tabs: List<TabItem> = listOf(
        TabItem("Oś czasu", TimelineRoute::class),
        TabItem("Galeria", MomentsGalleryRoute::class),
        TabItem("Mapa", MemoryMapRoute::class)
    )
    var selectedTab by remember { mutableStateOf(TabItem("Oś czasu", TimelineRoute::class)) }
    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color(0xFF7F5AF0),
                modifier = Modifier.padding(top = insets.calculateTopPadding())
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = selectedTab == tab, onClick = {
                        selectedTab = tab
                        val navOptions = navOptions(
                            {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        )
                        when (tab.route) {
                            TimelineRoute::class -> navController.navigateToTimeline(
                                navOptions
                            )

                            MomentsGalleryRoute::class -> navController.navigateToMomentsGallery(
                                navOptions
                            )

                            MemoryMapRoute::class -> navController.navigateToMemoryMap(
                                navOptions
                            )
                        }
                    }, text = { Text(tabs[index].title, color = Color.White) })
                }
            }
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = TimelineRoute,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                timelineScreen(
                    onMemoryDetailsClick = onMemoryDetailsClick,
                )
                memoryMapScreen(onMemoryDetailsClick)
                momentsGalleryScreen(
                    onMemoryClick =  {
                        onMemoryDetailsClick(1)
                    },
                    onAddMemoryClick = navController::navigateToCaptureMoment
                )
                //captureMomentScreen()
            }
        }
    )
}

private data class TabItem(
    val title: String, val route: KClass<*>
)
