package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.trustmarket.app.network.SellerStatsOut
import com.trustmarket.app.network.TrustProfileOut
import com.trustmarket.app.ui.components.RiskBadge
import com.trustmarket.app.ui.components.riskColor
import androidx.compose.foundation.clickable
import com.trustmarket.app.network.friendlyErrorMessage
import com.trustmarket.app.util.daysAgo
import com.trustmarket.app.util.formatPrice

private val TrustTeal = Color(0xFF1F4E5F)

@Composable
fun ProductDetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onBuy: (Int) -> Unit,
    onReportSeller: (Int) -> Unit,
    onViewTrustProfile: (Int) -> Unit
) {
    var product by remember { mutableStateOf<ProductOut?>(null) }
    var seller by remember { mutableStateOf<SellerStatsOut?>(null) }
    var trust by remember { mutableStateOf<TrustProfileOut?>(null) }
    var category by remember { mutableStateOf<CategoryOut?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        try {
            val p = RetrofitClient.api.getProduct(productId)
            product = p
            seller = RetrofitClient.api.getSellerStats(p.seller_id)
            trust = RetrofitClient.api.getSellerTrust(p.seller_id)
            category = RetrofitClient.api.getCategories().find { it.id == p.category_id }
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Back", color = TrustTeal, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(error!!, color = MaterialTheme.colorScheme.error) }
            product != null -> ProductDetailContent(
                product = product!!,
                seller = seller,
                trust = trust,
                categoryName = category?.name ?: "Uncategorized",
                onBuy = onBuy,
                onReportSeller = onReportSeller,
                onViewTrustProfile = onViewTrustProfile
            )
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: ProductOut,
    seller: SellerStatsOut?,
    trust: TrustProfileOut?,
    categoryName: String,
    onBuy: (Int) -> Unit,
    onReportSeller: (Int) -> Unit,
    onViewTrustProfile: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFE0E0E0))
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Text(product.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrice(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TrustTeal
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0F8FB), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = (product.condition ?: "N/A")
                            .replace("_", " ")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (trust != null && trust.risk_level != "LOW") {
                Spacer(Modifier.height(16.dp))
                val color = riskColor(trust.risk_level)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${trust.risk_level} Risk Transaction", fontWeight = FontWeight.Bold, color = color)
                        Text("Review the seller's trust profile before committing to purchase.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            product.description?.let {
                Spacer(Modifier.height(20.dp))
                Text("DESCRIPTION", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    DetailRow("Condition", product.condition?.replace("_", " ") ?: "N/A")
                    DetailRow("Category", categoryName)
                    DetailRow("Status", product.status?.replace("_", " ") ?: "N/A")
                    DetailRow("Listed", "${product.created_at?.let { daysAgo(it) } ?: 0} days ago")
                }
            }

            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SELLER", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).background(TrustTeal, RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(seller?.email?.take(2)?.uppercase() ?: "??", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(seller?.email ?: "Unknown", fontWeight = FontWeight.Bold)
                            Text("${seller?.total_products ?: 0} products listed", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    trust?.let { RiskBadge(riskLevel = it.risk_level, trustScore = it.trust_score) }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { onViewTrustProfile(product.seller_id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("View Trust Profile")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onReportSeller(product.seller_id) }) { Text("Report") }
                Button(
                    onClick = { onBuy(product.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Buy Now")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}