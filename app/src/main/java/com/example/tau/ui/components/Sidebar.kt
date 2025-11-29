package com.example.tau.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tau.navigation.Screen

private const val SIDEBAR_WIDTH_DP = 280
private const val ITEM_CORNER_RADIUS_DP = 50
private const val OVERLAY_ALPHA = 0.5f
private const val ICON_SIZE_DP = 24
private const val ITEM_PADDING_DP = 12
private const val TEXT_PADDING_START_DP = 12
private const val COLUMN_PADDING_START_DP = 8
private const val COLUMN_PADDING_TOP_DP = 48
private const val COLUMN_PADDING_END_DP = 8
private const val COLUMN_PADDING_BOTTOM_DP = 8
private const val HOVER_BACKGROUND_ALPHA = 0.15f

@Composable
fun Sidebar(
    isOpen: Boolean,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        ClickOutsideOverlay(onClickOutside = onClose)
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { -it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        SidebarContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            onClose = onClose,
            modifier = modifier
                .width(SIDEBAR_WIDTH_DP.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
private fun ClickOutsideOverlay(onClickOutside: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = OVERLAY_ALPHA))
            .clickable { onClickOutside() }
    )
}

@Composable
private fun SidebarContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = COLUMN_PADDING_START_DP.dp,
                top = COLUMN_PADDING_TOP_DP.dp,
                end = COLUMN_PADDING_END_DP.dp,
                bottom = COLUMN_PADDING_BOTTOM_DP.dp
            )
    ) {
        Screen.getAllScreens().forEach { screen ->
            SidebarItem(
                screen = screen,
                isSelected = currentRoute == screen.route,
                onClick = {
                    onNavigate(screen.route)
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun SidebarItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(ITEM_CORNER_RADIUS_DP.dp)
    val colorScheme = MaterialTheme.colorScheme

    val (backgroundColor, contentColor) = getSidebarItemColors(
        colorScheme = colorScheme,
        isSelected = isSelected,
        isHovered = isHovered.value
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .background(color = backgroundColor, shape = shape)
            .padding(ITEM_PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            modifier = Modifier.size(ICON_SIZE_DP.dp),
            tint = contentColor
        )
        Text(
            text = screen.title,
            modifier = Modifier.padding(start = TEXT_PADDING_START_DP.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun getSidebarItemColors(colorScheme: ColorScheme, isSelected: Boolean, isHovered: Boolean): Pair<Color, Color> {
    val backgroundColor = when {
        isSelected -> colorScheme.primaryContainer
        isHovered -> colorScheme.secondary.copy(alpha = HOVER_BACKGROUND_ALPHA)
        else -> colorScheme.surface
    }

    val contentColor = if (isSelected) {
        colorScheme.onPrimaryContainer
    } else {
        colorScheme.onSurface
    }

    return backgroundColor to contentColor
}
