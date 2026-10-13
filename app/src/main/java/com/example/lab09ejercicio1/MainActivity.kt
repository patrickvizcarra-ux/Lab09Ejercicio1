
package com.example.lab09ejercicio1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.lab09ejercicio1.ui.theme.Lab09Ejercicio1Theme
import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Lab09Ejercicio1Theme {
                TiendaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaApp() {
    val navController = rememberNavController()

    val servicio = remember {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ShopDemo",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "productos",
            modifier = Modifier.padding(padding)
        ) {
            composable("productos") {
                PantallaProductos(
                    servicio = servicio,
                    alSeleccionar = { id ->
                        navController.navigate("detalle/$id")
                    }
                )
            }

            composable(
                route = "detalle/{id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.IntType
                    }
                )
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 1

                PantallaDetalle(
                    id = id,
                    servicio = servicio,
                    alRegresar = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaProductos(
    servicio: ProductApiService,
    alSeleccionar: (Int) -> Unit
) {
    var productos by remember {
        mutableStateOf<List<ProductModel>>(emptyList())
    }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableIntStateOf(0) }

    LaunchedEffect(intento) {
        cargando = true
        error = null

        try {
            productos = servicio.getProducts().products
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "No se pudieron cargar los productos."
        } finally {
            cargando = false
        }
    }

    when {
        cargando -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Cargando productos...")
            }
        }

        error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No se pudieron cargar los productos")
                Spacer(Modifier.height(8.dp))
                Text(error ?: "")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { intento++ }) {
                    Text("Reintentar")
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(
                            top = 12.dp,
                            bottom = 4.dp
                        )
                    ) {
                        Text(
                            "Descubre productos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${productos.size} productos disponibles",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(
                    items = productos,
                    key = { it.id }
                ) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                alSeleccionar(producto.id)
                            },
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = producto.thumbnail,
                                contentDescription = producto.title,
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.size(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    producto.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    producto.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "⭐ ${producto.rating}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    "$ ${"%.2f".format(java.util.Locale.US, producto.price)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        alSeleccionar(producto.id)
                                    }
                                ) {
                                    Text("Ver detalle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDetalle(
    id: Int,
    servicio: ProductApiService,
    alRegresar: () -> Unit
) {
    var producto by remember(id) {
        mutableStateOf<ProductModel?>(null)
    }
    var cargando by remember(id) { mutableStateOf(true) }
    var error by remember(id) { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        cargando = true
        error = null

        try {
            producto = servicio.getProductById(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "No se pudo cargar el producto."
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(onClick = alRegresar) {
            Text("← Volver a productos")
        }

        when {
            cargando -> {
                CircularProgressIndicator()
                Text("Cargando detalle...")
            }

            error != null -> {
                Text("Ocurrió un error")
                Text(error ?: "")
            }

            producto != null -> {
                val p = producto!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = p.thumbnail,
                                    contentDescription = p.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Text(
                                    p.category.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    p.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "$ ${"%.2f".format(java.util.Locale.US, p.price)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Text("⭐ ${p.rating} / 5")
                                Text("Marca: ${p.brand ?: "No especificada"}")
                                Text("Stock: ${p.stock} unidades")

                                Text(
                                    "Descripción",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(p.description)
                            }
                        }
                    }
                }
            }
        }
    }
}
