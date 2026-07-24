package com.aistudio.hiromant.kxsrwa.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticBronze
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticGold
import com.aistudio.hiromant.kxsrwa.ui.theme.MysticGoldGlow

// Компонент: Мистический крупный заголовок с автоматическим подбором размера шрифта под ширину экрана
@Composable
fun MysticHeader(
    text: String, // Отображаемый текст заголовка
    modifier: Modifier = Modifier, // Дополнительные модификаторы
    textAlign: TextAlign = TextAlign.Center // Выравнивание текста
) {
    val initialSize = remember(text) {
        when {
            text.length > 18 -> 20.sp
            text.length > 14 -> 24.sp
            text.length > 10 -> 28.sp
            else -> 32.sp
        }
    }
    // Хранение текущего размера шрифта в состоянии для динамического масштабирования
    var fontSize by remember(text) { mutableStateOf(initialSize) }
    // Состояние готовности текста к отрисовке (чтобы избежать мерцания в момент расчета ширины)
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text, // Передаем текст
        style = MaterialTheme.typography.displayLarge.copy(
            color = MysticGold, // Золотистый цвет
            fontSize = fontSize, // Текущий динамический размер шрифта
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        ),
        textAlign = textAlign, // Способ выравнивания
        maxLines = 1, // Ограничение в одну строчку
        softWrap = false, // Отключение автоматического переноса слов
        overflow = TextOverflow.Ellipsis, // Обрезка с эллипсисом при нехватке места
        onTextLayout = { textLayoutResult ->
            // Если текст выходит за видимые границы (переполняет контейнер)
            if (textLayoutResult.hasVisualOverflow) {
                val nextSize = fontSize.value - 1.5f // Уменьшаем шаг шрифта на 1.5sp
                if (nextSize >= 12f) { // Контроль минимального размера
                    fontSize = nextSize.sp // Обновляем состояние для следующего прохода recomposition
                } else {
                    readyToDraw = true // Если дошли до предела, разрешаем рисовать как есть
                }
            } else {
                readyToDraw = true // Если всё поместилось, разрешаем мгновенную отрисовку
            }
        },
        modifier = modifier
            .fillMaxWidth() // Растягивание на всю ширину
            .padding(horizontal = 8.dp, vertical = 12.dp) // Внутренний вертикальный отступ
            .drawWithContent {
                if (readyToDraw) drawContent() // Отрисовываем контент только после успешного расчета размеров
            }
    )
}

// Компонент: Приглушенный мистический подзаголовок
@Composable
fun MysticSubtitle(
    text: String, // Отображаемый текст подзаголовка
    modifier: Modifier = Modifier, // Дополнительные модификаторы
    textAlign: TextAlign = TextAlign.Center // Выравнивание текста
) {
    Text(
        text = text, // Передаем текст подзаголовка
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFA0A0B0), // Приглушенный серо-голубой цвет ауры
            fontFamily = FontFamily.Serif // Шрифт с засечками
        ),
        textAlign = textAlign, // Настройка выравнивания текста
        modifier = modifier
            .fillMaxWidth() // Растягивание подзаголовка по ширине родителя
            .padding(horizontal = 16.dp, vertical = 4.dp) // Стандартные отступы по краям
    )
}

