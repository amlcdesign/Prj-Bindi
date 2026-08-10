package com.amlcdesign.roadpulsecollector.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

private enum class MainTab {
    HOME,
    HISTORY,
    MAP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadPulseApp() {

    var selectedTab by remember {
        mutableStateOf(MainTab.HOME)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet {

                Text("RoadPulse")

                Text("Profile")
                Text("Vehicles")
                Text("Rewards")
                Text("Data & Sync")
                Text("Settings")
                Text("My Contributions")
                Text("Road Events")
                Text("Help & Support")
                Text("About RoadPulse")
            }
        }

    ) {

        Scaffold(

            topBar = {

                TopAppBar(

                    title = {
                        Text("RoadPulse")
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Text("☰")
                        }
                    }
                )
            },

            bottomBar = {

                NavigationBar {

                    NavigationBarItem(

                        selected =
                            selectedTab == MainTab.HOME,

                        onClick = {
                            selectedTab = MainTab.HOME
                        },

                        icon = {
                            Text("⌂")
                        },

                        label = {
                            Text("Home")
                        }
                    )

                    NavigationBarItem(

                        selected =
                            selectedTab == MainTab.HISTORY,

                        onClick = {
                            selectedTab = MainTab.HISTORY
                        },

                        icon = {
                            Text("▤")
                        },

                        label = {
                            Text("History")
                        }
                    )

                    NavigationBarItem(

                        selected =
                            selectedTab == MainTab.MAP,

                        onClick = {
                            selectedTab = MainTab.MAP
                        },

                        icon = {
                            Text("⌖")
                        },

                        label = {
                            Text("Map")
                        }
                    )
                }
            }

        ) { paddingValues ->

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                when (selectedTab) {

                    MainTab.HOME -> {
                        HomeScreen()
                    }

                    MainTab.HISTORY -> {
                        HistoryPlaceholderScreen()
                    }

                    MainTab.MAP -> {
                        MapPlaceholderScreen()
                    }
                }
            }
        }
    }
}


@Composable
private fun HistoryPlaceholderScreen() {

    Text(
        text = "History\n\nRide history will appear here."
    )
}


@Composable
private fun MapPlaceholderScreen() {

    Text(
        text = "Map\n\nRoad map and events will appear here."
    )
}