package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.mycamera.R
import com.hinnka.mycamera.model.FilmRecipe
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraBodySurface
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite

@Composable
fun RecipeDrawer(
    recipes: List<FilmRecipe>,
    activeRecipe: FilmRecipe,
    onSelectRecipe: (FilmRecipe) -> Unit,
    onOpenRecipeEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CameraBodyDark.copy(alpha = 0.95f))
            .padding(vertical = 6.dp)
            .testTag("recipe_drawer")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recipes_title).uppercase(),
                color = CameraOsdGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "+ ${stringResource(R.string.create_new_recipe)}",
                color = CameraAccentAmber,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onOpenRecipeEditor)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("button_new_recipe")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            recipes.forEach { recipe ->
                val isSelected = recipe.id == activeRecipe.id
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) CameraAccentAmber.copy(alpha = 0.2f) else CameraBodySurface
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CameraAccentAmber else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectRecipe(recipe) }
                        .padding(horizontal = 12.dp)
                        .testTag("recipe_item_${recipe.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recipe.name,
                        color = if (isSelected) CameraAccentAmber else CameraOsdWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
