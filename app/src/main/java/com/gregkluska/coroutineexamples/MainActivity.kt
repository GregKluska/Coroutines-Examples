package com.gregkluska.coroutineexamples

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class MainActivity : AppCompatActivity() {

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000
    private lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        job_button.setOnClickListener {
            if (!::job.isInitialized) { //check if lateinit is init
                initJob()
            }
            job_progress_bar.startJobOrCancel(job)
        }

    }

    // Extension function
    @SuppressLint("SetTextI18n")
    fun ProgressBar.startJobOrCancel(job: Job) {
        if(this.progress > 0) {
            println("$job is already active. Cancelling...")
            resetJob()
        } else {
            job_button.text = "Cancel job #1"
            CoroutineScope(IO + job).launch {
                println("coroutine ${this} is activated with job $job")

                for(i in PROGRESS_START.. PROGRESS_MAX) {
                    delay((JOB_TIME / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }

                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String) {
        GlobalScope.launch(Main) {
            job_complete_text.text = text
        }
    }

    private fun resetJob() {
        if(job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initJob() //once the job was cancelled, you can't reuse it. Need to create a new one
    }

    @SuppressLint("SetTextI18n")
    fun initJob() {
        job_button.text = "Start Job #1"
        updateJobCompleteTextView("")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if(msg.isNullOrBlank()) {
                    msg = "Unknown cancellation error."
                }
                println("${job} was canceled. Reason: $msg")
                showToast(msg)
            }
        }
        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    fun showToast(text: String) {
        GlobalScope.launch(Main) {
            Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
        }
    }
}