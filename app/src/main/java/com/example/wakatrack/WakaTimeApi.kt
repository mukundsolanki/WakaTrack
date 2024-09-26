import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WakaTimeApi {
    @GET("users/current/summaries")
    suspend fun getProjects(
        @Header("Authorization") apiKey: String,
        @Query("start") startDate: String,
        @Query("end") endDate: String
    ): SummariesResponse
}

data class SummariesResponse(val data: List<DaySummary>)
data class DaySummary(val projects: List<Project>)
data class Project(val name: String, val total_seconds: Double)