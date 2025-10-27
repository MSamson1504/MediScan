package com.example.mediscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediscan.ui.theme.MediScanTheme
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.MapView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediScanTheme {
                var userName by remember { mutableStateOf("") }
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

                // Medication & Symptom logs (demo: keep in memory)
                var medicationReminders by remember { mutableStateOf(listOf<MedicationReminder>()) }
                var symptomLogs by remember { mutableStateOf(listOf<SymptomLog>()) }
//                var findHealthcare by remember { mutableStateOf(listOf<FindHealthcare>()) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.Login -> LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onSubmit = { name: String ->
                                userName = name
                                currentScreen = Screen.Dashboard
                            }
                        )
                        Screen.Dashboard -> DashboardScreen(
                            name = userName,
                            modifier = Modifier.padding(innerPadding),
                            onNavigate = { currentScreen = it },
                            onLogout = {
                                userName = ""
                                currentScreen = Screen.Login
                                medicationReminders = listOf()
                                symptomLogs = listOf()
                            }
                        )
                        Screen.MedicationReminders -> MedicationRemindersScreen(
                            reminders = medicationReminders,
                            onAddReminder = { reminder -> medicationReminders = medicationReminders + reminder },
                            onLogResponse = { index, response ->
                                medicationReminders = medicationReminders.mapIndexed { i, r ->
                                    if (i == index) r.copy(status = response) else r
                                }
                            },
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        Screen.SymptomInput -> SymptomInputScreen(
                            logs = symptomLogs,
                            onAddLog = { log -> symptomLogs = symptomLogs + log },
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                        Screen.FindHealthcare -> FindHealthcareScreen(
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}

enum class Screen {
    Login, Dashboard,
    MedicationReminders, SymptomInput, FindHealthcare
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSubmit: (String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var age by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("") }
    var healthHistory by remember { mutableStateOf(TextFieldValue("")) }

    val genderOptions = listOf("Male", "Female", "Other")
    var expanded by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = "MediScan Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 8.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text("MediScan", fontSize = 32.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (gender.isNotEmpty()) gender else "Select Gender")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            gender = option
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = healthHistory,
            onValueChange = { healthHistory = it },
            label = { Text("Health History") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            maxLines = 5
        )

        Button(
            onClick = {
                showConfirmation = true
                onSubmit(name.text)
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("Submit")
        }

        if (showConfirmation) {
            Text(
                text = "Your record has been saved. Proceeding...",
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DashboardScreen(
    name: String,
    modifier: Modifier = Modifier,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6FAFF))
            .padding(18.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Hi, ${name.ifBlank { "User" }}!",
                fontSize = 20.sp,
                color = Color(0xFF673AB7)
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) {
                Text("Logout", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DashboardMenuGrid(onNavigate = onNavigate)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "© 2025 MediScan",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DashboardMenuGrid(onNavigate: (Screen) -> Unit) {
    val menuItems = listOf(
        MenuItem(Icons.Default.LocalHospital, "Symptom Checker"),
        MenuItem(Icons.Default.Search, "Find Healthcare"),
        MenuItem(Icons.Default.DateRange, "Appointments"),
        MenuItem(Icons.Default.Warning, "Emergency"),
        MenuItem(Icons.Default.Info, "Recommendations"),
        MenuItem(Icons.Default.Settings, "Settings"),
        MenuItem(Icons.Default.Medication, "Medication Reminders"),
        MenuItem(Icons.Default.Sick, "Symptom Input")
    )
    val navMapping = mapOf(
        "Medication Reminders" to Screen.MedicationReminders,
        "Symptom Input" to Screen.SymptomInput,
        "Find Healthcare" to Screen.FindHealthcare
    )
    Column {
        for (row in menuItems.chunked(2)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (item in row) {
                    DashboardMenuCard(
                        icon = item.icon,
                        label = item.label,
                        onClick = {
                            navMapping[item.label]?.let { onNavigate(it) }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class MenuItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

@Composable
fun DashboardMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE3F2FD))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp), tint = Color(
            0xFF9C27B0
        )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color(0xFF673AB7), fontSize = 15.sp)
    }
}

// --- Medication Reminders Feature ---

data class MedicationReminder(
    val name: String,
    val dosage: String,
    val time: String,
    val status: String = "" // "taken", "missed", ""
)

@Composable
fun MedicationRemindersScreen(
    reminders: List<MedicationReminder>,
    onAddReminder: (MedicationReminder) -> Unit,
    onLogResponse: (Int, String) -> Unit,
    onBack: () -> Unit
) {
    var medicineName by remember { mutableStateOf(TextFieldValue("")) }
    var dosage by remember { mutableStateOf(TextFieldValue("")) }
    var reminderTime by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Medication Reminders", fontSize = 26.sp, color = Color(0xFF673AB7))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = medicineName,
            onValueChange = { medicineName = it },
            label = { Text("Medicine Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = dosage,
            onValueChange = { dosage = it },
            label = { Text("Dosage") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = reminderTime,
            onValueChange = { reminderTime = it },
            label = { Text("Reminder Time") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            onClick = {
                if (medicineName.text.isNotBlank() && dosage.text.isNotBlank() && reminderTime.text.isNotBlank()) {
                    onAddReminder(
                        MedicationReminder(
                            name = medicineName.text,
                            dosage = dosage.text,
                            time = reminderTime.text
                        )
                    )
                    medicineName = TextFieldValue("")
                    dosage = TextFieldValue("")
                    reminderTime = TextFieldValue("")
                }
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Add Reminder")
        }
        Spacer(Modifier.height(24.dp))
        Text("Reminders Log", fontSize = 18.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        reminders.forEachIndexed { index, reminder ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(reminder.name, fontSize = 16.sp, color = Color(0xFF673AB7))
                        Text("Dosage: ${reminder.dosage}, Time: ${reminder.time}", fontSize = 13.sp, color = Color.DarkGray)
                        Text("Status: ${reminder.status.ifBlank { "Pending" }}", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    if (reminder.status.isBlank()) {
                        Button(
                            onClick = { onLogResponse(index, "taken") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) { Text("Taken") }
                        Button(
                            onClick = { onLogResponse(index, "missed") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) { Text("Missed") }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

// --- Symptom Input & Log Feature ---

data class SymptomLog(
    val name: String,
    val severity: String,
    val notes: String
)

@Composable
fun SymptomInputScreen(
    logs: List<SymptomLog>,
    onAddLog: (SymptomLog) -> Unit,
    onBack: () -> Unit
) {
    var symptomName by remember { mutableStateOf(TextFieldValue("")) }
    var severity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    val severityOptions = listOf("Mild", "Moderate", "Severe")
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Symptom Input", fontSize = 26.sp, color = Color(0xFF673AB7))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = symptomName,
            onValueChange = { symptomName = it },
            label = { Text("Symptom Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (severity.isNotEmpty()) severity else "Select Severity")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                severityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            severity = option
                            expanded = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            maxLines = 3
        )
        Button(
            onClick = {
                if (symptomName.text.isNotBlank() && severity.isNotBlank()) {
                    onAddLog(SymptomLog(symptomName.text, severity, notes.text))
                    symptomName = TextFieldValue("")
                    severity = ""
                    notes = TextFieldValue("")
                }
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Add Symptom Log")
        }
        Spacer(Modifier.height(24.dp))
        Text("Symptom History", fontSize = 18.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        logs.forEach { log ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(log.name, fontSize = 16.sp, color = Color(0xFF673AB7))
                    Text("Severity: ${log.severity}", fontSize = 13.sp, color = Color.DarkGray)
                    Text("Notes: ${log.notes}", fontSize = 13.sp, color = Color.DarkGray)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun FindHealthcareScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Find Healthcare Facilities",
                fontSize = 20.sp,
                color = Color(0xFF673AB7),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Map container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMapView()
        }
    }
}

@Composable
fun GoogleMapView() {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                onCreate(Bundle())
                getMapAsync { googleMap ->
                    // Configure the map
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.uiSettings.isZoomGesturesEnabled = true

                    // Set up initial location (example: San Francisco)
                    val sanFrancisco = LatLng(37.7749, -122.4194)

//                    // Add markers for healthcare facilities
//                    val healthcareLocations = listOf(
//                        LatLng(37.7749, -122.4194) to "General Hospital",
//                        LatLng(37.7849, -122.4094) to "Community Clinic",
//                        LatLng(37.7649, -122.4294) to "Urgent Care Center",
//                        LatLng(37.7799, -122.4394) to "Medical Center"
//                    )
//
//                    healthcareLocations.forEach { (location, title) ->
//                        googleMap.addMarker(
//                            MarkerOptions()
//                                .position(location)
//                                .title(title)
//                                .snippet("Healthcare Facility")
//                        )
//                    }

                    // Move camera to show the area
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sanFrancisco, 12f))
                }
            }
        },
        update = { mapView ->
            mapView.onResume()
        },
        modifier = Modifier.fillMaxSize()
    )
}