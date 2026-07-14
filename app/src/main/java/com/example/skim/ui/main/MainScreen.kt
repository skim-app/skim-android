package com.example.skim.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.skim.BuildConfig
import com.example.skim.model.Recording
import com.example.skim.model.SummaryItem
import com.example.skim.model.SummarySource
import com.example.skim.model.TranscriptChunk
import com.example.skim.theme.SkimTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private enum class SkimScreen { List, Detail }
private enum class MainDestination(val label: String, val accessibilityLabel: String) {
    Home("홈", "홈"),
    Add("추가", "음성 파일 추가"),
    Library("라이브러리", "라이브러리"),
    Settings("설정", "설정"),
}
private data class PlaybackUiState(
    val positionMs: Long = 0,
    val durationMs: Long? = null,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun SkimMainRoute(viewModel: SkimMainViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        val filename = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            cursor.takeIf { it.moveToFirst() }?.getString(0)
        } ?: "recording.m4a"
        viewModel.upload(filename.substringBeforeLast('.').ifBlank { "음성 메모" }, filename, context.contentResolver.getType(uri) ?: "audio/mp4", bytes)
    }
    MainScreen(uiState = uiState, onRefresh = viewModel::refresh, onPickAudio = { picker.launch(arrayOf("audio/*")) }, modifier = modifier)
}

@Composable
fun MainScreen(
    uiState: SkimUiState,
    onRefresh: () -> Unit,
    onPickAudio: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        SkimUiState.Loading -> LoadingScreen(modifier)
        is SkimUiState.Error -> ErrorScreen(message = uiState.message, onRefresh = onRefresh, modifier = modifier)
        is SkimUiState.Content -> RecordingContent(
            recordings = uiState.recordings,
            refreshError = uiState.refreshError,
            onPickAudio = onPickAudio,
            modifier = modifier,
        )
    }
}

@Composable
private fun RecordingContent(
    recordings: List<Recording>,
    refreshError: String?,
    onPickAudio: () -> Unit,
    modifier: Modifier,
) {
    var screen by rememberSaveable { mutableStateOf(SkimScreen.List) }
    var destination by rememberSaveable { mutableStateOf(MainDestination.Home) }
    var selectedRecordingId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedRecording = recordings.firstOrNull { it.id == selectedRecordingId }

    LaunchedEffect(recordings) {
        if (selectedRecording == null) selectedRecordingId = recordings.firstOrNull()?.id
    }

    when {
        screen == SkimScreen.List -> RecordingListScreen(
            modifier = modifier,
            recordings = recordings,
            refreshError = refreshError,
            destination = destination,
            onDestinationChange = { destination = it },
            onPickAudio = onPickAudio,
            onRecordingClick = { recording ->
                selectedRecordingId = recording.id
                screen = SkimScreen.Detail
            },
        )
        selectedRecording != null -> RecordingDetailScreen(
            modifier = modifier,
            recording = selectedRecording,
            onBack = { screen = SkimScreen.List },
        )
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("녹음 목록을 불러오는 중입니다.")
    }
}

@Composable
private fun EmptyScreen(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("아직 저장된 녹음이 없습니다.")
    }
}

