package com.example.skim.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.runtime.DisposableEffect
import com.example.skim.BuildConfig
import com.example.skim.model.Recording
import com.example.skim.model.SummaryItem
import com.example.skim.model.SummarySource
import com.example.skim.model.TranscriptChunk
import com.example.skim.theme.SkimTheme

private enum class SkimScreen { List, Detail }
private enum class MainDestination(val label: String) { Home("홈"), Add("추가"), Library("라이브러리"), Settings("설정") }

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
                title = {
                    Column {
                        Text("Skim", fontWeight = FontWeight.Bold)
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
                        icon = { Text(item.icon) },
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
                    if (visibleRecordings.isEmpty()) item { Text("아직 저장된 녹음이 없습니다.") }
                    items(visibleRecordings, key = { it.id }) { recording ->
                        RecordingCard(recording = recording, onClick = { onRecordingClick(recording) })
                    }
                }
                MainDestination.Add -> item {
                    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("음성 파일을 선택해 업로드하세요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("업로드 후 처리 상태와 결과가 자동으로 갱신됩니다.", style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onPickAudio) { Text("파일 선택 후 업로드") }
                        }
                    }
                }
                MainDestination.Settings -> item {
                    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
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

private val MainDestination.icon: String
    get() = when (this) {
        MainDestination.Home -> "⌂"
        MainDestination.Add -> "+"
        MainDestination.Library -> "≡"
        MainDestination.Settings -> "i"
    }

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("요약을 믿기 전에, 근거를 확인하세요", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Skim은 요약 항목의 timestamp를 원문 구간에 연결합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun RecordingCard(recording: Recording, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusDot()
                Text(recording.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("· ${recording.duration}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(recording.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(recording.oneLineSummary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            recording.audioAssetName?.let { assetName ->
                Text(
                    text = "내장 오디오 · $assetName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(recording.createdAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Text("근거 ${recording.transcriptChunks.size}개 ›", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
    val context = LocalContext.current
    val player = remember(recording.id) { ExoPlayer.Builder(context).build() }
    DisposableEffect(player) { onDispose { player.release() } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
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
                navigationIcon = { TextButton(onClick = onBack) { Text("‹  목록") } },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("요약") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("원문") })
            }
            when (selectedTab) {
                0 -> SummaryTab(
                    recording = recording,
                    onSourceClick = { source ->
                        highlightedChunkId = source.chunkId
                        selectedTab = 1
                        if (recording.audioAvailable) {
                            player.setMediaItem(MediaItem.fromUri("${BuildConfig.SKIM_BASE_URL}v1/recordings/${recording.id}/audio"))
                            player.prepare()
                            player.seekTo(source.startMs)
                            player.play()
                        }
                    },
                )
                1 -> TranscriptTab(recording = recording, highlightedChunkId = highlightedChunkId)
            }
        }
    }
}

@Composable
private fun SummaryTab(recording: Recording, onSourceClick: (SummarySource) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
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
    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(item.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(item.text, style = MaterialTheme.typography.bodyLarge)
            Text("원문 근거", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.sources.forEach { source ->
                    AssistChip(onClick = { onSourceClick(source) }, label = { Text("▶ ${source.label}") })
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
        modifier = Modifier.fillMaxWidth(),
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
    SkimTheme(dynamicColor = false) {
        MainScreen(uiState = SkimUiState.Content(emptyList()), onRefresh = {})
    }
}
