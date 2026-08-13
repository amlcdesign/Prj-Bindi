package com.amlcdesign.roadpulsecollector.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.viewmodel.compose.viewModel

import com.amlcdesign.roadpulsecollector.R
import com.amlcdesign.roadpulsecollector.storage.HistoryManager

import androidx.compose.ui.platform.LocalContext

private enum class MainTab {
    HOME,
    HISTORY,
    MAP
}

private val RoadPulseNavy = Color(0xFF102A43)
private val RoadPulseDarkNavy = Color(0xFF071A2B)
private val RoadPulseCyan = Color(0xFF00A6D6)
private val RoadPulseLightCyan = Color(0xFFDDF5FB)
private val RoadPulseBackground = Color(0xFFF7F9FB)
private val RoadPulseSlate = Color(0xFF627D98)
private val RoadPulseGreen = Color(0xFF2E7D32)
private val RoadPulseRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadPulseApp() {

    val rideControllerViewModel: RideControllerViewModel =
        viewModel()

    val rideController =
        rideControllerViewModel.rideController

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

            RoadPulseDrawer()
        }

    ) {

        Scaffold(

            containerColor = RoadPulseBackground,

            topBar = {

                RoadPulseTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },

            bottomBar = {

                RoadPulseBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                    }
                )
            }

        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                when (selectedTab) {

                    MainTab.HOME -> {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            rideController = rideController
                        )
                    }

                    MainTab.HISTORY -> {
                        HistoryScreen(
                            historyManager = HistoryManager(
                                LocalContext.current.applicationContext
                            )
                        )
                    }

                    MainTab.MAP -> {
                        MapPlaceholderScreen()
                    }
                }
            }
        }
    }
}


//Header
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoadPulseTopBar(
    onMenuClick: () -> Unit
) {

    TopAppBar(

        title = {

            Text(
                text = "RoadPulse",
                color = Color.White,
                fontSize = 20.sp
            )
        },

        navigationIcon = {

            IconButton(
                onClick = onMenuClick
            ) {

                Icon(
                    painter = painterResource(R.drawable.rp_ic_menu),
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },

        //Status dots
        actions = {

            // GPS status
            Icon(
                painter = painterResource(R.drawable.rp_ic_gps),
                contentDescription = "GPS",
                tint = RoadPulseGreen,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            // Recording status
            Icon(
                painter = painterResource(R.drawable.rp_ic_recording),
                contentDescription = "Recording",
                tint = RoadPulseSlate,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            // Future server / sync status
            Icon(
                painter = painterResource(R.drawable.rp_ic_sync),
                contentDescription = "Server sync",
                tint = RoadPulseSlate,
                modifier = Modifier.size(22.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = RoadPulseNavy
        )
    )
}




//Bottom Navigation
@Composable
private fun RoadPulseBottomNavigation(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {

    NavigationBar(
        containerColor = Color.White
    ) {

        NavigationBarItem(

            selected = selectedTab == MainTab.HOME,

            onClick = {
                onTabSelected(MainTab.HOME)
            },

            icon = {
                Icon(
                    painter = painterResource(R.drawable.rp_ic_home),
                    contentDescription = "Home"
                )
            },

            label = {
                Text("Home")
            },

            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = RoadPulseCyan,
                selectedTextColor = RoadPulseNavy,
                indicatorColor = RoadPulseLightCyan,
                unselectedIconColor = RoadPulseSlate,
                unselectedTextColor = RoadPulseSlate
            )
        )

        NavigationBarItem(

            selected = selectedTab == MainTab.HISTORY,

            onClick = {
                onTabSelected(MainTab.HISTORY)
            },

            icon = {
                Icon(
                    painter = painterResource(R.drawable.rp_ic_history),
                    contentDescription = "History"
                )
            },

            label = {
                Text("History")
            },

            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = RoadPulseCyan,
                selectedTextColor = RoadPulseNavy,
                indicatorColor = RoadPulseLightCyan,
                unselectedIconColor = RoadPulseSlate,
                unselectedTextColor = RoadPulseSlate
            )
        )

        NavigationBarItem(

            selected = selectedTab == MainTab.MAP,

            onClick = {
                onTabSelected(MainTab.MAP)
            },

            icon = {
                Icon(
                    painter = painterResource(R.drawable.rp_ic_map),
                    contentDescription = "Map"
                )
            },

            label = {
                Text("Map")
            },

            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = RoadPulseCyan,
                selectedTextColor = RoadPulseNavy,
                indicatorColor = RoadPulseLightCyan,
                unselectedIconColor = RoadPulseSlate,
                unselectedTextColor = RoadPulseSlate
            )
        )
    }
}

//Hamburger Menu
@Composable
private fun RoadPulseDrawer() {

    ModalDrawerSheet(
        drawerContainerColor = Color.White
    ) {

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // Profile header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(R.drawable.rp_ic_profile),
                contentDescription = "Profile",
                tint = RoadPulseCyan,
                modifier = Modifier.size(52.dp)
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column {

                Text(
                    text = "RoadPulse User",
                    color = RoadPulseNavy,
                    fontSize = 17.sp
                )

                Text(
                    text = "User ID: Not configured",
                    color = RoadPulseSlate,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HorizontalDivider(
            color = Color(0xFFD9E2EC)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_profile),
            title = "Profile"
        )
        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_vehicle),
            title = "Vehicles"
        )
        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_rewards),
            title = "Rewards"
        )
        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_sync),
            title = "Data & Sync"
        )
        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_settings),
            title = "Settings"
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HorizontalDivider(
            color = Color(0xFFD9E2EC)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_contributions),
            title = "My Contributions"
        )

        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_events),
            title = "Road Events"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        HorizontalDivider(
            color = Color(0xFFD9E2EC)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_help),
            title = "Help & Support"
        )

        DrawerComingSoonItem(
            icon = painterResource(R.drawable.rp_ic_info),
            title = "About RoadPulse"
        )
    }
}

//Regular Menu Items
@Composable
private fun DrawerItem(
    icon: Painter,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = icon,
            contentDescription = title,
            tint = RoadPulseNavy,
            modifier = Modifier.size(23.dp)
        )

        Spacer(
            modifier = Modifier.width(18.dp)
        )

        Text(
            text = title,
            color = RoadPulseNavy,
            fontSize = 15.sp
        )
    }
}

//Comin Soon
@Composable
private fun DrawerComingSoonItem(
    icon: Painter,
    title: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = icon,
            contentDescription = title,
            tint = RoadPulseSlate,
            modifier = Modifier.size(23.dp)
        )

        Spacer(
            modifier = Modifier.width(18.dp)
        )

        Column {

            Text(
                text = title,
                color = RoadPulseSlate,
                fontSize = 15.sp
            )

            Text(
                text = "Coming Soon",
                color = RoadPulseSlate,
                fontSize = 11.sp
            )
        }
    }
}

//existing placeholder functions:
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