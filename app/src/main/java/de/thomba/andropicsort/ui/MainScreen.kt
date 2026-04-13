package de.thomba.andropicsort.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.thomba.andropicsort.R
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onPickSource: () -> Unit,
    onPickTarget: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val dryRunLabel = stringResource(R.string.dry_run_mode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = stringResource(R.string.title), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(R.string.subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

                Spacer(modifier = Modifier.height(4.dp))

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

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = state.dryRun,
                        onCheckedChange = { viewModel.onDryRunChanged(it) },
                        enabled = !state.isRunning,
                        modifier = Modifier.semantics {
                            contentDescription = "$dryRunLabel ${if (state.dryRun) "on" else "off"}"
                        }
                    )
                    Text(text = stringResource(R.string.dry_run_mode), modifier = Modifier.padding(top = 12.dp))
                }
                if (state.dryRun) {
                    Text(text = stringResource(R.string.dry_run_active), color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = viewModel::startSort,
                    enabled = !state.isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.start_sort))
                }
            }
        }

        if (state.isRunning) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.sorting_in_progress), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
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
    Text(text = title, style = MaterialTheme.typography.bodyMedium)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = option.label },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option.selected,
                    onClick = option.onSelect,
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
        Text(text = value ?: stringResource(R.string.no_folder_selected), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = onPick, enabled = enabled) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(buttonLabel)
        }
    }
}


