package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.CategoryOut
import com.trustmarket.app.network.ProductOut
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.TrustProfileOut
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.util.formatPrice

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun MarketplaceHomeScreen(
    token: String?,
    onNavigateToProfile: () -> Unit,
    onViewProduct: (Int) -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToMyProducts: () -> Unit,
    onNavigateToDisputes: () -> Unit
) {
    var products by remember { mutableStateOf<List<ProductOut>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryOut>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            categories = RetrofitClient.api.getCategories()
            products = RetrofitClient.api.getProducts()
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    val filteredProducts = products.filter { product ->
        val matchesCategory = selectedCategoryId == null || product.category_id == selectedCategoryId
        val matchesSearch = searchQuery.isEmpty() || product.title.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏪") }, label = { Text("Market") })
                NavigationBarItem(selected = false, onClick = onNavigateToPurchases, icon = { Text("🛒") }, label = { Text("Purchases") })
                NavigationBarItem(selected = false, onClick = onNavigateToMyProducts, icon = { Text("🏬") }, label = { Text("My Shop") })
                NavigationBarItem(selected = false, onClick = onNavigateToDisputes, icon = { Text("🚩") }, label = { Text("Disputes") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Text("👤") }, label = { Text("Profile") })
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header & Search Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TrustTeal)
                    .padding(16.dp)
            ) {
                Text(
                    text = "TrustMarket",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Category Filter Chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("All") }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Main Content Body
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                filteredProducts.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found")
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(product = product, onClick = { onViewProduct(product.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductOut,
    onClick: () -> Unit
) {
    var trustProfile by remember { mutableStateOf<TrustProfileOut?>(null) }

    LaunchedEffect(product.seller_id) {
        try {
            trustProfile = RetrofitClient.api.getSellerTrust(product.seller_id)
        } catch (_: Exception) { }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatPrice(product.price),
                fontWeight = FontWeight.Bold,
                color = TrustTeal,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.condition?.replace("_", " ") ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                trustProfile?.let {
                    Text(
                        text = "${it.trust_score}% Trust",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrustTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}