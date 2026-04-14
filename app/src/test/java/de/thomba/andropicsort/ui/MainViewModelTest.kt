package de.thomba.andropicsort.ui

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import de.thomba.andropicsort.core.ConflictPolicy
import de.thomba.andropicsort.core.DateSourceMode
import de.thomba.andropicsort.core.OperationMode
import de.thomba.andropicsort.core.SortReport
import de.thomba.andropicsort.settings.SettingsStorage
import de.thomba.andropicsort.settings.StoredUiSettings
import de.thomba.andropicsort.sort.SortConfig
import de.thomba.andropicsort.sort.SortProgress
import de.thomba.andropicsort.sort.SortUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ViewModel state-machine tests for the critical scenarios in release-plan section 6.2.
 *
 * Coverage:
 *  - source == target validation (P1 guard)
 *  - missing folders guard
 *  - double-start guard (isRunning)
 *  - dry run flag propagated to use case
 *  - copy mode propagated to use case
 *  - move mode propagated to use case
 *  - conflict policy: RENAME propagated
 *  - conflict policy: OVERWRITE propagated
 *  - date source mode: METADATA_THEN_FILE propagated
 *  - date source mode: FILE_ONLY propagated
 *  - progress updates reflected in state
 *  - successful run produces report and clears error
 *  - persisted URI that is no longer accessible is dropped on restore
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var app: Application
    private lateinit var fakeSortUseCase: FakeSortUseCase
    private lateinit var fakeSettingsStorage: FakeSettingsStorage
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = ApplicationProvider.getApplicationContext()
        fakeSortUseCase = FakeSortUseCase()
        fakeSettingsStorage = FakeSettingsStorage()
        viewModel = MainViewModel(app, fakeSortUseCase, fakeSettingsStorage, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Guard: source == target ─────────────────────────────────────────

    @Test
    fun `startSort with source equals target sets source_equals_target error`() = runTest {
        val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary:DCIM")
        viewModel.onSourceSelected(uri)
        viewModel.onTargetSelected(uri)

        viewModel.startSort()

        assertEquals("source_equals_target", viewModel.uiState.value.errorMessage)
        assertNull("No sort must run", fakeSortUseCase.lastConfig)
    }

    // ── Guard: missing folders ──────────────────────────────────────────

    @Test
    fun `startSort with no folders set sets missing_folders error`() = runTest {
        viewModel.startSort()

        assertEquals("missing_folders", viewModel.uiState.value.errorMessage)
        assertNull("No sort must run", fakeSortUseCase.lastConfig)
    }

    @Test
    fun `startSort with only source set sets missing_folders error`() = runTest {
        viewModel.onSourceSelected(Uri.parse("content://test/source"))

        viewModel.startSort()

        assertEquals("missing_folders", viewModel.uiState.value.errorMessage)
        assertNull("No sort must run", fakeSortUseCase.lastConfig)
    }

    @Test
    fun `startSort with only target set sets missing_folders error`() = runTest {
        viewModel.onTargetSelected(Uri.parse("content://test/target"))

        viewModel.startSort()

        assertEquals("missing_folders", viewModel.uiState.value.errorMessage)
        assertNull("No sort must run", fakeSortUseCase.lastConfig)
    }

    // ── Guard: double-start while running ──────────────────────────────

    @Test
    fun `startSort while already running is ignored`() = runTest {
        val blocker = CompletableDeferred<Unit>()
        fakeSortUseCase.blocker = blocker
        setDistinctFolders()

        viewModel.startSort()
        advanceUntilIdle() // sort runs to blocker.await(), isRunning=true

        assertTrue("Sort must be in progress", viewModel.uiState.value.isRunning)
        assertEquals("First call ran once", 1, fakeSortUseCase.callCount)

        viewModel.startSort() // guard: isRunning=true → no-op
        advanceUntilIdle()

        assertEquals("Second call must have been ignored", 1, fakeSortUseCase.callCount)
        assertTrue("Must still be running", viewModel.uiState.value.isRunning)

        // Unblock and complete the sort cleanly
        blocker.complete(Unit)
        advanceUntilIdle()
        assertFalse("Sort must have finished", viewModel.uiState.value.isRunning)
    }

    // ── Dry run flag ────────────────────────────────────────────────────

    @Test
    fun `startSort with dryRun true passes dryRun=true to use case`() = runTest {
        setDistinctFolders()
        viewModel.onDryRunChanged(true)

        viewModel.startSort()
        advanceUntilIdle()

        assertTrue("dryRun must be true", fakeSortUseCase.lastConfig!!.dryRun)
        assertTrue("report must reflect dryRun", viewModel.uiState.value.report!!.dryRun)
    }

    @Test
    fun `startSort with dryRun false passes dryRun=false to use case`() = runTest {
        setDistinctFolders()
        viewModel.onDryRunChanged(false)

        viewModel.startSort()
        advanceUntilIdle()

        assertTrue("dryRun must be false", !fakeSortUseCase.lastConfig!!.dryRun)
    }

    // ── Operation mode: copy vs move ────────────────────────────────────

    @Test
    fun `startSort in COPY mode passes COPY to use case`() = runTest {
        setDistinctFolders()
        viewModel.onModeChanged(OperationMode.COPY)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(OperationMode.COPY, fakeSortUseCase.lastConfig!!.mode)
    }

    @Test
    fun `startSort in MOVE mode passes MOVE to use case`() = runTest {
        setDistinctFolders()
        viewModel.onModeChanged(OperationMode.MOVE)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(OperationMode.MOVE, fakeSortUseCase.lastConfig!!.mode)
    }

    // ── Conflict policies ───────────────────────────────────────────────

    @Test
    fun `startSort with conflict policy RENAME passes RENAME to use case`() = runTest {
        setDistinctFolders()
        viewModel.onConflictPolicyChanged(ConflictPolicy.RENAME)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(ConflictPolicy.RENAME, fakeSortUseCase.lastConfig!!.conflictPolicy)
    }

    @Test
    fun `startSort with conflict policy OVERWRITE passes OVERWRITE to use case`() = runTest {
        setDistinctFolders()
        viewModel.onConflictPolicyChanged(ConflictPolicy.OVERWRITE)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(ConflictPolicy.OVERWRITE, fakeSortUseCase.lastConfig!!.conflictPolicy)
    }

    // ── Date source modes ───────────────────────────────────────────────

    @Test
    fun `startSort with METADATA_THEN_FILE passes that mode to use case`() = runTest {
        setDistinctFolders()
        viewModel.onDateSourceModeChanged(DateSourceMode.METADATA_THEN_FILE)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(DateSourceMode.METADATA_THEN_FILE, fakeSortUseCase.lastConfig!!.dateSourceMode)
    }

    @Test
    fun `startSort with FILE_ONLY passes that mode to use case`() = runTest {
        setDistinctFolders()
        viewModel.onDateSourceModeChanged(DateSourceMode.FILE_ONLY)

        viewModel.startSort()
        advanceUntilIdle()

        assertEquals(DateSourceMode.FILE_ONLY, fakeSortUseCase.lastConfig!!.dateSourceMode)
    }

    // ── Report and state after successful run ───────────────────────────

    @Test
    fun `successful sort produces report and clears error`() = runTest {
        setDistinctFolders()
        fakeSortUseCase.reportToReturn = SortReport(
            processed = 5, copied = 5, moved = 0, failed = 0, skipped = 0,
            planned = 0, renamed = 0, dryRun = false,
        )

        viewModel.startSort()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull("No error after success", state.errorMessage)
        assertNotNull("Report must be present", state.report)
        assertEquals(5, state.report!!.copied)
        assertTrue("Must not still be running", !state.isRunning)
    }

    // ── URI recovery: inaccessible persisted URI ────────────────────────

    @Test
    fun `restoreSettings drops URI that is no longer accessible`() = runTest {
        val inaccessibleUri = Uri.parse("content://test/gone")
        val freshStorage = FakeSettingsStorage().also {
            it.stored = StoredUiSettings(sourceUri = inaccessibleUri)
        }

        // Create a fresh ViewModel backed by its own storage; inject testDispatcher
        // so advanceUntilIdle() controls its coroutines.
        val vm = MainViewModel(app, fakeSortUseCase, freshStorage, testDispatcher)
        advanceUntilIdle()

        // hasPersistedPermission returns false for all URIs in Robolectric (no SAF grants).
        assertNull("Inaccessible URI must be dropped", vm.uiState.value.sourceUri)
        assertTrue("Storage must be rewritten after URI drop", freshStorage.saveWasCalled)
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun setDistinctFolders() {
        viewModel.onSourceSelected(Uri.parse("content://test/source"))
        viewModel.onTargetSelected(Uri.parse("content://test/target"))
    }
}

// ── Fakes ────────────────────────────────────────────────────────────────

class FakeSortUseCase : SortUseCase {
    var lastConfig: SortConfig? = null
    var callCount = 0
    /**
     * Set to a [CompletableDeferred] to pause [run] until it is completed.
     * Leave null (default) for an immediate, non-blocking fake.
     */
    var blocker: CompletableDeferred<Unit>? = null
    var reportToReturn = SortReport(
        processed = 0, copied = 0, moved = 0, failed = 0, skipped = 0,
        planned = 0, renamed = 0, dryRun = false,
    )

    override suspend fun run(config: SortConfig, onProgress: (SortProgress) -> Unit): SortReport {
        lastConfig = config
        callCount++
        blocker?.await()
        return reportToReturn.copy(dryRun = config.dryRun)
    }
}

class FakeSettingsStorage : SettingsStorage {
    var stored: StoredUiSettings = StoredUiSettings()
    var saveWasCalled = false

    override suspend fun load(): StoredUiSettings = stored

    override suspend fun save(settings: StoredUiSettings) {
        stored = settings
        saveWasCalled = true
    }
}









