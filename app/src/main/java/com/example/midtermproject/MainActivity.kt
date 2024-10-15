package com.example.midtermproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var btnAddTask: Button
    private lateinit var btnAbout: Button
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadTasks()

        recyclerView = findViewById(R.id.recyclerView)
        btnAddTask = findViewById(R.id.btnAddTask)
        btnAbout = findViewById(R.id.btnAbout)

        taskAdapter = TaskAdapter(taskList) { position -> openTaskDetail(position) }
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddTask.setOnClickListener {
            if (taskList.size < 7) {
                val intent = Intent(this, AddTaskActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_ADD_TASK)
            } else {
                btnAddTask.text = "Too much work!"
            }
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun openTaskDetail(position: Int) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putStringArrayListExtra("taskList", ArrayList(taskList.map { it.name }))
        intent.putExtra("taskIndex", position)
        startActivityForResult(intent, REQUEST_CODE_TASK_DETAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_TASK && resultCode == RESULT_OK) {
            val taskName = data?.getStringExtra("task") ?: return
            taskList.add(Task(taskName))
            taskAdapter.notifyDataSetChanged()
            saveTasks()
        } else if (requestCode == REQUEST_CODE_TASK_DETAIL && resultCode == RESULT_OK) {
            val completedTaskName = data?.getStringExtra("completedTask")
            val taskToRemove = taskList.find { it.name == completedTaskName }
            taskToRemove?.let {
                taskList.remove(it)
                taskAdapter.notifyDataSetChanged()
                saveTasks()
            }
        }
    }

    private fun saveTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(taskList)
        editor.putString("taskList", json)
        editor.apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getSharedPreferences("tasks", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("taskList", null)
        val type = object : TypeToken<MutableList<Task>>() {}.type
        taskList.clear()
        if (json != null) {
            taskList.addAll(gson.fromJson(json, type))
        }
    }

    companion object {
        const val REQUEST_CODE_ADD_TASK = 1
        const val REQUEST_CODE_TASK_DETAIL = 2
    }
}