package pl.soulsnaps.features.memoryhub.timeline

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.Uri
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.components.FullScreenCircularProgress
import pl.soulsnaps.components.PrimaryButton
import pl.soulsnaps.designsystem.DsIcons
import pl.soulsnaps.domain.model.Memory
import pl.soulsnaps.features.memoryhub.timeline.TimelineViewModel
import pl.soulsnaps.utils.formatDate
import pl.soulsnaps.utils.toLocalDateTime

@Composable
internal fun TimelineRoute(
    onMemoryDetailsClick: (Int) -> Unit,
    viewModel: TimelineViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TimelineScreen(uiState = uiState, onSnapClick = onMemoryDetailsClick)
}

@Composable
fun TimelineScreen(
    uiState: TimelineViewModel.TimelineUiState,
    onSnapClick: (Int) -> Unit
) {
    when {
        uiState.isLoading -> {
            FullScreenCircularProgress()
        }

        uiState.snaps.isEmpty() -> {
            EmptyTimelineView()
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                uiState.quoteOfTheDay?.let { quote ->
                    item {
                        QuoteOfTheDayCard(quote = quote)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Oś czasu z pogrupowanymi Snapami
                uiState.snaps.groupBy {
                    it.createdAt.toLocalDateTime()
                }.forEach { (date, snaps) ->
                    item {
                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(snaps) { snap ->
                        SnapItem(snap = snap, onClick = { onSnapClick(snap.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTimelineView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Brak zapisanych chwil",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SnapItem(
    snap: Memory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(snap.photoUri),
                contentDescription = "Snap Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = snap.description, style = MaterialTheme.typography.bodyLarge)
                snap.mood?.let {
                    Text(text = it.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}


//@Composable
//internal fun TimelineScreen1(
//    viewModel: TimelineViewModel = koinViewModel(),
//    onMemoryDetailsClick: (Int) -> Unit,
//    onAddMemoryClick: () -> Unit,
//) {
//    val memories by viewModel.state.collectAsState()
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        if (memories.snaps.isEmpty()) {
//            EmptyTimelineScreen(onAddMemoryClick)
//        } else {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//            ) {
//                Column(modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        "Quote of the Day",
//                        style = MaterialTheme.typography.titleSmall,
//                        color = Gray,
//                        modifier = Modifier.padding(top = 16.dp)
//                    )
//                    Text(
//                        "Easy decisions, hard life. Hard decisions, easy life.",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer,
//                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
//                    )
//                }
//                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
//                    val groupedMemories = memories.groupBy { it.createdAt }
//                    groupedMemories.forEach { (date, items) ->
//                        item { SectionTitle(title = DateUtils.millisToFormattedDate(date)) }
//                        items(items) { memory ->
//                            MemoryItem(memory.title, memory.description, memory.photoUri)
//                            //MemoryCard(memory)
//                        }
//                    }
//                }
//                SmallFloatingActionButton(
//                    onClick = { onAddMemoryClick() },
//                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                    contentColor = MaterialTheme.colorScheme.secondary
//                ) {
//                    Icon(Icons.Filled.Add, "Small floating action button.")
//                }
//            }
//        }
//    }
//}

@Composable
fun EmptyTimelineScreen(onAddMemoryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = "Your memories",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Welcome to your personal memory space. You can add photos, notes, and even tracks of your mood. Memories are private and only you can see them.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)

                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = DsIcons.Add(),
                contentDescription = "Memory Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton("Let's add a memory") {
            onAddMemoryClick.invoke()
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun MemoryCard(memory: Memory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = memory.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = memory.description.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AsyncImage(
                model = memory.photoUri,
                contentDescription = "Memory Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MemoryItem(title: String, description: String?, imageUrl: Uri?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(top = 8.dp, bottom = 8.dp)

    ) {
        Column(
            modifier = Modifier
                .weight(4f)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = description.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
//            Button(
//                onClick = { /* TODO: Edit action */ },
//                modifier = Modifier.padding(top = 8.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEBEBF0))
//            ) {
//                Text("Edit")
//            }
        }
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun QuoteOfTheDayCard(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧘 Cytat dnia",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}