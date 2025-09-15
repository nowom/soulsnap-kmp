package pl.soulsnaps.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.soulsnaps.designsystem.AppColorScheme
import pl.soulsnaps.designsystem.SoulSnapTypography

/**
 * Dialog that explains why permissions are needed and provides a button to open phone settings
 */
@Composable
fun PermissionRequiredDialog(
    permissionType: PermissionType,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = AppColorScheme.surface,
        titleContentColor = AppColorScheme.onSurface,
        textContentColor = AppColorScheme.onSurface,
        title = {
            Text(
                text = permissionType.title,
                style = SoulSnapTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = AppColorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = permissionType.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = AppColorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = permissionType.description,
                    style = SoulSnapTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = AppColorScheme.onSurfaceVariant,
                    lineHeight = SoulSnapTypography.bodyMedium.lineHeight
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Open Settings button
                    Button(
                        onClick = {
                            openAppSettings(context)
                            onOpenSettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColorScheme.primary,
                            contentColor = AppColorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Otwórz ustawienia",
                            style = SoulSnapTypography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Anuluj",
                            style = SoulSnapTypography.labelLarge,
                            color = AppColorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            // Empty - we handle buttons in the text content
        }
    )
}

/**
 * Types of permissions that can be requested
 */
enum class PermissionType(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    CAMERA(
        title = "Dostęp do aparatu",
        description = "Aby robić zdjęcia i dodawać je do wspomnień, aplikacja potrzebuje dostępu do aparatu.",
        icon = Icons.Default.CameraAlt
    ),
    GALLERY(
        title = "Dostęp do galerii",
        description = "Aby wybierać zdjęcia z galerii i dodawać je do wspomnień, aplikacja potrzebuje dostępu do galerii.",
        icon = Icons.Default.PhotoLibrary
    ),
    LOCATION(
        title = "Dostęp do lokalizacji",
        description = "Aby automatycznie dodawać lokalizację do wspomnień, aplikacja potrzebuje dostępu do lokalizacji.",
        icon = Icons.Default.LocationOn
    )
}
