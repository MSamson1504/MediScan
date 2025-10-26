package com.example.mediscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

                // Holds selected symptoms passed from SymptomChecker -> PossibleDiagnoses
                var selectedSymptomsState by remember { mutableStateOf(listOf<String>()) }

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
                                selectedSymptomsState = listOf()
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
                        Screen.SymptomChecker -> SymptomCheckerScreen(
                            onBack = { currentScreen = Screen.Dashboard },
                            onNext = { selected ->
                                selectedSymptomsState = selected
                                currentScreen = Screen.PossibleDiagnoses
                            }
                        )
                        Screen.PossibleDiagnoses -> PossibleDiagnosesScreen(
                            selectedSymptoms = selectedSymptomsState,
                            onBack = { currentScreen = Screen.SymptomChecker }
                        )
                    }
                }
            }
        }
    }
}

enum class Screen {
    Login, Dashboard,
    MedicationReminders, SymptomInput, SymptomChecker, PossibleDiagnoses
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
        "Symptom Checker" to Screen.SymptomChecker
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
        Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp), tint = Color(0xFF9C27B0))
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
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
            label = { Text("Dosage (e.g. 500mg)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = reminderTime,
            onValueChange = { reminderTime = it },
            label = { Text("Schedule (e.g. 2x a day)") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            onClick = {
                if (medicineName.text.isNotBlank() && dosage.text.isNotBlank() && reminderTime.text.isNotBlank()) {
                    onAddReminder(MedicationReminder(medicineName.text, dosage.text, reminderTime.text))
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
        Text("Your Reminders", fontSize = 18.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        reminders.forEach { reminder ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(reminder.name, fontSize = 16.sp, color = Color(0xFF673AB7))
                    Text("Dosage: ${reminder.dosage}", fontSize = 13.sp, color = Color.DarkGray)
                    Text("Schedule: ${reminder.time}", fontSize = 13.sp, color = Color.DarkGray)
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
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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

// --- Symptom Checker Feature ---
@Composable
fun SymptomCheckerScreen(
    onBack: () -> Unit,
    onNext: (List<String>) -> Unit
) {
    var selectedBodyPart by remember { mutableStateOf<String?>(null) }
    var selectedSymptoms by remember { mutableStateOf(listOf<String>()) }
    val scrollState = rememberScrollState()

    val bodyParts = listOf(
        "Abdomen", "Arms general", "Back", "Buttocks & rectum", "Chest",
        "Face & Eyes", "Forehead & head in general", "Foot", "Finger",
        "Forearm & elbow", "Genitals & groin", "Hand & wrist", "Hips & hip joint",
        "Hair & scalp", "Lateral chest", "Legs general", "Lower leg & ankle",
        "Mouth & jaw", "Nose, ears, throat, & neck", "Pelvis", "Skin", "Thigh & knee", "Toes"
    )

    val symptoms = when (selectedBodyPart) {
        "Abdomen" -> listOf(
            "Abdominal pain", "Nausea", "Heartburn", "Bloated", "Diarrhea",
            "Vomiting", "Stomach burning", "Reduced Appetite"
        )
        "Arms general" -> listOf(
            "Pain in the limbs", "Joint pain", "Cramps", "Muscle stiffness",
            "Muscle pain", "Joint Swelling", "Joint effusion", "Joint redness",
            "Numbness in the arm", "Arm swelling/pain"
        )
        "Back" -> listOf("Back pain", "Lower back pain", "Pain radiating to the leg", "Pain radiating to the arm")
        "Buttocks & rectum" -> listOf(
            "Diarrhea", "Difficult defecation", "Hard defecation", "Incomplete defecation",
            "Less than 3 defecations per week", "Painful defecation", "Blood in stool", "Pain of the anus"
        )
        "Chest" -> listOf("Cough", "Chest pain", "Palpitations", "Nausea", "Heartburn", "Sputum", "Night cough", "Bloody cough", "Breathing related pains", "Lump in the breast")
        "Face & Eyes" -> listOf(
            "Eye redness", "Vision impairment", "Halo", "Itching eyes", "Burning eyes", "Blurred vision",
            "Oversensitivity to light", "Eyelid Swelling", "Tears", "Face pain", "Drooping eyelid", "Eye pain",
            "Burning nose", "Pain when chewing", "Facial paralysis", "Cheek swelling"
        )
        "Forehead & head in general" -> listOf(
            "Headache", "Fever", "Drowsiness", "Tiredness", "Nausea", "Difficulty to concentrate", "Mood swings",
            "Nervousness", "Dizziness", "Memory gap", "Anxiety", "Hallucination", "Feeling faint", "Unconsciousness", "Blackening of vision"
        )
        "Foot" -> listOf("Cold feet", "Changes in the nails", "Tremor at rest", "Tremor on movement", "Leg cramps", "Tingling", "Discolorations of nails", "Foot pain", "Agitation", "Foot swelling", "Limited mobility of the ankle")
        "Finger" -> listOf("Changes in the nails", "Tingling", "Finger deformity", "Finger pain", "Finger swelling")
        "Forearm & elbow" -> listOf("Hand pain", "Arm swelling", "Elbow pain")
        "Genitals & groin" -> listOf("Increased urine quantity", "Frequent urination", "Burning sensation when urinating", "Dark urine", "Painful urination", "Testicular pain", "Swollen glands in the groin", "Swelling of the testicles", "Itching or burning in the genital area")
        "Hand & wrist" -> listOf("Hand swelling", "Hand pain", "Numbness of the hands", "Discoloration of nails", "Cold hands")
        "Hips & hip joint" -> listOf("Pain radiating to the leg", "Physical activity pain", "Pain in the bones", "Bone fracture", "Limited mobility of the leg", "Hip pain", "Hip deformity", "Leg pain")
        "Hair & scalp" -> listOf("Hair loss", "Bold area among hair on the head", "Flaking skin on the head", "Itching on head", "Scalp redness")
        "Lateral chest" -> listOf("Side pain", "Swollen glands in the armpit")
        "Legs general" -> listOf("Pain in the bones", "Bone fracture", "Muscle pain", "Stress-related leg pain", "Joint swelling", "Joint effusion", "Limited mobility of the leg", "Joint instability", "Joint redness", "Leg redness", "Muscle weakness", "Enlarged calf", "Numbness in the leg")
        "Lower leg & ankle" -> listOf("Leg ulcer", "Feeling of tension in the legs", "Leg cramps", "Ankle swelling", "Limited mobility of the ankle", "Ankle deformity")
        "Mouth & jaw" -> listOf("Lip swelling", "Increased thirst", "Cravings", "Reduced appetite", "Mouth ulcers", "Difficulty in swallowing", "Vomiting", "Hiccups", "Increased appetite", "Mouth pain", "Pain on swallowing", "Lockjaw", "Increased salivation", "Dry mouth", "Pain when chewing", "Facial swelling", "Itching in the mouth or throat", "Tongue swelling", "Tongue burning", "Toothache")
        "Nose, ears, throat, & neck" -> listOf("Hoarseness", "Hiccups", "Night cough", "Neck pain", "Swollen glands in the neck", "Pain on swallowing", "Neck stiffness", "Burning nose", "Fast, deepened breathing")
        "Pelvis" -> listOf("Dark urine", "Painful urination", "Genital warts", "Urge to urinate", "Decreased urine system", "Swelling in the genital area")
        "Skin" -> listOf("Skin lesion", "Skin wheal", "Skin redness", "Formation of blisters", "Non-healing skin wound", "Irregular mole", "Yellow colored skin", "Skin rash", "Crusting", "Sweating", "Cold sweats", "Hot flushes", "Pallor", "Dry skin", "Muscle pain", "Hardening of the skin", "Wound", "Flaking skin", "Moist skin", "Skin thickening", "Blue spot", "Scar")
        "Thigh & knee" -> listOf("Feeling of tension in the legs", "Leg cramps", "Knee pain", "Absence of a pulse", "Leg pain")
        "Toes" -> listOf("Joint pain", "Changes in the nails", "Tingling", "Discoloration of nails", "Joint redness", "Brittleness of nails", "Toe deformity", "Toe swelling")
        else -> emptyList()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Symptom Checker", fontSize = 26.sp, color = Color(0xFF673AB7))
            TextButton(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(8.dp))

        if (selectedBodyPart == null) {
            Text("Select a body part:", fontSize = 18.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            bodyParts.forEach { part ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedBodyPart = part }
                ) {
                    Text(
                        text = part,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 16.sp,
                        color = Color(0xFF673AB7)
                    )
                }
            }
        } else if (symptoms.isNotEmpty()) {
            Text("Select symptoms from $selectedBodyPart:", fontSize = 18.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            symptoms.forEach { symptom ->
                val isSelected = symptom in selectedSymptoms
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            selectedSymptoms = if (isSelected)
                                selectedSymptoms - symptom
                            else
                                selectedSymptoms + symptom
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFE1BEE7) else Color.White
                    )
                ) {
                    Text(symptom, Modifier.padding(12.dp), fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { selectedBodyPart = null }) {
                    Text("Back to Body List")
                }
                Button(
                    onClick = { onNext(selectedSymptoms) },
                ) {
                    Text("Next")
                }
            }
        }
    }
}

// --- Possible Diagnoses Screen (single top-level definition) ---
@Composable
fun PossibleDiagnosesScreen(
    selectedSymptoms: List<String>,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // --- SYMPTOM-TO-DIAGNOSIS MAPPING (same as your map) ---
    val diagnosisMap = mapOf(
        "Abdominal pain" to listOf("Gastritis", "Food Poisoning", "Ulcer", "Appendicitis"),
        "Nausea" to listOf("Motion Sickness", "Gastroenteritis", "Pregnancy", "Food Poisoning"),
        "Heartburn" to listOf("Acid Reflux (GERD)", "Gastritis", "Hiatal Hernia"),
        "Bloated" to listOf("Indigestion", "Irritable Bowel Syndrome", "Lactose Intolerance"),
        "Diarrhea" to listOf("Infection", "Food Poisoning", "Irritable Bowel Syndrome"),
        "Vomiting" to listOf("Gastroenteritis", "Migraine", "Food Poisoning", "Pregnancy"),
        "Stomach burning" to listOf("Acid Reflux", "Gastritis", "Ulcer"),
        "Reduced Appetite" to listOf("Depression", "Infection", "Stomach Disorder"),

        "Chest pain" to listOf("Heart Attack (Myocardial Infarction)", "Angina", "Pneumonia", "Muscle Strain"),
        "Cough" to listOf("Common Cold", "Flu", "Bronchitis", "Asthma"),
        "Palpitations" to listOf("Anxiety", "Arrhythmia", "Thyroid Disorder"),
        "Breathing related pains" to listOf("Pleurisy", "Pneumonia", "Pulmonary Embolism"),
        "Lump in the breast" to listOf("Fibroadenoma", "Breast Cyst", "Breast Cancer"),

        "Eye redness" to listOf("Conjunctivitis", "Allergy", "Eye Strain"),
        "Blurred vision" to listOf("Refractive Error", "Diabetes", "Migraine"),
        "Eye pain" to listOf("Glaucoma", "Eye Infection", "Corneal Injury"),
        "Burning eyes" to listOf("Allergic Reaction", "Dry Eye Syndrome"),
        "Eyelid Swelling" to listOf("Stye", "Blepharitis", "Allergy"),

        "Headache" to listOf("Migraine", "Tension Headache", "Sinusitis", "Dehydration"),
        "Dizziness" to listOf("Vertigo", "Low Blood Pressure", "Anemia"),
        "Fever" to listOf("Infection", "Flu", "Dengue", "Viral Illness"),
        "Tiredness" to listOf("Anemia", "Lack of Sleep", "Thyroid Disorder", "Stress"),

        "Back pain" to listOf("Muscle Strain", "Slipped Disc", "Scoliosis", "Kidney Stone"),
        "Pain radiating to the leg" to listOf("Sciatica", "Herniated Disc"),
        "Lower back pain" to listOf("Muscle Strain", "Arthritis", "Kidney Issue"),

        "Joint pain" to listOf("Arthritis", "Rheumatism", "Gout", "Lupus"),
        "Leg cramps" to listOf("Dehydration", "Poor Circulation", "Electrolyte Imbalance"),
        "Muscle pain" to listOf("Overuse", "Injury", "Inflammation"),
        "Numbness in the leg" to listOf("Sciatica", "Peripheral Neuropathy", "Pinched Nerve"),
        "Swelling" to listOf("Injury", "Infection", "Venous Insufficiency"),

        "Painful urination" to listOf("Urinary Tract Infection", "Kidney Infection", "STD"),
        "Frequent urination" to listOf("Diabetes", "UTI", "Overactive Bladder"),
        "Dark urine" to listOf("Dehydration", "Liver Disease", "Hematuria"),
        "Testicular pain" to listOf("Epididymitis", "Testicular Torsion", "Hernia"),
        "Itching in genital area" to listOf("Fungal Infection", "Allergy", "STD"),

        "Toothache" to listOf("Cavity", "Gingivitis", "Tooth Infection"),
        "Dry mouth" to listOf("Dehydration", "Medication Side Effect", "Diabetes"),
        "Mouth ulcers" to listOf("Canker Sores", "Vitamin Deficiency", "Viral Infection"),

        "Skin rash" to listOf("Allergic Reaction", "Eczema", "Chickenpox", "Fungal Infection"),
        "Itching" to listOf("Allergy", "Scabies", "Fungal Infection"),
        "Yellow colored skin" to listOf("Jaundice", "Liver Disorder"),
        "Dry skin" to listOf("Eczema", "Dehydration"),
        "Formation of blisters" to listOf("Burns", "Allergic Reaction", "Chickenpox"),

        "Joint swelling" to listOf("Arthritis", "Injury", "Infection"),
        "Muscle stiffness" to listOf("Parkinson’s Disease", "Muscle Strain"),
        "Cold hands" to listOf("Poor Circulation", "Anemia", "Raynaud’s Disease"),
        "Cold feet" to listOf("Peripheral Artery Disease", "Diabetes", "Anemia")
    )

    val possibleDiagnoses = selectedSymptoms
        .flatMap { symptom -> diagnosisMap[symptom] ?: emptyList() }
        .distinct()
        .ifEmpty { listOf("No specific diagnosis found. Please consult a healthcare professional.") }

    val diagnosisDetails = mapOf(
        "Gastritis" to Pair(
            "Inflammation of the stomach lining, often triggered by spicy food, stress, or alcohol.",
            "Medicines: Antacids (Kremil-S, Gaviscon), Omeprazole (Losec)."
        ),
        "Food Poisoning" to Pair(
            "Caused by eating contaminated food or water.",
            "Medicines: Oral Rehydration Salts (Hydrite), Loperamide (Diatabs), Erceflora probiotics."
        ),
        "Ulcer" to Pair(
            "Painful sores in the stomach caused by stress, infection, or excessive acid.",
            "Medicines: Omeprazole, Antacids, avoid spicy foods."
        ),
        "Appendicitis" to Pair(
            "Inflammation of the appendix, causing severe abdominal pain — requires medical attention.",
            "Immediate hospital care is recommended, not self-medication."
        ),
        "Migraine" to Pair(
            "Severe throbbing headache triggered by stress, lack of sleep, or certain foods.",
            "Medicines: Paracetamol (Biogesic), Ibuprofen (Medicol, Advil), Mefenamic Acid (Ponstan)."
        ),
        "Tension Headache" to Pair(
            "Mild to moderate head pain caused by muscle tension or stress.",
            "Medicines: Paracetamol, Ibuprofen, rest and hydration."
        ),
        "Sinusitis" to Pair(
            "Inflammation of the sinuses due to infection or allergy.",
            "Medicines: Phenylephrine + Chlorphenamine (Neozep), steam inhalation, Lagundi syrup."
        ),
        "Dehydration" to Pair(
            "Body lacks water due to heat, diarrhea, or excessive sweating.",
            "Medicines: Oral Rehydration Salts (Hydrite), drink plenty of fluids."
        ),
        "Flu" to Pair(
            "Viral infection causing fever, cough, and fatigue.",
            "Medicines: Paracetamol, Lagundi syrup, Vitamin C, rest and fluids."
        ),
        "Dengue" to Pair(
            "Viral infection spread by mosquitoes, causes high fever and body pain.",
            "No specific medicine — drink water, rest, and consult a doctor immediately."
        ),
        "Bronchitis" to Pair(
            "Inflammation of the airways causing cough with phlegm.",
            "Medicines: Ambroxol (Mucosolvan), Carbocisteine (Solmux), rest and fluids."
        ),
        "Asthma" to Pair(
            "Narrowing of airways causing difficulty breathing, often triggered by allergens or pollution.",
            "Medicines: Salbutamol inhaler (Ventolin) if prescribed, avoid triggers."
        ),
        "Common Cold" to Pair(
            "Viral infection causing runny nose and sneezing.",
            "Medicines: Neozep, Bioflu, rest, fluids, and Vitamin C."
        ),
        "Arthritis" to Pair(
            "Joint inflammation causing pain and stiffness, triggered by overuse or aging.",
            "Medicines: Alaxan, Flanax (Naproxen), topical pain liniments."
        ),
        "Rheumatism" to Pair(
            "Joint or muscle pain due to inflammation or cold weather.",
            "Medicines: Mefenamic Acid, Ibuprofen, warm compress."
        ),
        "Gout" to Pair(
            "Build-up of uric acid in joints, triggered by red meat and alcohol.",
            "Medicines: Colchicine (for attacks), avoid high-purine foods."
        ),
        "Lupus" to Pair(
            "Autoimmune disorder causing joint pain and rashes.",
            "Requires medical diagnosis and prescription medication."
        ),
        "Indigestion" to Pair(
            "Discomfort in the upper abdomen due to overeating or acidic foods.",
            "Medicines: Kremil-S, Gaviscon, avoid spicy/oily meals."
        ),
        "Vertigo" to Pair(
            "Feeling of spinning due to inner ear issues or dehydration.",
            "Medicines: Meclizine (Bonamine), Betahistine (Serc)."
        ),
        "Conjunctivitis" to Pair(
            "Also called pink eye; inflammation of the eye’s outer layer due to infection or allergy.",
            "Medicines: Eye drops (Eye Mo, Rohto), avoid rubbing eyes."
        ),
        "Allergy" to Pair(
            "Reaction to food, dust, or pollen causing sneezing or itchiness.",
            "Medicines: Cetirizine (Virlix, Allerkid), Loratadine (Claritin), Calamine lotion."
        ),
        "Fungal Infection" to Pair(
            "Caused by fungus on skin or genital area, often itchy.",
            "Medicines: Clotrimazole cream, Ketoconazole (Nizoral), maintain dryness."
        ),
        "Tooth Infection" to Pair(
            "Bacterial infection in a tooth causing severe pain and swelling.",
            "Medicines: Mefenamic Acid (Ponstan) for pain; see a dentist for antibiotics."
        ),
        "Anemia" to Pair(
            "Low red blood cell count often due to iron deficiency.",
            "Medicines: Iron supplements (Sangobion, Iberet), eat leafy greens and red meat."
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text("Possible Diagnoses", fontSize = 26.sp, color = Color(0xFF673AB7))
        Spacer(Modifier.height(16.dp))
        Text("Based on your selected symptoms:", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        selectedSymptoms.forEach { symptom ->
            Text("• $symptom", fontSize = 15.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text("Here are the possible conditions:", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        possibleDiagnoses.forEach { diag ->
            val (desc, meds) = diagnosisDetails[diag]
                ?: Pair("No detailed information available.", "Please consult a doctor.")

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(diag, fontSize = 18.sp, color = Color(0xFF4A148C))
                    Spacer(Modifier.height(4.dp))
                    Text(desc, fontSize = 15.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(4.dp))
                    Text(meds, fontSize = 14.sp, color = Color(0xFF4CAF50))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}
