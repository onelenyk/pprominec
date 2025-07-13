package dev.onelenyk.pprominec.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    value: String,
    isRequired: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: String = "",
    placeholder: String = "",
    onValueChange: (String) -> Unit,
    decorationPadding: PaddingValues = PaddingValues(),
    textStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    labelTextStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    requiredTextStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.error,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    placeholderTextStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.W400,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    radius: Dp = 12.dp,
    leftContent: @Composable (() -> Unit)? = null,
    rightContent: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val showLabelAbove = label.isNotEmpty() && isFocused

    // Convert string to TextFieldValue with caret at end when focused
    val textFieldValue = remember(value, isFocused) {
        TextFieldValue(
            text = value,
            selection = if (isFocused && value.isNotEmpty()) {
                TextRange(value.length)
            } else {
                TextRange(0)
            }
        )
    }

    BasicTextField(
        modifier = modifier,
        value = textFieldValue,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        onValueChange = { newValue ->
            onValueChange(newValue.text)
        },
        textStyle = textStyle,
        decorationBox = { innerTextField ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(decorationPadding)
                    .clip(RoundedCornerShape(radius))
                    .border(
                        width = 0.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(radius),
                    )
                    .background(backgroundColor)
                    .padding(8.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (leftContent != null) {
                        Box(modifier = Modifier.padding(end = 6.dp)) {
                            leftContent()
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            else -> {
                                Column {
                                    Row {
                                        Text(
                                            text = label,
                                            style = labelTextStyle,
                                        )
                                        if (isRequired) {
                                            Text(
                                                text = "*",
                                                style = requiredTextStyle,
                                            )
                                        }
                                    }
                                    Box() {
                                        innerTextField()
                                        if (value.isEmpty()) {
                                            Text(
                                                text = placeholder,
                                                style = placeholderTextStyle,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (rightContent != null) {
                        Box(modifier = Modifier.padding(start = 6.dp)) {
                            rightContent()
                        }
                    }
                }
            }
        },
    )
} 