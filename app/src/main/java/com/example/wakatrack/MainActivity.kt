package com.example.wakatrack

import Project
import ProjectAdapter
import WakaTimeApi
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64


class MainActivity : AppCompatActivity() {
    private lateinit var apiKeyInput: EditText
    private lateinit var submitButton: Button
    private lateinit var apiKeyLayout: LinearLayout
    private lateinit var homeLayout: LinearLayout
    private lateinit var projectList: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var wakaTimeApi: WakaTimeApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        sharedPreferences = getSharedPreferences("WakaTrackPrefs", Context.MODE_PRIVATE)

        setupWakaTimeApi()

        val savedApiKey = sharedPreferences.getString("api_key", null)
        if (savedApiKey != null) {
            showHomeScreen(savedApiKey)
        } else {
            showApiKeyInput()
        }

        setupSubmitButton()
    }

    private fun initializeViews() {
        apiKeyInput = findViewById(R.id.apiKeyInput)
        submitButton = findViewById(R.id.submitButton)
        apiKeyLayout = findViewById(R.id.apiKeyLayout)
        homeLayout = findViewById(R.id.homeLayout)
        projectList = findViewById(R.id.projectList)
    }

    private fun setupWakaTimeApi() {
        wakaTimeApi = Retrofit.Builder()
            .baseUrl("https://wakatime.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WakaTimeApi::class.java)
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            val apiKey = apiKeyInput.text.toString()
            if (apiKey.isNotEmpty()) {
                saveApiKey(apiKey)
                showHomeScreen(apiKey)
            }
        }
    }

    private fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString("api_key", apiKey).apply()
    }

    private fun showApiKeyInput() {
        apiKeyLayout.visibility = View.VISIBLE
        homeLayout.visibility = View.GONE
    }

    private fun showHomeScreen(apiKey: String) {
        apiKeyLayout.visibility = View.GONE
        homeLayout.visibility = View.VISIBLE
        fetchProjects(apiKey)
    }

    private fun fetchProjects(apiKey: String) {
        lifecycleScope.launch {
            try {
                Log.d("WakaTrack", "Fetching projects...")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val calendar = Calendar.getInstance()
                val endDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, -7) // Fetch last 7 days
                val startDate = dateFormat.format(calendar.time)

                // Encode API key to Base64
                val encodedApiKey = Base64.encodeToString(apiKey.toByteArray(), Base64.NO_WRAP)
                val authorizationHeader = "Basic $encodedApiKey"

                val response = wakaTimeApi.getProjects(authorizationHeader, startDate, endDate)
                val allProjects = response.data.flatMap { it.projects }
                val consolidatedProjects = allProjects.groupBy { it.name }
                    .map { (name, projects) ->
                        Project(name, projects.sumOf { it.total_seconds })
                    }

                Log.d("WakaTrack", "Fetched ${consolidatedProjects.size} projects from $startDate to $endDate")

                if (consolidatedProjects.isEmpty()) {
                    Log.w("WakaTrack", "No projects found in the last 7 days")
                    Toast.makeText(this@MainActivity, "No projects found in the last 7 days", Toast.LENGTH_LONG).show()
                } else {
                    projectList.layoutManager = LinearLayoutManager(this@MainActivity)
                    projectList.adapter = ProjectAdapter(consolidatedProjects)
                    Log.d("WakaTrack", "Set adapter with ${consolidatedProjects.size} projects")
                }
            } catch (e: Exception) {
                Log.e("WakaTrack", "Error fetching projects", e)
                val errorMessage = when (e) {
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            401 -> "Authentication failed. Please check your API key."
                            403 -> "Access forbidden. Your API key might not have the necessary permissions."
                            else -> "HTTP Error ${e.code()}: ${e.message()}"
                        }
                    }
                    is java.net.UnknownHostException -> "Network error. Please check your internet connection."
                    else -> "Error fetching projects: ${e.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()

                // If it's an authentication error, prompt the user to re-enter the API key
                if (e is retrofit2.HttpException && e.code() == 401) {
                    showApiKeyInput()
                }
            }
        }
    }
}