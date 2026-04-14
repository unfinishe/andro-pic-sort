package de.thomba.andropicsort.ui

import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import de.thomba.andropicsort.R
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onPickSource: () -> Unit,
    onPickTarget: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val isWideLayout = configuration.screenWidthDp >= 820
    val dryRunLabel = stringResource(R.string.dry_run_mode)
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            // maxHeight is unbounded inside verticalScroll; landscape detection via LocalConfiguration covers compact-height case.
            val compactLayout = isWideLayout || isLandscape
            val sectionSpacing = if (compactLayout) 10.dp else 14.dp

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                    ) {
                        AppIdentityHeader(compact = compactLayout, onAboutClick = { showAbout = true })
                        FoldersCard(state, onPickSource, onPickTarget, compact = compactLayout)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                    ) {
                        RunOptionsCard(state, viewModel, dryRunLabel, compact = compactLayout)
                        StatusArea(state, compact = compactLayout)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(sectionSpacing),
                ) {
                    AppIdentityHeader(compact = compactLayout, onAboutClick = { showAbout = true })
                    FoldersCard(state, onPickSource, onPickTarget, compact = compactLayout)
                    RunOptionsCard(state, viewModel, dryRunLabel, compact = compactLayout)
                    StatusArea(state, compact = compactLayout)
                }
            }

            Spacer(modifier = Modifier.height(if (compactLayout) 12.dp else 18.dp))
        }
    }
}

@Composable
private fun AppIdentityHeader(compact: Boolean, onAboutClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (compact) 4.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(26.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (compact) 66.dp else 94.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            )
            IconButton(
                onClick = onAboutClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = if (compact) 4.dp else 6.dp, end = if (compact) 4.dp else 6.dp)
                    .size(if (compact) 32.dp else 40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_about),
                    contentDescription = stringResource(R.string.about_title),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(0.58f),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_folders_pair),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(if (compact) 22.dp else 26.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.hero_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.hero_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.28f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                )
            }
        }
    }
}

@Composable
private fun HeaderBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun FoldersCard(
    state: MainUiState,
    onPickSource: () -> Unit,
    onPickTarget: () -> Unit,
    compact: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        CardAccentHeader(
            title = stringResource(R.string.folders_section),
            accentIconRes = R.drawable.ic_folders_pair,
            compact = compact,
            description = stringResource(R.string.folders_section_description),
        )
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_source_directory),
                    contentDescription = stringResource(R.string.source_icon_desc),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = stringResource(R.string.source_folder), style = MaterialTheme.typography.bodyMedium)
            }
            FolderSection(
                value = state.sourceUri?.toDisplayText(),
                buttonLabel = stringResource(R.string.choose_source_folder),
                iconRes = R.drawable.ic_source_directory,
                onPick = onPickSource,
                enabled = !state.isRunning,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_target_directory),
                    contentDescription = stringResource(R.string.target_icon_desc),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = stringResource(R.string.target_folder), style = MaterialTheme.typography.bodyMedium)
            }
            FolderSection(
                value = state.targetUri?.toDisplayText(),
                buttonLabel = stringResource(R.string.choose_target_folder),
                iconRes = R.drawable.ic_target_directory,
                onPick = onPickTarget,
                enabled = !state.isRunning,
            )
        }
    }
}

@Composable
private fun RunOptionsCard(
    state: MainUiState,
    viewModel: MainViewModel,
    dryRunLabel: String,
    compact: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        val optionsSpacing = if (compact) 8.dp else 10.dp
        CardAccentHeader(
            title = stringResource(R.string.run_options_section),
            accentIconRes = R.drawable.ic_tune,
            compact = compact,
            description = stringResource(R.string.run_options_section_description),
        )
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(optionsSpacing)) {
            OptionSelector(
                title = stringResource(R.string.operation_mode),
                enabled = !state.isRunning,
                options = listOf(
                    SelectorOption(
                        label = stringResource(R.string.copy_mode),
                        selected = state.mode == OperationMode.COPY,
                        onSelect = { viewModel.onModeChanged(OperationMode.COPY) },
                        iconRes = R.drawable.ic_copy_action,
                        iconDescription = stringResource(R.string.copy_icon_desc),
                    ),
                    SelectorOption(
                        label = stringResource(R.string.move_mode),
                        selected = state.mode == OperationMode.MOVE,
                        onSelect = { viewModel.onModeChanged(OperationMode.MOVE) },
                        iconRes = R.drawable.ic_move_action,
                        iconDescription = stringResource(R.string.move_icon_desc),
                    ),
                ),
            )

            OptionSelector(
                title = stringResource(R.string.conflict_policy),
                enabled = !state.isRunning,
                options = listOf(
                    SelectorOption(
                        label = stringResource(R.string.conflict_rename),
                        selected = state.conflictPolicy == ConflictPolicy.RENAME,
                        onSelect = { viewModel.onConflictPolicyChanged(ConflictPolicy.RENAME) },
                    ),
                    SelectorOption(
                        label = stringResource(R.string.conflict_overwrite),
                        selected = state.conflictPolicy == ConflictPolicy.OVERWRITE,
                        onSelect = { viewModel.onConflictPolicyChanged(ConflictPolicy.OVERWRITE) },
                    ),
                ),
            )

            OptionSelector(
                title = stringResource(R.string.date_source_mode),
                enabled = !state.isRunning,
                options = listOf(
                    SelectorOption(
                        label = stringResource(R.string.date_source_metadata_then_file),
                        selected = state.dateSourceMode == DateSourceMode.METADATA_THEN_FILE,
                        onSelect = { viewModel.onDateSourceModeChanged(DateSourceMode.METADATA_THEN_FILE) },
                        iconRes = R.drawable.ic_date_metadata,
                        iconDescription = stringResource(R.string.date_source_metadata_icon_desc),
                    ),
                    SelectorOption(
                        label = stringResource(R.string.date_source_file_only),
                        selected = state.dateSourceMode == DateSourceMode.FILE_ONLY,
                        onSelect = { viewModel.onDateSourceModeChanged(DateSourceMode.FILE_ONLY) },
                        iconRes = R.drawable.ic_date_file,
                        iconDescription = stringResource(R.string.date_source_file_icon_desc),
                    ),
                ),
            )

            OptionSelector(
                title = stringResource(R.string.sort_scope),
                enabled = !state.isRunning,
                options = listOf(
                    SelectorOption(
                        label = stringResource(R.string.sort_scope_images_only),
                        selected = !state.sortNonImages,
                        onSelect = { viewModel.onSortNonImagesChanged(false) },
                        iconRes = R.drawable.ic_scope_images_only,
                        iconDescription = stringResource(R.string.sort_scope_images_only_icon_desc),
                    ),
                    SelectorOption(
                        label = stringResource(R.string.sort_scope_images_and_non_images),
                        selected = state.sortNonImages,
                        onSelect = { viewModel.onSortNonImagesChanged(true) },
                        iconRes = R.drawable.ic_scope_images_plus,
                        iconDescription = stringResource(R.string.sort_scope_images_and_non_images_icon_desc),
                    ),
                ),
            )

            if (state.sortNonImages) {
                Text(text = stringResource(R.string.sort_non_images_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.dryRun,
                    onCheckedChange = { viewModel.onDryRunChanged(it) },
                    enabled = !state.isRunning,
                    modifier = Modifier.semantics {
                        contentDescription = "$dryRunLabel ${if (state.dryRun) "on" else "off"}"
                    }
                )
                Text(text = stringResource(R.string.dry_run_mode))
            }
            if (state.dryRun) {
                Text(text = stringResource(R.string.dry_run_active), color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = viewModel::startSort,
                enabled = !state.isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 46.dp else 50.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.start_sort))
            }
        }
    }
}

