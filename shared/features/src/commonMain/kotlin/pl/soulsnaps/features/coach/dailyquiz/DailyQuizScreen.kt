package pl.soulsnaps.features.coach.dailyquiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.soulsnaps.features.coach.model.*
// Removed unused import

/**
 * Daily Emotion Quiz Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuizScreen(
    onBack: () -> Unit = {},
    onCompleted: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: DailyQuizViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted && !state.showReflection) {
            // Quiz completed but no reflection to show - go back
            onCompleted()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (state.isCompleted) "Twoja refleksja" else "Jak się dziś czujesz?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        
        when {
            state.isLoading -> {
                LoadingView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            state.errorMessage != null -> {
                ErrorView(
                    message = state.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.handleIntent(DailyQuizIntent.LoadQuiz) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            state.showReflection && state.quiz?.aiReflection != null -> {
                val reflection = state.quiz?.aiReflection
                if (reflection != null) {
                    ReflectionView(
                        reflection = reflection,
                    onClose = { 
                        viewModel.handleIntent(DailyQuizIntent.CloseReflection)
                        onCompleted()
                    },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
            
            state.isCompleted -> {
                CompletedView(
                    onViewReflection = { viewModel.handleIntent(DailyQuizIntent.ViewReflection) },
                    onClose = onCompleted,
                    hasReflection = state.quiz?.aiReflection != null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            else -> {
                QuizView(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun LoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Przygotowuję quiz...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "😔",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Spróbuj ponownie")
            }
        }
    }
}

@Composable
private fun QuizView(
    state: DailyQuizState,
    viewModel: DailyQuizViewModel,
    modifier: Modifier = Modifier
) {
    val currentQuestion = viewModel.getCurrentQuestion()
    
    if (currentQuestion == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("Brak pytań w quizie")
        }
        return
    }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Progress indicator
        QuizProgressIndicator(
            progress = viewModel.getProgress(),
            currentQuestion = state.currentQuestionIndex + 1,
            totalQuestions = state.quiz?.questions?.size ?: 0
        )
        
        // Question
        QuestionCard(
            question = currentQuestion,
            answer = viewModel.getAnswerForCurrentQuestion(),
            onAnswerChanged = { answer ->
                viewModel.handleIntent(
                    DailyQuizIntent.AnswerQuestion(currentQuestion.id, answer)
                )
            },
            modifier = Modifier.weight(1f)
        )
        
        // Navigation buttons
        QuizNavigationButtons(
            canGoBack = !viewModel.isFirstQuestion(),
            canGoForward = viewModel.canProceed(),
            isLastQuestion = viewModel.isLastQuestion(),
            isSubmitting = state.isSubmitting,
            onBack = { viewModel.handleIntent(DailyQuizIntent.PreviousQuestion) },
            onNext = { viewModel.handleIntent(DailyQuizIntent.NextQuestion) },
            onSubmit = { viewModel.handleIntent(DailyQuizIntent.SubmitQuiz) }
        )
    }
}

@Composable
private fun QuizProgressIndicator(
    progress: Float,
    currentQuestion: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pytanie $currentQuestion z $totalQuestions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun QuestionCard(
    question: EmotionQuizQuestion,
    answer: QuizAnswer?,
    onAnswerChanged: (QuizAnswer) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Question text
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Answer input based on question type
            when (question.type) {
                QuestionType.EMOTION_SCALE -> {
                    ScaleQuestionInput(
                        options = question.options,
                        selectedValue = answer?.scaleValue,
                        onValueSelected = { value ->
                            onAnswerChanged(
                                QuizAnswer(
                                    questionId = question.id,
                                    scaleValue = value
                                )
                            )
                        }
                    )
                }
                
                QuestionType.EMOJI_SELECT -> {
                    EmojiQuestionInput(
                        options = question.options,
                        selectedOptions = answer?.selectedOptions ?: emptyList(),
                        onSelectionChanged = { selectedIds ->
                            onAnswerChanged(
                                QuizAnswer(
                                    questionId = question.id,
                                    selectedOptions = selectedIds
                                )
                            )
                        }
                    )
                }
                
                QuestionType.MULTIPLE_CHOICE -> {
                    MultipleChoiceInput(
                        options = question.options,
                        selectedOptions = answer?.selectedOptions ?: emptyList(),
                        onSelectionChanged = { selectedIds ->
                            onAnswerChanged(
                                QuizAnswer(
                                    questionId = question.id,
                                    selectedOptions = selectedIds
                                )
                            )
                        }
                    )
                }
                
                QuestionType.MULTIPLE_SELECT -> {
                    MultipleSelectInput(
                        options = question.options,
                        selectedOptions = answer?.selectedOptions ?: emptyList(),
                        onSelectionChanged = { selectedIds ->
                            onAnswerChanged(
                                QuizAnswer(
                                    questionId = question.id,
                                    selectedOptions = selectedIds
                                )
                            )
                        }
                    )
                }
                
                QuestionType.TEXT_INPUT -> {
                    TextInputField(
                        value = answer?.textAnswer ?: "",
                        onValueChanged = { text ->
                            onAnswerChanged(
                                QuizAnswer(
                                    questionId = question.id,
                                    textAnswer = text
                                )
                            )
                        },
                        placeholder = "Wpisz swoją odpowiedź..."
                    )
                }
            }
        }
    }
}

@Composable
private fun ScaleQuestionInput(
    options: List<QuizOption>,
    selectedValue: Int?,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scale values
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(options) { option ->
                val value = option.value ?: 1
                val isSelected = selectedValue == value
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .clickable { onValueSelected(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        // Scale labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = options.firstOrNull()?.text ?: "1",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = options.lastOrNull()?.text ?: "10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmojiQuestionInput(
    options: List<QuizOption>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options.chunked(2)) { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    val isSelected = selectedOptions.contains(option.id)
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onSelectionChanged(listOf(option.id))
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 4.dp else 1.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = option.emoji ?: "😐",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Text(
                                text = option.text,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                
                // Fill remaining space if odd number of options
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceInput(
    options: List<QuizOption>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = selectedOptions.contains(option.id)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectionChanged(listOf(option.id))
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    option.emoji?.let { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Wybrane",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultipleSelectInput(
    options: List<QuizOption>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = selectedOptions.contains(option.id)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newSelection = if (isSelected) {
                            selectedOptions - option.id
                        } else {
                            selectedOptions + option.id
                        }
                        onSelectionChanged(newSelection)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    option.emoji?.let { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null, // Handled by card click
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TextInputField(
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun QuizNavigationButtons(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLastQuestion: Boolean,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back button
        if (canGoBack) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Poprzednie")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Next/Submit button
        Button(
            onClick = if (isLastQuestion) onSubmit else onNext,
            enabled = canGoForward && !isSubmitting,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Zapisuję...")
            } else if (isLastQuestion) {
                Text("Zakończ quiz")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Check, contentDescription = null)
            } else {
                Text("Dalej")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun CompletedView(
    onViewReflection: () -> Unit,
    onClose: () -> Unit,
    hasReflection: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Text(
                    text = "Gratulacje!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Ukończyłeś dzisiejszy quiz emocjonalny. Dziękuję za poświęcenie czasu na refleksję nad swoimi uczuciami.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (hasReflection) {
                        Button(
                            onClick = onViewReflection,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Zobacz swoją refleksję")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zamknij")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReflectionView(
    reflection: AIReflection,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "🤖 Twoja AI Refleksja",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Zamknij",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Text(
                        text = reflection.reflectionText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                    )
                }
            }
        }
        
        if (reflection.suggestedAffirmations.isNotEmpty()) {
            item {
                ReflectionSection(
                    title = "💫 Sugerowane afirmacje",
                    items = reflection.suggestedAffirmations
                )
            }
        }
        
        if (reflection.emotionalInsights.isNotEmpty()) {
            item {
                ReflectionSection(
                    title = "🧠 Spostrzeżenia",
                    items = reflection.emotionalInsights
                )
            }
        }
        
        if (reflection.recommendedActions.isNotEmpty()) {
            item {
                ReflectionSection(
                    title = "🎯 Rekomendowane działania",
                    items = reflection.recommendedActions
                )
            }
        }
        
        item {
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Zakończ")
            }
        }
    }
}

@Composable
private fun ReflectionSection(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
