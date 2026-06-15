package com.myniyam.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R

/** One tab: either a vector (core icon) or a drawable resource for the glyph. */
private data class NavTab(
    val route: String,
    val labelRes: Int,
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null
)

/**
 * Floating pill bottom navigation (SP-P1, founder reference). Four tabs; the
 * active one sits in a brand-orange circle with a white glyph. Rendered as an
 * overlay over the top-level screens (Today / Library / Favourites / Settings).
 */
@Composable
fun NiyamBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    val tabs = listOf(
        NavTab(NiyamRoutes.HOME, R.string.nav_today, iconVector = Icons.Filled.Home),
        NavTab(NiyamRoutes.LIBRARY, R.string.nav_library, iconRes = R.drawable.ic_library),
        NavTab(NiyamRoutes.FAVOURITES, R.string.nav_favourites, iconVector = Icons.Filled.Favorite),
        NavTab(NiyamRoutes.SETTINGS, R.string.nav_settings, iconVector = Icons.Filled.Settings)
    )
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 28.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(999.dp),
                    ambientColor = Color(0xFF7A3D12).copy(alpha = 0.14f),
                    spotColor = Color(0xFF7A3D12).copy(alpha = 0.14f)
                )
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                NavItem(
                    selected = currentRoute == tab.route,
                    tab = tab,
                    onClick = { if (currentRoute != tab.route) onSelect(tab.route) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(selected: Boolean, tab: NavTab, onClick: () -> Unit) {
    val tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val glyph: @Composable () -> Unit = {
            val desc = stringResource(tab.labelRes)
            if (tab.iconVector != null) {
                Icon(tab.iconVector, contentDescription = desc, tint = tint, modifier = Modifier.size(26.dp))
            } else {
                Icon(painterResource(tab.iconRes!!), contentDescription = desc, tint = tint, modifier = Modifier.size(26.dp))
            }
        }
        if (selected) {
            Box(
                modifier = Modifier.size(46.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) { glyph() }
        } else {
            glyph()
        }
    }
}