// Компонент: Фирменная мистическая кнопка с тактильным эффектом сжатия при нажатии
@Composable
fun MysticButton(
    text: String, // Текст внутри кнопки
    onClick: () -> Unit, // Callback-лямбда при клике на кнопку
    modifier: Modifier = Modifier, // Дополнительные модификаторы
    isSecondary: Boolean = false, // Является ли кнопка второстепенной (бронзовая рамка вместо залитого золота)
    enabled: Boolean = true, // Активна ли кнопка для взаимодействия
    height: androidx.compose.ui.unit.Dp = 54.dp // Стандартная высота кнопки (не менее 48dp по стандартам доступности)
) {
    // Состояние нажатия кнопки для анимации масштабирования
    var isPressed by remember { mutableStateOf(false) }
    // Плавная анимация изменения масштаба при зажатии пальцем
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f, // Сжатие до 95% при нажатии
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), // Эффект пружины
        label = "ButtonScale" // Название анимации для отладки
    )

    // Вычисление обводки границы в зависимости от типа кнопки (основная или контурная)
    val borderStroke = if (isSecondary) {
        BorderStroke(1.dp, MysticGold.copy(0.4f)) // Тонкая полупрозрачная обводка
    } else {
        BorderStroke(1.5.dp, MysticGold) // Плотная сияющая золотая рамка
    }

    // Вычисление фонового цвета контейнера кнопки
    val containerColor = if (isSecondary) {
        Color(0xFF1C1A17) // Темная бронзовая поверхность
    } else {
        MysticGold // Золотая заливка для главной кнопки действия
    }

    // Вычисление цвета текста на кнопке
    val contentColor = if (isSecondary) {
        Color.White // Белые буквы для темного фона
    } else {
        Color.Black // Черные контрастные буквы для золотого фона
    }

    OutlinedButton(
        onClick = onClick, // Навешиваем событие клика
        enabled = enabled, // Задаем доступность кнопки
        shape = RoundedCornerShape(16.dp), // Округлые углы по дизайну Material 3
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) containerColor else Color(0x11888888), // Приглушенный фон при блокировке
            contentColor = if (enabled) contentColor else Color.Gray // Серые буквы при блокировке
        ),
        // Рамка выключается при залитой кнопке и включается при контурной
        border = if (enabled) (if (isSecondary) borderStroke else BorderStroke(0.dp, Color.Transparent)) else BorderStroke(1.dp, Color.Gray),
        modifier = modifier
            .scale(scale) // Применение масштабирования сжатия
            .height(height) // Высота кнопки
            .pointerInput(Unit) {
                // Детектор касаний для отслеживания удержания пальца (тактильный фидбек)
                detectTapGestures(
                    onPress = {
                        isPressed = true // Палец опущен — сжимаем кнопку
                        tryAwaitRelease() // Ждем, пока пользователь отпустит палец
                        isPressed = false // Палец отпущен — возвращаем масштаб
                    }
                )
            },
        contentPadding = PaddingValues(horizontal = 24.dp) // Внутренние горизонтальные отступы для текста
    ) {
        Text(
            text = text, // Текст кнопки
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.sp, // Разреженный межсимвольный интервал для солидности
                fontWeight = FontWeight.Bold, // Жирный шрифт
                color = if (enabled) contentColor else Color.Gray // Цвет шрифта в зависимости от активности
            ),
            maxLines = 1, // Текст кнопки строго в одну строчку
            overflow = TextOverflow.Ellipsis // Троеточие при переполнении
        )
    }
}

// Компонент: Элегантная темная карточка с тонкой золотой аурой-границей
@Composable
fun MysticCard(
    modifier: Modifier = Modifier.padding(bottom = 12.dp), // Дефолтный отступ снизу
    content: @Composable ColumnScope.() -> Unit // Дочерние компоненты, располагающиеся внутри карточки
) {
    Card(
        shape = RoundedCornerShape(16.dp), // Скругление углов карточки
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141210) // Глубокий темно-коричневый фон
        ),
        border = BorderStroke(1.dp, MysticGold.copy(0.2f)), // Тонкая золотая каемка-аура с 20% непрозрачностью
        modifier = Modifier
            .fillMaxWidth() // Растягивание на всю ширину экрана
            .then(modifier), // Применение внешних переданных модификаторов
        content = content // Отрисовка вложенных компонентов
    )
}

