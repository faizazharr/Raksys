package com.raksys.feature.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raksys.core.model.ConnectionProfile
import com.raksys.core.model.DbType
import com.raksys.core.ui.DbTypeLogo
import com.raksys.core.ui.RaksysStatusBadge
import com.raksys.core.ui.RaksysThemeColors

@Composable
fun ConnectionCard(
    profile: ConnectionProfile,
    isSelected: Boolean,
    isTesting: Boolean,
    onClick: () -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit,
) {
    val (badgeColor, badgeBg) = when (profile.dbType) {
        DbType.POSTGRES -> RaksysThemeColors.PostgresColor to Color(0xFF1E334D)
        DbType.MYSQL -> RaksysThemeColors.MysqlColor to Color(0xFF3D2D14)
        DbType.SQLITE -> RaksysThemeColors.SqliteColor to Color(0xFF1B374C)
        DbType.MONGODB -> RaksysThemeColors.MongodbColor to Color(0xFF193B26)
        DbType.REDIS -> RaksysThemeColors.RedisColor to Color(0xFF421D1D)
    }

    val cardBg = if (isSelected) RaksysThemeColors.PrimaryContainer else RaksysThemeColors.SurfaceElevated
    val borderColor = if (isSelected) RaksysThemeColors.Primary else RaksysThemeColors.BorderLight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    DbTypeLogo(typeName = profile.dbType.name, color = badgeColor, size = 16.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RaksysThemeColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val (envColor, envBg, envLabel) = when (profile.environment) {
                    com.raksys.core.model.EnvironmentType.DEVELOPMENT -> Triple(RaksysThemeColors.EnvDev, RaksysThemeColors.EnvDevBg, "DEV")
                    com.raksys.core.model.EnvironmentType.STAGING -> Triple(RaksysThemeColors.EnvStaging, RaksysThemeColors.EnvStagingBg, "STG")
                    com.raksys.core.model.EnvironmentType.PRODUCTION -> Triple(RaksysThemeColors.EnvProd, RaksysThemeColors.EnvProdBg, "PROD")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RaksysStatusBadge(
                        text = envLabel,
                        statusColor = envColor,
                        bgColor = envBg
                    )
                    RaksysStatusBadge(
                        text = profile.dbType.name,
                        statusColor = badgeColor,
                        bgColor = badgeBg
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val targetDesc = if (profile.dbType == DbType.SQLITE) {
                profile.database.ifBlank { "SQLite File" }
            } else {
                "${profile.host}:${profile.port}"
            }

            Text(
                text = targetDesc,
                style = MaterialTheme.typography.bodySmall,
                color = RaksysThemeColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onTest,
                    enabled = !isTesting,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = RaksysThemeColors.Primary
                        )
                    } else {
                        Text("Test", fontSize = 11.sp, color = RaksysThemeColors.Info)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("🗑", fontSize = 11.sp)
                }
            }
        }
    }
}
