package com.xiaominote.app.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var token by remember(state.token) { mutableStateOf(state.token) }
    var owner by remember(state.owner) { mutableStateOf(state.owner) }
    var repo by remember(state.repo) { mutableStateOf(state.repo) }
    var branch by remember(state.branch) { mutableStateOf(state.branch) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                title = { Text("设置") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ---------- GitHub sync ----------
            SectionTitle("GitHub 自动同步")
            OutlinedTextField(
                value = token, onValueChange = { token = it },
                label = { Text("个人访问令牌 (PAT)") },
                placeholder = { Text("ghp_...") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = owner, onValueChange = { owner = it },
                label = { Text("仓库所有者 (用户名)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = repo, onValueChange = { repo = it },
                label = { Text("仓库名称 (私有仓库)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = branch, onValueChange = { branch = it },
                label = { Text("分支") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.updateCredentials(token, owner, repo, branch) }) { Text("保存凭据") }
                Button(onClick = { viewModel.verifyCredentials() }, enabled = !state.verifying) {
                    Text(if (state.verifying) "验证中…" else "验证")
                }
            }
            state.verifyMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            HorizontalDivider()

            SwitchRow(
                title = "启用自动同步",
                subtitle = "在后台定期上传/下载笔记到 GitHub",
                checked = state.syncEnabled,
                onChange = viewModel::setSyncEnabled,
            )
            SwitchRow(
                title = "仅 Wi-Fi 同步",
                subtitle = "避免使用移动数据流量",
                checked = state.syncOnWifiOnly,
                onChange = viewModel::setWifiOnly,
            )

            Text("同步间隔：每 ${state.syncIntervalMinutes} 分钟")
            Slider(
                value = state.syncIntervalMinutes.toFloat(),
                onValueChange = { viewModel.setSyncInterval(it.toInt()) },
                valueRange = 15f..360f,
                steps = 0,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.syncNow() }, enabled = !state.syncing) {
                    Text(if (state.syncing) "同步中…" else "立即同步")
                }
            }
            state.syncMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            HorizontalDivider()

            // ---------- Theme ----------
            SectionTitle("外观")
            val themeOptions = listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色")
            var themeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                OutlinedTextField(
                    value = themeOptions.firstOrNull { it.first == state.darkTheme }?.second ?: "跟随系统",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("主题模式") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(themeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                androidx.compose.material3.ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                    themeOptions.forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.setDarkTheme(value); themeExpanded = false })
                    }
                }
            }
            SwitchRow(
                title = "动态取色",
                subtitle = "根据壁纸自动生成配色 (Android 12+)",
                checked = state.dynamicColor,
                onChange = viewModel::setDynamicColor,
            )

            HorizontalDivider()

            // ---------- Recycle bin ----------
            SectionTitle("回收站")
            Text("自动清理：${if (state.recycleBinRetentionDays == 0) "永不" else "${state.recycleBinRetentionDays} 天后"}")
            Slider(
                value = state.recycleBinRetentionDays.toFloat(),
                onValueChange = { viewModel.setRecycleBinRetention(it.toInt()) },
                valueRange = 0f..90f,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
