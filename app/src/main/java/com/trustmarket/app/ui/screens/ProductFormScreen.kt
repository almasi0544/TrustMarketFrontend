package com.trustmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.CategoryOut
import com.trustmarket.app.network.ProductCreateRequest
import com.trustmarket.app.network.ProductUpdateRequest
import com.trustmarket.app.network.RetrofitClient
import kotlinx.coroutines.launch
import com.trustmarket.app.network.friendlyErrorMessage

private val TrustTeal = Color(0xFF1F4E5F)
private val CONDITIONS = listOf("new", "used", "refurbished")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(token: String?, productId: Int?, onSaved: () -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(CONDITIONS.first()) }
    var conditionExpanded by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<CategoryOut>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isEdit = productId != null

    LaunchedEffect(productId) {
        try {
            categories = RetrofitClient.api.getCategories()
            if (productId != null) {
                val p = RetrofitClient.api.getProduct(productId)
                title = p.title
                description = p.description ?: ""
                price = p.price.toString()
                condition = p.condition ?: CONDITIONS.first()
                selectedCategoryId = p.category_id
            } else if (categories.isNotEmpty()) {
                selectedCategoryId = categories.first().id
            }
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().background(TrustTeal).padding(24.dp)) {
            Text(if (isEdit) "Edit Product" else "Create Listing", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        }

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Text("Condition", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(expanded = conditionExpanded, onExpandedChange = { conditionExpanded = it }) {
                OutlinedTextField(
                    value = condition.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = conditionExpanded, onDismissRequest = { conditionExpanded = false }) {
                    CONDITIONS.forEach { c ->
                        DropdownMenuItem(text = { Text(c.replaceFirstChar { it.uppercase() }) }, onClick = { condition = c; conditionExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Category", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = categories.find { it.id == selectedCategoryId }?.name ?: "Select category",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; categoryExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(Modifier.height(20.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        if (token == null) return@Button
                        val priceValue = price.toDoubleOrNull()
                        val catId = selectedCategoryId
                        if (priceValue == null || catId == null || title.isBlank()) {
                            error = "Please fill in title, a valid price, and category"
                            return@Button
                        }
                        saving = true
                        error = null
                        scope.launch {
                            try {
                                if (isEdit) {
                                    RetrofitClient.api.updateProduct(
                                        productId!!,
                                        ProductUpdateRequest(title, description, priceValue, condition, catId),
                                        "Bearer $token"
                                    )
                                } else {
                                    RetrofitClient.api.createProduct(
                                        ProductCreateRequest(title, description, priceValue, condition, catId),
                                        "Bearer $token"
                                    )
                                }
                                onSaved()
                            } catch (e: Exception) {
                                error = friendlyErrorMessage(e)
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (saving) "Saving..." else if (isEdit) "Save Changes" else "Publish")
                }
            }
        }
    }
}