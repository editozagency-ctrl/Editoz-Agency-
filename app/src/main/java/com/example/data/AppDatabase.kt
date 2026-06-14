package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val progress: Int, // 0 to 100
    val status: String, // "Planning", "In Progress", "Review", "Completed"
    val taskListJson: String, // Comma-separated tasks: e.g. "Draft briefs:Done,Script video:Done,Record A-roll:Done,Edit rough cut:Pending,Color grade:Pending"
    val revisionRequest: String? = null,
    val documentUrl: String? = null,
    val signatureSaved: Boolean = false,
    val lastUpdate: String
)

@Entity(tableName = "tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val category: String,
    val status: String, // "Open", "Pending", "Resolved"
    val priority: String, // "High", "Critical", "Medium", "Low"
    val lastMessage: String,
    val dateCreated: String
)

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val businessName: String,
    val serviceType: String,
    val websiteUrl: String? = null,
    val description: String,
    val inquiryType: String, // "Consultation", "Free Audit", "Inquiry"
    val dateCreated: String
)

@Entity(tableName = "ai_logs")
data class AILog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolType: String, // "Caption", "Hashtag", "Reel Script", "Idea"
    val prompt: String,
    val result: String,
    val dateCreated: String
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: String,
    val amount: Double,
    val serviceName: String,
    val status: String, // "Paid", "Pending"
    val dateCreated: String,
    val invoiceUrl: String
)

@Dao
interface AgencyDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getProjectsFlow(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Update
    suspend fun updateProject(project: Project)

    @Query("SELECT * FROM tickets ORDER BY dateCreated DESC")
    fun getTicketsFlow(): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicket)

    @Query("SELECT * FROM leads ORDER BY id DESC")
    fun getLeadsFlow(): Flow<List<Lead>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteLeadById(id: Int)

    @Query("SELECT * FROM ai_logs ORDER BY id DESC")
    fun getAILogsFlow(): Flow<List<AILog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAILog(aiLog: AILog)

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
}

@Database(entities = [Project::class, SupportTicket::class, Lead::class, AILog::class, Payment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agencyDao(): AgencyDao
}
