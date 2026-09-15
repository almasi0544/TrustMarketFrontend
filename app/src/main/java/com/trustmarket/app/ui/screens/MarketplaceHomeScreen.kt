package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.CategoryOut
import com.trustmarket.app.network.ProductOut
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.ui.components.RiskBadge

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun MarketplaceHomeScreen(token: String?) {
    var products by remember { mutableStateOf<List<ProductOut>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryOut>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            products = RetrofitClient.api.getProducts()
            categories = RetrofitClient.api.getCategories()
        } catch (e: Exception) {
            error = "Failed to load: ${e.message}"
        } finally {
            loading = false
        }
    }

    val filtered = products.filter { p ->
        (selectedCategory == null || p.category_id == selectedCategory) &&
                (searchQuery.isBlank() || p.title.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(20.dp)) {
            Text("✦ TrustMarket", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("Risk-rated marketplace", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search listings...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All") })
            Spacer(Modifier.width(8.dp))
            categories.forEach { cat ->
                FilterChip(selected = selectedCategory == cat.id, onClick = { selectedCategory = cat.id }, label = { Text(cat.name) })
                Spacer(Modifier.width(8.dp))
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("No products found") }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered) { product -> ProductCard(product) }
            }
        }

        NavigationBar {
            NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏪") }, label = { Text("Market") })
            NavigationBarItem(selected = false, onClick = {}, icon = { Text("🛒") }, label = { Text("Purchases") })
            NavigationBarItem(selected = false, onClick = {}, icon = { Text("🏬") }, label = { Text("My Shop") })
            NavigationBarItem(selected = false, onClick = {}, icon = { Text("🚩") }, label = { Text("Disputes") })
            NavigationBarItem(selected = false, onClick = {}, icon = { Text("👤") }, label = { Text("Profile") })
        }
    }
}

@Composable
private fun ProductCard(product: ProductOut) {
    var trust by remember { mutableStateOf<Pair<String, Int>?>(null) }

    LaunchedEffect(product.seller_id) {
        try {
            val t = RetrofitClient.api.getSellerTrust(product.seller_id)
            trust = t.risk_level to t.trust_score
        } catch (e: Exception) {
            trust = null
        }
    }

    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(product.title, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("$${product.price}", fontWeight = FontWeight.Bold, color = TrustTeal)
            Spacer(Modifier.height(4.dp))
            trust?.let { (risk, score) -> RiskBadge(riskLevel = risk, trustScore = score) }
        }
    }
}