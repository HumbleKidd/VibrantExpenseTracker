package com.humblekidd.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

data class Expense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val color: Color,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibrantExpenseTrackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@Composable
fun VibrantExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerApp() {
    var expenses by remember { mutableStateOf(
        listOf(
            Expense(1, "Lunch at Island Grill", 1500.0, "Food", Color(0xFF4CAF50), Icons.Default.Restaurant),
            Expense(2, "Gas for car", 4500.0, "Transport", Color(0xFF2196F3), Icons.Default.DirectionsCar),
            Expense(3, "Netflix", 1200.0, "Entertainment", Color(0xFFE91E63), Icons.Default.Movie),
            Expense(4, "Groceries", 3200.0, "Food", Color(0xFF4CAF50), Icons.Default.ShoppingCart),
            Expense(5, "Phone Bill", 2800.0, "Bills", Color(0xFF9C27B0), Icons.Default.Phone)
        )
    ) }
    var showAddSheet by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(6) }

    val total = expenses.sumOf { it.amount }
    val currency = NumberFormat.getCurrencyInstance(Locale("en", "JM")).apply {
        currency = Currency.getInstance("JMD")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vibrant Expenses", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Color(0xFF03DAC6)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.Black)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Spent", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        currency.format(total),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("${expenses.size} expenses", color = Color.White.copy(alpha = 0.7f))
                }
            }

            Text(
                "Recent Expenses",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        currency = currency,
                        onDelete = {
                            expenses = expenses.filter { it.id != expense.id }
                        }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            containerColor = Color.White
        ) {
            AddExpenseSheet(
                onAdd = { title, amount, category ->
                    val color = when (category) {
                        "Food" -> Color(0xFF4CAF50)
                        "Transport" -> Color(0xFF2196F3)
                        "Entertainment" -> Color(0xFFE91E63)
                        "Bills" -> Color(0xFF9C27B0)
                        else -> Color(0xFFFF9800)
                    }
                    val icon = when (category) {
                        "Food" -> Icons.Default.Restaurant
                        "Transport" -> Icons.Default.DirectionsCar
                        "Entertainment" -> Icons.Default.Movie
                        "Bills" -> Icons.Default.Phone
                        else -> Icons.Default.Category
                    }
                    expenses = expenses + Expense(nextId++, title, amount, category, color, icon)
                    showAddSheet = false
                },
                onDismiss = { showAddSheet = false }
            )
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, currency: NumberFormat, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(expense.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(expense.icon, contentDescription = null, tint = expense.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Medium)
                Text(expense.category, color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currency.format(expense.amount),
                    fontWeight = FontWeight.Bold,
                    color = expense.color
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AddExpenseSheet(onAdd: (String, Double, String) -> Unit, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    val categories = listOf("Food", "Transport", "Entertainment", "Bills", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Add New Expense", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (JMD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (title.isNotBlank() && amt > 0) {
                    onAdd(title, amt, category)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03DAC6))
        ) {
            Text("Add Expense", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}
