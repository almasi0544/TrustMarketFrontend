package com.trustmarket.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trustmarket.app.network.CategoryOut
import com.trustmarket.app.network.ProductCreateRequest
import com.trustmarket.app.network.RetrofitClient
import com.trustmarket.app.network.friendlyErrorMessage
import kotlinx.coroutines.launch

private val TrustTeal = Color(0xFF1F4E5F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    onBack: () -> Unit,
    onProductCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<CategoryOut>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<CategoryOut?>(null) }
    var condition by remember { mutableStateOf("used_good") }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            categories = RetrofitClient.api.getCategories()
            if (categories.isNotEmpty()) selectedCategory = categories.first()
        } catch (e: Exception) {
            error = friendlyErrorMessage(e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Cancel", color = TrustTeal, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(8.dp))
        Text("Create Listing", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Price ($)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Select Category",
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name) },
                        onClick = {
                            selectedCategory = cat
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val priceVal = priceStr.toDoubleOrNull()
                val catId = selectedCategory?.id
                if (title.isBlank() || priceVal == null || catId == null) {
                    error = "Please fill in all fields with valid data."
                    return@Button
                }

                submitting = true
                scope.launch {
                    try {
                        RetrofitClient.api.createProduct(
                            ProductCreateRequest(
                                title = title,
                                description = description.ifBlank { null },
                                price = priceVal,
                                category_id = catId,
                                condition = condition
                            )
                        )
                        onProductCreated()
                    } catch (e: Exception) {
                        error = friendlyErrorMessage(e)
                    } finally {
                        submitting = false
                    }
                }
            },
            enabled = !submitting,
            colors = ButtonDefaults.buttonColors(containerColor = TrustTeal),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (submitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Publish Listing", fontWeight = FontWeight.Bold)
        }
    }
}