package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyContactEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactToEdit: EmergencyContactEntity?,
    onBack: () -> Unit,
    onSaveContact: (String, String, String, Boolean) -> Unit,
    onUpdateExistingContact: (EmergencyContactEntity) -> Unit
) {
    var name by remember { mutableStateOf(contactToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(contactToEdit?.phone ?: "") }
    var relationship by remember { mutableStateOf(contactToEdit?.relationship ?: "Family") }
    var isImportant by remember { mutableStateOf(contactToEdit?.isImportant ?: true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(contactToEdit) {
        if (contactToEdit != null) {
            name = contactToEdit.name
            phone = contactToEdit.phone
            relationship = contactToEdit.relationship
            isImportant = contactToEdit.isImportant
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (contactToEdit == null) "Add Emergency Contact" else "Edit Contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .testTag("add_edit_contact_screen")
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .testTag("contact_name_input")
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number * (Include Country Code)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .testTag("contact_phone_input")
                            .fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Relationship (Spouse, Parent, Doctor, Friend)") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                        modifier = Modifier
                            .testTag("contact_relationship_input")
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mark as High Priority Contact", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("High priority contacts get instant priority alert", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            modifier = Modifier.testTag("contact_important_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter name and valid phone number") }
                                return@Button
                            }
                            if (contactToEdit != null) {
                                onUpdateExistingContact(
                                    contactToEdit.copy(
                                        name = name,
                                        phone = phone,
                                        relationship = relationship,
                                        isImportant = isImportant
                                    )
                                )
                            } else {
                                onSaveContact(name, phone, relationship, isImportant)
                            }
                        },
                        modifier = Modifier
                            .testTag("save_contact_button")
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE CONTACT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
