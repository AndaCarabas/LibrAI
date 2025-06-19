package com.example.librai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.librai.models.BookInfo
import com.example.librai.ui.theme.PrimaryColor

@Composable
fun RecommendationsSection(
    recs: List<BookInfo>,
    isLoading: Boolean,
    onFetch: () -> Unit
) {
    // ▶ expand/collapse state
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        // Header row with title and chevron
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    if (!expanded) {
                        onFetch()     // fire API only on first expand
                    }
                    expanded = !expanded
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                color = PrimaryColor
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = PrimaryColor
            )
        }

        // Animated show/hide of the content
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit  = fadeOut()
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (recs.isEmpty()) {
                Text(
                    "No recommendations available.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recs.forEach { book ->
                        RecommendationCard(book)
                    }
                }
            }
        }
    }
}