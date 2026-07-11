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

@Composable
fun MysticHeader(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge.copy(
            color = MysticGold,
            fontSize = 34.sp
        ),
        textAlign = textAlign,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

@Composable
fun MysticSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFA0A0B0),
            fontFamily = FontFamily.Serif
        ),
        textAlign = textAlign,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
fun MysticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false,
    enabled: Boolean = true,
    height: androidx.compose.ui.unit.Dp = 54.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ButtonScale"
    )

    val borderStroke = if (isSecondary) {
        BorderStroke(1.dp, MysticGold.copy(0.4f))
    } else {
        BorderStroke(1.5.dp, MysticGold)
    }

    val containerColor = if (isSecondary) {
        Color(0xFF1C1A17)
    } else {
        MysticGold
    }

    val contentColor = if (isSecondary) {
        Color.White
    } else {
        Color.Black
    }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) containerColor else Color(0x11888888),
            contentColor = if (enabled) contentColor else Color.Gray
        ),
        border = if (enabled) (if (isSecondary) borderStroke else BorderStroke(0.dp, Color.Transparent)) else BorderStroke(1.dp, Color.Gray),
        modifier = modifier
            .scale(scale)
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) contentColor else Color.Gray
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MysticCard(
    modifier: Modifier = Modifier.padding(bottom = 12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141210)
        ),
        border = BorderStroke(1.dp, MysticGold.copy(0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        content = content
    )
}

@Composable
fun MysticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String = ""
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = MysticGold),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray, style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MysticGold,
                unfocusedBorderColor = MysticBronze.copy(0.6f),
                cursorColor = MysticGold,
                errorBorderColor = Color(0xFFCF6679)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCF6679)),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun GlowingBorderCircle(
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CircleGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(
                    color = MysticGold,
                    radius = (size.toPx() / 2) + 2.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = MysticGold.copy(alpha = pulseAlpha * 0.2f),
                    radius = (size.toPx() / 2) + 6.dp.toPx(),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(size / 2))
            .clickable { }
    ) {
        content()
    }
}
