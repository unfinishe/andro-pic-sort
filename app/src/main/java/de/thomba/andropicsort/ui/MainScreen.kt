package de.thomba.andropicsort.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val dryRunLabel = stringResource(R.string.dry_run_mode)
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isLandscape) {
                        Text(
                            text = stringResource(R.string.title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
        ) {
            val isWideLayout = maxWidth >= 820.dp
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
                        HeroBanner(compact = compactLayout)
                        FoldersCard(state, onPickSource, onPickTarget)
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
                    HeroBanner(compact = compactLayout)
                    FoldersCard(state, onPickSource, onPickTarget)
                    RunOptionsCard(state, viewModel, dryRunLabel, compact = compactLayout)
                    StatusArea(state, compact = compactLayout)
                }
            }

            Spacer(modifier = Modifier.height(if (compactLayout) 12.dp else 18.dp))
        }
    }
}

@Composable
private fun HeroBanner(compact: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (compact) 2.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                        )
                    )
                )
                .padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(if (compact) 64.dp else 90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_scope_images_plus),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (compact) 24.dp else 28.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.hero_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.hero_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FoldersCard(
    state: MainUiState,
    onPickSource: () -> Unit,
    onPickTarget: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = stringResource(R.string.folders_section), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_source_directory),
                    contentDescription = stringResource(R.string.source_icon_desc),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(text = stringResource(R.string.source_folder), style = MaterialTheme.typography.bodyMedium)
            }
            FolderSection(
                value = state.sourceUri?.toString(),
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
                value = state.targetUri?.toString(),
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(optionsSpacing)) {
            Text(text = stringResource(R.string.run_options_section), style = MaterialTheme.typography.titleMedium)
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.report_title), style = MaterialTheme.typography.titleMedium)
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
                }
            }
        }

        state.errorMessage?.let { key ->
            val message = if (key == "missing_folders") {
                stringResource(R.string.missing_folder_selection)
            } else {
                stringResource(R.string.error_prefix, key)
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


