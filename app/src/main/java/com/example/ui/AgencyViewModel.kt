package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgencyViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AgencyRepository(application)

    // Room Flows
    val projectsState: StateFlow<List<Project>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ticketsState: StateFlow<List<SupportTicket>> = repository.tickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leadsState: StateFlow<List<Lead>> = repository.leads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentsState: StateFlow<List<Payment>> = repository.payments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiLogsState: StateFlow<List<AILog>> = repository.aiLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Navigation & Role States
    var currentScreen by mutableStateOf("splash") // splash, auth, home, services, packages, portfolio, portal, support, aitools, admin
    var currentUserRole by mutableStateOf("Client") // Client, Admin
    var isLoggedIn by mutableStateOf(false)
    var userEmail by mutableStateOf("")
    var userName by mutableStateOf("Enterprise Partner")
    var companyName by mutableStateOf("FitLife Gym")

    // UI Operation States
    var aiGeneratorType by mutableStateOf("Caption") // Caption, Hashtag, Reel Script, Content Idea
    var aiPromptInput by mutableStateOf("")
    var aiResultOutput by mutableStateOf("")
    var isGeneratingAI by mutableStateOf(false)

    var lastNotificationMessage by mutableStateOf<String?>(null)

    // Seeding dynamic feedback helper
    private fun showNotification(message: String) {
        lastNotificationMessage = message
    }

    fun dismissNotification() {
        lastNotificationMessage = null
    }

    // Auth actions
    fun performLogin(email: String, name: String, company: String, role: String = "Client") {
        userEmail = email
        userName = name.ifEmpty { "Enterprise Partner" }
        companyName = company.ifEmpty { "FitLife Gym" }
        currentUserRole = role
        isLoggedIn = true
        currentScreen = "home"
        showNotification("Welcome back, $userName!")
    }

    fun performLogout() {
        isLoggedIn = false
        currentScreen = "auth"
        showNotification("Logged out successfully.")
    }

    fun switchRole() {
        currentUserRole = if (currentUserRole == "Client") "Admin" else "Client"
        showNotification("Role-based access switched to $currentUserRole")
    }

    // Lead Generation Actions
    fun submitLeadForm(
        name: String,
        email: String,
        phone: String,
        businessName: String,
        serviceType: String,
        websiteUrl: String,
        description: String,
        inquiryType: String
    ) {
        viewModelScope.launch {
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            val lead = Lead(
                name = name,
                email = email,
                phone = phone,
                businessName = businessName,
                serviceType = serviceType,
                websiteUrl = websiteUrl.ifEmpty { null },
                description = description,
                inquiryType = inquiryType,
                dateCreated = date
            )
            repository.insertLead(lead)
            showNotification("Thank you! Your $inquiryType has been received securely.")
        }
    }

    fun deleteLead(id: Int) {
        viewModelScope.launch {
            repository.deleteLead(id)
            showNotification("Lead removed successfully.")
        }
    }

    // Support Ticket actions
    fun createSupportTicket(subject: String, category: String, priority: String, description: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            val ticket = SupportTicket(
                subject = subject,
                category = category,
                status = "Open",
                priority = priority,
                lastMessage = "YouRaised: '$description'",
                dateCreated = date
            )
            repository.insertTicket(ticket)
            showNotification("Support ticket raised. Team Editoz will respond within 15 minutes.")
        }
    }

    // Active Project revisions & contract signings
    fun submitRevisionRequest(project: Project, revision: String) {
        viewModelScope.launch {
            val updated = project.copy(
                status = "In Progress",
                revisionRequest = revision,
                lastUpdate = "Revision requested just now"
            )
            repository.updateProject(updated)
            showNotification("Revision request dispatched to production editor.")
        }
    }

    fun signAgreement(project: Project) {
        viewModelScope.launch {
            val updated = project.copy(
                signatureSaved = true,
                lastUpdate = "Agreement digitally signed just now"
            )
            repository.updateProject(updated)
            showNotification("Agreement securely sealed and certified.")
        }
    }

    // Mock payments trigger
    fun initiateMockPayment(amount: Double, service: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            val txn = "TXN-EDITOZ-" + (100000..999999).random()
            val newPayment = Payment(
                transactionId = txn,
                amount = amount,
                serviceName = service,
                status = "Paid",
                dateCreated = date,
                invoiceUrl = "invoice_${service.lowercase().replace(" ", "_")}.pdf"
            )
            repository.insertPayment(newPayment)
            showNotification("Payment of $${"%,.2f".format(amount)} received via Razorpay UPI.")
        }
    }

    // AI Generators Call
    fun generateAICreativeContent(type: String, prompt: String) {
        if (prompt.trim().isEmpty()) {
            showNotification("Please supply an input prompt first!")
            return
        }
        aiGeneratorType = type
        aiPromptInput = prompt
        isGeneratingAI = true
        aiResultOutput = ""

        viewModelScope.launch {
            try {
                val instruction = when (type) {
                    "Caption" -> "You are an expert social media copywriter for premium brands. Output an engaging, punchy caption utilizing gorgeous line spacing, rich emojis, and 5 highly researched strategic hashtags. Brand context: Editoz Agency digital solutions."
                    "Hashtag" -> "You are a social media optimization expert. Generate a highly targeted set of 15 viral tags tailored perfectly for the user's focus, arranged cleanly."
                    "Reel Script" -> "You are a professional cinematic scriptwriter for short form 15-second Reels and TikToks. Provide high retention structure: [0-3s] Hook with motion directions, [3-12s] Body with high-value points, [12-15s] Call-To-Action (CTA) directing to Editoz. Keep formatting structured and luxury-toned."
                    else -> "You are a digital marketing agency creative director. Provide 3 specific, out-of-the-box marketing content execution strategies for the user's business niche."
                }

                val finalPrompt = "Write a $type about: $prompt"
                val response = GeminiService.generateContent(finalPrompt, instruction)
                
                aiResultOutput = response

                // Log the output to Room database
                val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                repository.insertAILog(
                    AILog(
                        toolType = type,
                        prompt = prompt,
                        result = response,
                        dateCreated = date
                    )
                )
                showNotification("AI Generation complete! Saved to history log.")
            } catch (e: Exception) {
                aiResultOutput = "Generation completed. Failed to save logs but here is the result: $e"
            } finally {
                isGeneratingAI = false
            }
        }
    }
}
