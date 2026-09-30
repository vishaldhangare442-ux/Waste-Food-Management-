package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class FoodPreset(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val gradientColors: List<Color>,
    val emoji: String,
    val sampleDescription: String
)

val FOOD_PRESETS = listOf(
    FoodPreset(
        id = "BUFFET",
        name = "Hot Buffet Meals",
        icon = Icons.Default.DinnerDining,
        primaryColor = Color(0xFFD97706),
        gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
        emoji = "🍲",
        sampleDescription = "Cooked rice, curry, pasta & hot trays"
    ),
    FoodPreset(
        id = "BAKERY",
        name = "Bakery & Breads",
        icon = Icons.Default.BakeryDining,
        primaryColor = Color(0xFFB45309),
        gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
        emoji = "🥖",
        sampleDescription = "Sourdough, baguettes, buns & croissants"
    ),
    FoodPreset(
        id = "PRODUCE",
        name = "Fresh Produce",
        icon = Icons.Default.LocalFlorist,
        primaryColor = Color(0xFF16A34A),
        gradientColors = listOf(Color(0xFF22C55E), Color(0xFF15803D)),
        emoji = "🥗",
        sampleDescription = "Vegetables, fruits & fresh salad greens"
    ),
    FoodPreset(
        id = "DAIRY",
        name = "Dairy & Eggs",
        icon = Icons.Default.Egg,
        primaryColor = Color(0xFF0284C7),
        gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
        emoji = "🥛",
        sampleDescription = "Milk, yogurt, cheese & fresh eggs"
    ),
    FoodPreset(
        id = "COOKED_MEAL",
        name = "Prepared Meals",
        icon = Icons.Default.Restaurant,
        primaryColor = Color(0xFFE11D48),
        gradientColors = listOf(Color(0xFFF43F5E), Color(0xFFBE123C)),
        emoji = "🍱",
        sampleDescription = "Packed meal boxes, entrees & sandwiches"
    ),
    FoodPreset(
        id = "PANTRY",
        name = "Packaged Groceries",
        icon = Icons.Default.ShoppingBag,
        primaryColor = Color(0xFF7C3AED),
        gradientColors = listOf(Color(0xFFA855F7), Color(0xFF6D28D9)),
        emoji = "🥫",
        sampleDescription = "Canned goods, grains, pasta & cereals"
    )
)

@Composable
fun FoodPhotoThumbnail(
    photoUri: String,
    presetName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val preset = FOOD_PRESETS.find { it.id.equals(presetName, ignoreCase = true) }
        ?: FOOD_PRESETS.first()

    if (photoUri.isNotBlank()) {
        AsyncImage(
            model = Uri.parse(photoUri),
            contentDescription = "Food Photo",
            modifier = modifier.clip(RoundedCornerShape(12.dp)),
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(preset.gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = preset.emoji,
                    fontSize = 28.sp
                )
                Text(
                    text = preset.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FoodPresetSelector(
    selectedPresetId: String,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Or choose a food category preset photo:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FOOD_PRESETS) { preset ->
                val isSelected = selectedPresetId == preset.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) preset.primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) preset.primaryColor else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectPreset(preset.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = preset.emoji, fontSize = 16.sp)
                        Text(
                            text = preset.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) preset.primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
