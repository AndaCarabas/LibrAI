package com.example.librai.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.librai.models.BookInfo
import com.example.librai.R
import com.example.librai.ui.theme.MintBackground
import com.example.librai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationCard(
    book: BookInfo,
    modifier: Modifier = Modifier,
    //onBuy: (BookInfo) -> Unit
) {
    val ctx = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardColors(containerColor = Color.White, contentColor = Color.Black, disabledContentColor = Color.Gray.copy(alpha = 0.6f), disabledContainerColor = Color.White.copy(alpha = 0.8f) )
    ) {
        Row(
            modifier = Modifier
                .clickable { /* maybe navigate to detail */ }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = "Cover of ${book.title}",
                placeholder   = painterResource(R.drawable.book_placeholder),
                error         = painterResource(R.drawable.book_placeholder),
                fallback      = painterResource(R.drawable.book_placeholder),
                modifier      = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale  = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(
//                    onClick = {
//                        Log.d("BookAPI", "BookTitle: ${book.title}, Book buy link: ${book.buyLink}")
//                        if (!book.buyLink.isNullOrBlank()) {
//                            // open the provided link
//                            ctx.startActivity(
//                                Intent(Intent.ACTION_VIEW, Uri.parse(book.buyLink))
//                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            )
//                        } else {
//                            // fallback to Amazon (or whatever)
//                            ctx.buyOnAmazon(book)
//                        }
//                    }
                    onClick = { menuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Buy ${book.title}",
                        tint = PrimaryColor
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MintBackground
                ) {
                    // 1) Amazon
                    DropdownMenuItem(
                        text = { Text("Amazon") },
                        onClick = {
                            menuExpanded = false
                            val query = Uri.encode("${book.title} ${book.author}")
                            ctx.buyOnAmazonWithUrl("https://www.amazon.com/s?k=$query")
                        }
                    )
                    // 2) Google Books
                    DropdownMenuItem(
                        text = { Text("Google Books") },
                        onClick = {
                            menuExpanded = false
                            val query = Uri.encode("${book.title} ${book.author}")
                            ctx.buyOnAmazonWithUrl("https://www.google.com/search?tbm=bks&q=$query")
                        }
                    )
                    // 3) Goodreads
                    DropdownMenuItem(
                        text = { Text("Goodreads") },
                        onClick = {
                            menuExpanded = false
                            val query = Uri.encode("${book.title} ${book.author}")
                            ctx.buyOnAmazonWithUrl("https://www.goodreads.com/search?q=$query")
                        }
                    )
                    // 4) Local library lookup (WorldCat)
                    DropdownMenuItem(
                        text = { Text("Find in Library") },
                        onClick = {
                            menuExpanded = false
                            val query = Uri.encode("${book.title} ${book.author}")
                            ctx.buyOnAmazonWithUrl("https://www.worldcat.org/search?q=$query")
                        }
                    )
                    // 4) Carturesti
                    DropdownMenuItem(
                        text = { Text("Find in Carturesti") },
                        onClick = {
                            menuExpanded = false
                            val query = Uri.encode("${book.title} ${book.author}")
                            ctx.buyOnAmazonWithUrl("https://carturesti.ro/product/search/$query")
                        }
                    )
                }
            }
        }
    }
}

fun Context.buyOnAmazonWithUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(Intent.createChooser(intent, "Open with…"))
}

fun Context.buyOnAmazon(book: BookInfo) {
    val query = Uri.encode("${book.title} ${book.author}")
    val url = Uri.parse("https://www.amazon.com/s?k=$query")
    val intent = Intent(Intent.ACTION_VIEW, url)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val chooser = Intent.createChooser(intent, "Open with…")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    // verify you have an app that can handle it
    if (packageManager.resolveActivity(chooser, 0) != null) {
        startActivity(chooser)
    } else {
        Toast.makeText(this, "No app available to open links", Toast.LENGTH_SHORT).show()
    }
}