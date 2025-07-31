package pl.soulsnaps.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun WithCameraPermission(
    content: @Composable () -> Unit,
    deniedContent: @Composable () -> Unit
) 