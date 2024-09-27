import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wakatrack.R

class ProjectAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {

    init {
        Log.d("WakaTrack", "ProjectAdapter initialized with ${projects.size} projects")
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.projectName)
        val timeSpent: TextView = view.findViewById(R.id.timeSpent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.project_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.name
        holder.timeSpent.text = formatTime(project.total_seconds)
        Log.d(
            "WakaTrack",
            "Bound project: ${project.name} with time: ${formatTime(project.total_seconds)}"
        )
    }

    override fun getItemCount() = projects.size

    private fun formatTime(seconds: Double): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format("%.1fh %.0fm", hours, minutes)
    }
}