@Composable
private fun ErrorScreen(message: String, onRefresh: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message)
        TextButton(onClick = onRefresh) { Text("다시 시도") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordingListScreen(
    recordings: List<Recording>,
    refreshError: String?,
    destination: MainDestination,
    onDestinationChange: (MainDestination) -> Unit,
    onPickAudio: () -> Unit,
    onRecordingClick: (Recording) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Column {
                        Text("Skim", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "검증 가능한 AI 음성 노트",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { onDestinationChange(item) },
                        icon = { DestinationIcon(item) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (destination == MainDestination.Home) item { HeroCard() }
            refreshError?.let { error ->
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            when (destination) {
                MainDestination.Home, MainDestination.Library -> {
                    val visibleRecordings = if (destination == MainDestination.Home) recordings.take(3) else recordings
                    item {
                        Text(
                            text = if (destination == MainDestination.Home) "최근 녹음 ${visibleRecordings.size}개" else "전체 녹음 ${visibleRecordings.size}개",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    if (visibleRecordings.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("첫 녹음을 추가해보세요", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "파일을 올리면 요약과 timestamp 근거를 한 화면에서 확인할 수 있습니다.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Button(onClick = onPickAudio, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                        Icon(Icons.Filled.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.size(8.dp))
                                        Text("음성 파일 추가")
                                    }
                                }
                            }
                        }
                    }
                    items(visibleRecordings, key = { it.id }) { recording ->
                        RecordingCard(recording = recording, onClick = { onRecordingClick(recording) })
                    }
                }
                MainDestination.Add -> item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("음성 파일을 선택해 업로드하세요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("업로드 후 처리 상태와 결과가 자동으로 갱신됩니다.", style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = onPickAudio, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("파일 선택 후 업로드")
                            }
                        }
                    }
                }
                MainDestination.Settings -> item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("처리 정책", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("음성 파일은 서버에서 처리되며, 요약의 timestamp로 원문 근거를 확인할 수 있습니다.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationIcon(destination: MainDestination) {
    val image = when (destination) {
        MainDestination.Home -> Icons.Filled.Home
        MainDestination.Add -> Icons.Filled.Add
        MainDestination.Library -> Icons.AutoMirrored.Filled.LibraryBooks
        MainDestination.Settings -> Icons.Filled.Settings
    }
    Icon(imageVector = image, contentDescription = destination.accessibilityLabel)
}

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("요약을 믿기 전에,\n근거를 확인하세요", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Skim은 요약 항목의 timestamp를 원문 구간에 연결합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)) {
                Text(
                    text = "듣고, 훑고, 확인한다",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RecordingCard(recording: Recording, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusDot()
                Text(recording.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("· ${recording.duration}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(recording.title, style = MaterialTheme.typography.titleMedium)
            Text(recording.oneLineSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            recording.audioAssetName?.let { assetName ->
                Text(
                    text = "내장 오디오 · $assetName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(recording.createdAt.take(10), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(
                        "근거 ${recording.transcriptChunks.size}개",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDot() {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordingDetailScreen(
    recording: Recording,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable(recording.id) { mutableIntStateOf(0) }
    var highlightedChunkId by rememberSaveable(recording.id) { mutableStateOf<String?>(null) }
    val playbackStateHolder = remember(recording.id) { mutableStateOf(PlaybackUiState()) }
    val playbackError = remember(recording.id) { mutableStateOf<String?>(null) }
    val playbackState by playbackStateHolder
    var mediaLoaded by remember(recording.id) { mutableStateOf(false) }
    val context = LocalContext.current
    val player = remember(recording.id) { ExoPlayer.Builder(context).build() }
    fun updatePlaybackState() {
        playbackStateHolder.value = PlaybackUiState(
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET && it >= 0 },
            isPlaying = player.isPlaying,
            errorMessage = playbackError.value,
        )
    }
    fun playFrom(positionMs: Long) {
        if (!recording.audioAvailable) {
            playbackError.value = "오디오가 없어 재생할 수 없습니다."
            updatePlaybackState()
            return
        }
        if (!mediaLoaded) {
            player.setMediaItem(MediaItem.fromUri("${BuildConfig.SKIM_BASE_URL}v1/recordings/${recording.id}/audio"))
            player.prepare()
            mediaLoaded = true
        }
        player.seekTo(positionMs.coerceAtLeast(0))
        player.play()
        playbackError.value = null
        updatePlaybackState()
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                updatePlaybackState()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playbackError.value = "오디오를 재생할 수 없습니다. 네트워크 연결과 오디오 파일을 확인해 주세요."
                updatePlaybackState()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player) {
        while (isActive) {
            updatePlaybackState()
            delay(250)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Column {
                        Text(recording.title, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${recording.createdAt} · ${recording.duration} · ${recording.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "목록으로 돌아가기")
                        Text("목록")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PlaybackControls(
                audioAvailable = recording.audioAvailable,
                playbackState = playbackState,
                onTogglePlayback = {
                    if (player.isPlaying) {
                        player.pause()
                    } else if (mediaLoaded) {
                        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
                        player.play()
                    } else {
                        playFrom(0)
                    }
                    updatePlaybackState()
                },
            )
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Skim") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("근거") })
            }
            when (selectedTab) {
                0 -> SummaryTab(
                    recording = recording,
                    onSourceClick = { source ->
                        highlightedChunkId = source.chunkId
                        selectedTab = 1
                        playFrom(source.startMs)
                    },
                )
                1 -> TranscriptTab(recording = recording, highlightedChunkId = highlightedChunkId)
            }
        }
    }
}

@Composable
private fun PlaybackControls(
    audioAvailable: Boolean,
    playbackState: PlaybackUiState,
    onTogglePlayback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("근거 오디오", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (!audioAvailable) {
                Text(
                    "이 녹음에는 재생 가능한 오디오가 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val progress = playbackState.durationMs
                    ?.takeIf { it > 0 }
                    ?.let { (playbackState.positionMs.toFloat() / it).coerceIn(0f, 1f) }
                    ?: 0f
                Text(
                    "${formatPlaybackTime(playbackState.positionMs)} / ${playbackState.durationMs?.let(::formatPlaybackTime) ?: "--:--"}",
                    style = MaterialTheme.typography.labelLarge,
                )
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Button(onClick = onTogglePlayback, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (playbackState.isPlaying) "일시정지" else "근거 듣기")
                }
            }
            playbackState.errorMessage?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun formatPlaybackTime(millis: Long): String = "%02d:%02d".format(millis.coerceAtLeast(0) / 60_000, (millis.coerceAtLeast(0) / 1_000) % 60)

@Composable
private fun SummaryTab(recording: Recording, onSourceClick: (SummarySource) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("한 줄 요약", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(recording.oneLineSummary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("아래 timestamp를 누르면 해당 원문 근거가 열립니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        items(recording.summaryItems, key = { it.id }) { item -> SummaryCard(item = item, onSourceClick = onSourceClick) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCard(item: SummaryItem, onSourceClick: (SummarySource) -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(item.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(item.text, style = MaterialTheme.typography.bodyLarge)
            Text("원문 근거", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.sources.forEach { source ->
                    AssistChip(
                        onClick = { onSourceClick(source) },
                        modifier = Modifier.semantics { contentDescription = "근거 듣기 ${source.label}" },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(source.label)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TranscriptTab(recording: Recording, highlightedChunkId: String?) {
    val listState = rememberLazyListState()
    val highlightedIndex = recording.transcriptChunks.indexOfFirst { it.id == highlightedChunkId }

    LaunchedEffect(highlightedChunkId, highlightedIndex) {
        if (highlightedIndex >= 0) listState.animateScrollToItem(highlightedIndex + 1)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            val message = if (highlightedChunkId == null) {
                "요약 탭의 timestamp를 누르면 해당 원문 구간이 여기에서 강조됩니다."
            } else {
                "선택한 timestamp의 원문 근거입니다. 강조된 카드를 확인하세요."
            }
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
        items(recording.transcriptChunks, key = { it.id }) { chunk ->
            TranscriptChunkCard(chunk = chunk, highlighted = chunk.id == highlightedChunkId)
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun TranscriptChunkCard(chunk: TranscriptChunk, highlighted: Boolean) {
    val targetColor = if (highlighted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val containerColor by animateColorAsState(targetValue = targetColor, label = "chunk-highlight")
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { if (highlighted) stateDescription = "선택한 근거" },
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(chunk.timeRange, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (highlighted) {
                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary) {
                        Text(
                            "선택한 근거",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(chunk.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun MainScreenPreview() {
    SkimTheme {
        MainScreen(uiState = SkimUiState.Content(emptyList()), onRefresh = {})
    }
}
