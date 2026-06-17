package com.workernotfound.app.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppRadius
import com.workernotfound.app.core.designsystem.AppTheme

/** Field label + content wrapper used in forms. */
@Composable
fun LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(text = label, color = AppColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (required) {
                Text(text = " *", color = AppColors.Danger, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/** Single-select pill chips that wrap across rows (style guide: chip 999dp). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = AppTheme.role.accent
    val accentTint = AppTheme.role.accentTint
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) accentTint else AppColors.CardSurface,
                        shape = RoundedCornerShape(AppRadius.pill),
                    )
                    .border(
                        BorderStroke(1.dp, if (selected) accent else AppColors.Border),
                        RoundedCornerShape(AppRadius.pill),
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = label,
                    color = if (selected) accent else AppColors.TextSub,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/** Numeric stepper with min/max bounds (style guide: 둥근 컨트롤). */
@Composable
fun AppStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
    suffix: String = "",
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        StepperButton(symbol = "−", enabled = value > min) { onValueChange((value - 1).coerceAtLeast(min)) }
        Text(
            text = "$value$suffix",
            modifier = Modifier.padding(horizontal = 20.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AppColors.TextMain,
        )
        StepperButton(symbol = "+", enabled = value < max) { onValueChange((value + 1).coerceAtMost(max)) }
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val accent = AppTheme.role.accent
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(if (enabled) accent else AppColors.Border, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = symbol, color = if (enabled) Color.White else AppColors.TextPlaceholder, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
