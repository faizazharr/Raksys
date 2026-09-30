package com.raksys.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.ui.ModalGuard
import com.raksys.core.ui.Appearance
import com.raksys.core.ui.RaksysSettings
import com.raksys.core.ui.RaksysThemeColors
import com.raksys.core.ui.raksysInteractive

/**
 * App settings: appearance and privacy. Every change applies and is saved immediately, so the only
 * button is "Selesai".
 */
@Composable
fun SettingsDialog(
    onClearSavedHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalGuard()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(10.dp),
        containerColor = RaksysThemeColors.Surface,
        title = {
            Text("Pengaturan", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RaksysThemeColors.TextPrimary)
        },
        text = {
            Column(modifier = Modifier.width(420.dp)) {
                SectionTitle("Tampilan")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RaksysThemeColors.SurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    AppearanceOption("Ikuti sistem", Appearance.SYSTEM, Modifier.weight(1f))
                    AppearanceOption("Terang", Appearance.LIGHT, Modifier.weight(1f))
                    AppearanceOption("Gelap", Appearance.DARK, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bawaan mengikuti tampilan sistem operasi.",
                    fontSize = 12.sp,
                    color = RaksysThemeColors.TextMuted,
                )

                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("Privasi")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = RaksysSettings.saveQueryHistory,
                            role = Role.Switch,
                            onValueChange = { RaksysSettings.changeSaveQueryHistory(it) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simpan riwayat kueri di disk", fontSize = 13.sp, color = RaksysThemeColors.TextPrimary)
                        Text(
                            text = "Riwayat bisa berisi nilai yang kamu ketik di kueri. Kalau dimatikan, riwayat hanya ada selama aplikasi terbuka.",
                            fontSize = 12.sp,
                            color = RaksysThemeColors.TextMuted,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // The Row above owns the click; the Switch only shows the state.
                    Switch(
                        checked = RaksysSettings.saveQueryHistory,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(checkedTrackColor = RaksysThemeColors.PrimaryFill),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onClearSavedHistory,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp),
                ) {
                    Text("Hapus semua riwayat tersimpan", fontSize = 12.sp, color = RaksysThemeColors.Error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = RaksysThemeColors.PrimaryFill, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text("Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = RaksysThemeColors.TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun AppearanceOption(label: String, value: Appearance, modifier: Modifier = Modifier) {
    val selected = RaksysSettings.appearance == value
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .height(30.dp)
            .clip(shape)
            .background(if (selected) RaksysThemeColors.PrimaryContainer else Color.Transparent)
            .border(1.dp, if (selected) RaksysThemeColors.Primary.copy(alpha = 0.6f) else Color.Transparent, shape)
            .raksysInteractive(shape, enabled = !selected)
            .selectable(selected = selected, role = Role.RadioButton, onClick = { RaksysSettings.changeAppearance(value) }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) RaksysThemeColors.Primary else RaksysThemeColors.TextSecondary,
        )
    }
}