@Composable
private fun StatusArea(state: MainUiState, compact: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)) {
        if (state.isRunning) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.sorting_in_progress), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                    }
                    if (state.progressTotal > 0) {
                        val progressValue = state.progressProcessed.toFloat() / state.progressTotal.toFloat()
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "${state.progressProcessed} of ${state.progressTotal}"
                                },
                        )
                        Text(
                            text = stringResource(
                                R.string.progress_value,
                                state.progressProcessed,
                                state.progressTotal,
                            )
                        )
                    }
                }
            }
        }

        state.report?.let { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                CardAccentHeader(
                    title = stringResource(R.string.report_title),
                    accentIconRes = R.drawable.ic_summary,
                    compact = compact,
                )
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            R.string.report_line,
                            report.processed,
                            report.copied,
                            report.moved,
                            report.failed,
                            report.skipped,
                            report.planned,
                            report.renamed,
                        )
                    )
                    Text(
                        text = stringResource(
                            R.string.report_errors_line,
                            report.createFailed,
                            report.copyFailed,
                            report.deleteFailed,
                        )
                    )
                    if (report.dryRun) {
                        Text(text = stringResource(R.string.dry_run_report_note))
                    }
                    if (report.durationMillis > 0) {
                        Text(
                            text = stringResource(R.string.report_duration, formatDuration(report.durationMillis)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { key ->
            val message = when (key) {
                "missing_folders" -> stringResource(R.string.missing_folder_selection)
                "source_equals_target" -> stringResource(R.string.source_equals_target)
                else -> stringResource(R.string.error_prefix, key)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

private data class SelectorOption(
    val label: String,
    val selected: Boolean,
    val onSelect: () -> Unit,
    val iconRes: Int? = null,
    val iconDescription: String? = null,
)

@Composable
private fun CardAccentHeader(
    title: String,
    accentIconRes: Int,
    compact: Boolean = false,
    description: String? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                    )
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            )
            .padding(horizontal = 14.dp, vertical = if (compact) 9.dp else 11.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(if (compact) 52.dp else 66.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(accentIconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OptionSelector(
    title: String,
    options: List<SelectorOption>,
    enabled: Boolean,
) {
    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled, onClick = option.onSelect)
                    .background(
                        if (option.selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 4.dp)
                    .semantics { contentDescription = option.label },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option.selected,
                    onClick = null,
                    enabled = enabled,
                )
                option.iconRes?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = option.iconDescription,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Text(text = option.label)
            }
        }
    }
}

@Composable
private fun FolderSection(
    value: String?,
    buttonLabel: String,
    iconRes: Int,
    onPick: () -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = value ?: stringResource(R.string.no_folder_selected),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = onPick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(buttonLabel)
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}.${(millis % 1000) / 100}s"
}

@Composable
private fun Uri.toDisplayText(): String {
    val internalStorage = stringResource(R.string.folder_storage_internal)
    val externalStorage = stringResource(R.string.folder_storage_external)

    val decodedUri = Uri.decode(toString())
    if (!DocumentsContract.isTreeUri(this)) return decodedUri

    val treeId = runCatching { DocumentsContract.getTreeDocumentId(this) }
        .getOrNull()
        ?.let(Uri::decode)
        ?: return decodedUri

    val separatorIndex = treeId.indexOf(':')
    if (separatorIndex <= 0) return treeId

    val volumeId = treeId.substring(0, separatorIndex)
    val relativePath = treeId.substring(separatorIndex + 1).trim('/').ifBlank { null }
    val volumeLabel = if (volumeId.equals("primary", ignoreCase = true)) {
        internalStorage
    } else {
        "$externalStorage ($volumeId)"
    }

    return if (relativePath == null) volumeLabel else "$volumeLabel/$relativePath"
}


