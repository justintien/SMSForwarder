package com.jiapan.smsfowarder.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jiapan.smsfowarder.data.db.Webhook

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val receiverMobile by viewModel.receiverMobile.collectAsStateWithLifecycle()
    val webhooks by viewModel.webhooks.collectAsStateWithLifecycle()

    // null = dialog closed; Webhook(id=0) = add new; existing = edit
    var editing by remember { mutableStateOf<Webhook?>(null) }

    editing?.let { target ->
        WebhookEditorDialog(
            initial = target,
            onDismiss = { editing = null },
            onConfirm = { label, url ->
                if (target.id == 0L) {
                    viewModel.addWebhook(label, url)
                } else {
                    viewModel.updateWebhook(target, label, url)
                }
                editing = null
            }
        )
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = Webhook(label = "", url = "") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("新增 Webhook") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            ReceiverMobileField(
                value = receiverMobile,
                onSave = viewModel::setReceiverMobile
            )

            Text(
                text = "Discord Webhooks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            if (webhooks.isEmpty()) {
                Text(
                    text = "尚未設定任何 webhook，點右下角新增。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(webhooks, key = { it.id }) { webhook ->
                        WebhookItem(
                            webhook = webhook,
                            onToggle = { viewModel.setEnabled(webhook, it) },
                            onEdit = { editing = webhook },
                            onDelete = { viewModel.deleteWebhook(webhook) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiverMobileField(value: String, onSave: (String) -> Unit) {
    // Local editable copy so typing is smooth; persisted on Save.
    var text by remember(value) { mutableStateOf(value) }
    Column(modifier = Modifier.padding(top = 12.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("接收者手機號碼 (receiver_mobile)") },
            singleLine = true
        )
        Button(
            onClick = { onSave(text.trim()) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("儲存")
        }
    }
}

@Composable
private fun WebhookItem(
    webhook: Webhook,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("刪除這個 webhook？") },
            text = { Text(webhook.label.ifBlank { webhook.url }) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("刪除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = webhook.label.ifBlank { "(未命名)" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = webhook.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(checked = webhook.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "編輯")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "刪除")
            }
        }
    }
}

@Composable
private fun WebhookEditorDialog(
    initial: Webhook,
    onDismiss: () -> Unit,
    onConfirm: (label: String, url: String) -> Unit
) {
    var label by remember { mutableStateOf(initial.label) }
    var url by remember { mutableStateOf(initial.url) }
    val isValid = url.trim().startsWith("http")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "新增 Webhook" else "編輯 Webhook") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("名稱") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Webhook URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(label, url) },
                enabled = isValid
            ) { Text("確定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
