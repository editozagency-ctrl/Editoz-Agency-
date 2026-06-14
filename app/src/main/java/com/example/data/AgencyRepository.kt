package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgencyRepository(private val context: Context) {

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "editoz_agency_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val dao: AgencyDao by lazy { db.agencyDao() }

    val projects: Flow<List<Project>> = dao.getProjectsFlow()
    val tickets: Flow<List<SupportTicket>> = dao.getTicketsFlow()
    val leads: Flow<List<Lead>> = dao.getLeadsFlow()
    val aiLogs: Flow<List<AILog>> = dao.getAILogsFlow()
    val payments: Flow<List<Payment>> = dao.getPaymentsFlow()

    init {
        // Pre-populate database with default items on startup if empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedInitialDataIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProjects = dao.getProjectsFlow().first()
        if (currentProjects.isEmpty()) {
            // Seed Projects
            dao.insertProject(
                Project(
                    title = "FitLife Summer Reels Campaign",
                    category = "Reel Editing",
                    progress = 75,
                    status = "Review",
                    taskListJson = "Draft briefs:Done,Capture high-quality clips:Done,Dynamic transitions:Done,Sound design elements:Done,Captions & Color grade:Pending",
                    revisionRequest = "Please speed up the intro clip by 1.5x and make the music drop hit exactly on the fitness transition.",
                    documentUrl = "editoz_fitlife_reel_agreement.pdf",
                    signatureSaved = true,
                    lastUpdate = "Updated 2 hours ago"
                )
            )
            dao.insertProject(
                Project(
                    title = "Apex Tech Brand Visual Overhaul",
                    category = "Branding",
                    progress = 100,
                    status = "Completed",
                    taskListJson = "Discovery consultation:Done,Branding moodboard:Done,Typography & Logo concepts:Done,Brand stylebook finalization:Done",
                    revisionRequest = null,
                    documentUrl = "editoz_apextech_agreement_finalized.pdf",
                    signatureSaved = true,
                    lastUpdate = "Completed yesterday"
                )
            )
            dao.insertProject(
                Project(
                    title = "Glam Beauty Meta Ads Funnel",
                    category = "Meta Ads",
                    progress = 30,
                    status = "In Progress",
                    taskListJson = "Audience persona building:Done,Ad copy drafting:Done,CBO structure config:Pending,Dynamic creative upload:Pending,A/B testing launch:Pending",
                    revisionRequest = null,
                    documentUrl = "editoz_glam_beauty_funnel.pdf",
                    signatureSaved = false,
                    lastUpdate = "Updated 3 days ago"
                )
            )

            // Seed Payments
            dao.insertPayment(
                Payment(
                    transactionId = "TXN-EDITOZ-948102",
                    amount = 1250.00,
                    serviceName = "Premium Social Media Retainer",
                    status = "Paid",
                    dateCreated = "June 10, 2026",
                    invoiceUrl = "invoice_fitlife_june.pdf"
                )
            )
            dao.insertPayment(
                Payment(
                    transactionId = "TXN-EDITOZ-331045",
                    amount = 3500.00,
                    serviceName = "Corporate Branding Package",
                    status = "Paid",
                    dateCreated = "May 28, 2026",
                    invoiceUrl = "invoice_apextech_branding.pdf"
                )
            )
            dao.insertPayment(
                Payment(
                    transactionId = "TXN-EDITOZ-105284",
                    amount = 899.00,
                    serviceName = "Reel Production Bundle (15 custom revisions)",
                    status = "Pending",
                    dateCreated = "June 13, 2026",
                    invoiceUrl = "invoice_glambeauty_reels.pdf"
                )
            )

            // Seed Tickets
            dao.insertTicket(
                SupportTicket(
                    subject = "Meta Ads Lead Cost Fluctuations",
                    category = "Meta Ads",
                    status = "Pending",
                    priority = "High",
                    lastMessage = "Account Manager: 'We noticed a 15% CTR boost. Bid modifiers adjusted to balance CPL.'",
                    dateCreated = "June 12, 2026"
                )
            )
            dao.insertTicket(
                SupportTicket(
                    subject = "Reel edit - sound synchronization issue",
                    category = "Reel Editing",
                    status = "Open",
                    priority = "Medium",
                    lastMessage = "Client: 'The secondary audio track has a 1-sec lag around the middle mark.'",
                    dateCreated = "June 13, 2026"
                )
            )

            // Seed AI logs
            dao.insertAILog(
                AILog(
                    toolType = "Caption",
                    prompt = "High-end jewelry minimalist caption with premium aesthetic",
                    result = "Shine in silent elegance. ✨ Crafted for those who speak luxury without whispering a word. Discover the Eclipse Collection.\n\n#MinimalistLuxury #PremiumJewellery #EclipseCollec #AestheticShine",
                    dateCreated = "June 12, 2026"
                )
            )
        }
    }

    suspend fun insertProject(project: Project) = withContext(Dispatchers.IO) {
        dao.insertProject(project)
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        dao.updateProject(project)
    }

    suspend fun insertPayment(payment: Payment) = withContext(Dispatchers.IO) {
        dao.insertPayment(payment)
    }

    suspend fun insertTicket(ticket: SupportTicket) = withContext(Dispatchers.IO) {
        dao.insertTicket(ticket)
    }

    suspend fun insertLead(lead: Lead) = withContext(Dispatchers.IO) {
        dao.insertLead(lead)
    }

    suspend fun deleteLead(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteLeadById(id)
    }

    suspend fun insertAILog(aiLog: AILog) = withContext(Dispatchers.IO) {
        dao.insertAILog(aiLog)
    }
}