// Компонент: Мистическое поле текстового ввода с золотистой подсветкой контура
@Composable
fun MysticTextField(
    value: String, // Текущее текстовое значение в поле
    onValueChange: (String) -> Unit, // Callback-лямбда при вводе/изменении символов
    label: String, // Подпись над полем ввода
    modifier: Modifier = Modifier, // Дополнительные модификаторы
    error: String? = null, // Описание ошибки валидации (если есть)
    placeholder: String = "", // Текст-подсказка внутри пустого поля
    readOnly: Boolean = false, // Флаг «Только для чтения»
    trailingIcon: @Composable (() -> Unit)? = null // Иконка в правом углу поля ввода
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Подпись над полем ввода
        Text(
            text = label, // Название поля
            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold), // Золотистый цвет текста подписи
            modifier = Modifier.padding(bottom = 6.dp) // Нижний отступ перед полем
        )
        OutlinedTextField(
            value = value, // Текст
            onValueChange = onValueChange, // Обработчик изменений
            placeholder = { Text(placeholder, color = Color.Gray, style = MaterialTheme.typography.bodyMedium) }, // Заглушка
            singleLine = true, // Ввод в одну строку
            isError = error != null, // Переключаем режим ошибки при наличии сообщения
            readOnly = readOnly, // Режим только для чтения
            trailingIcon = trailingIcon, // Правая иконка
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, // Белый цвет текста при фокусе
                unfocusedTextColor = Color.White, // Белый цвет текста без фокуса
                focusedBorderColor = MysticGold, // Золотая граница при фокусе
                unfocusedBorderColor = MysticBronze.copy(0.6f), // Бронзовая полупрозрачная граница без фокуса
                cursorColor = MysticGold, // Золотой курсор ввода
                errorBorderColor = Color(0xFFCF6679) // Красноватый цвет рамки при ошибке
            ),
            shape = RoundedCornerShape(12.dp), // Углы скругления текстового поля
            modifier = Modifier.fillMaxWidth() // Поле занимает всю ширину колонки
        )
        // Если зафиксирована ошибка валидации
        if (error != null) {
            Text(
                text = error, // Сообщение об ошибке
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCF6679)), // Красный шрифт предупреждения
                modifier = Modifier.padding(top = 4.dp, start = 4.dp) // Верхний отступ сообщения
            )
        }
    }
}

// Компонент: Анимированный пульсирующий круг с сияющей золотой границей для сканирования ладони
@Composable
fun GlowingBorderCircle(
    size: Dp, // Физический диаметр круга
    modifier: Modifier = Modifier, // Дополнительные модификаторы
    content: @Composable BoxScope.() -> Unit // Дочерние элементы внутри круга
) {
    // Бесконечная анимация для эффекта мягкой пульсации ауры
    val infiniteTransition = rememberInfiniteTransition(label = "CircleGlow")
    // Колебание непрозрачности свечения от 30% до 80%
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, // Минимальная яркость
        targetValue = 0.8f, // Максимальная яркость
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Время одного цикла пульсации 1.5 секунды
            repeatMode = RepeatMode.Reverse // Возвратное движение анимации (туда-обратно)
        ),
        label = "PulseAlpha" // Лейбл анимации
    )

    Box(
        contentAlignment = Alignment.Center, // Центрируем содержимое круга
        modifier = modifier
            .size(size) // Применяем заданный размер
            .drawBehind {
                // Отрисовываем первую тонкую статичную золотую рамку по контуру
                drawCircle(
                    color = MysticGold,
                    radius = (size.toPx() / 2) + 2.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Отрисовываем внешнее мягкое пульсирующее облако ауры золотого цвета
                drawCircle(
                    color = MysticGold.copy(alpha = pulseAlpha * 0.2f), // Динамическая альфа
                    radius = (size.toPx() / 2) + 6.dp.toPx(), // Радиус чуть больше основного круга
                    style = Stroke(width = 4.dp.toPx()) // Толстый размытый контур
                )
            }
            .clip(RoundedCornerShape(size / 2)) // Обрезаем по краям круга
            .clickable { } // Навешиваем пустое событие клика
    ) {
        content() // Отрисовка внутреннего содержимого круга
    }
}
