package fr.utbm.lo52.flicYouWear

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.IntentFilter



class MainActivity : WearableActivity() {

    @SuppressLint("ParcelCreator")

    private val waitingTaskText: String = "En attente de tâches..."

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra("TASK")
            text.text = data
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.andy.myapplication")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var task = TaskModel("Manger", 4, false)
        text.text = waitingTaskText

        var data = intent.extras
        if (data?.getParcelable<TaskModel>("task") !== null) {
            task = data.getParcelable<TaskModel?>("task") as TaskModel
            if (!task.status) {
                text.text = "Chambre ${task.roomNumber} \n ${task.typeOfTask}"
            } else {
                text.text = "Tâche terminée"
            }
        } else {
            TaskModel("Manger", 4, false)
        }

        // Enables Always-on
        setAmbientEnabled()
        pauseButton.setOnClickListener {
            launchPauseActivity()
        }
        launchTaskButton.setOnClickListener {
            if (text.text !== waitingTaskText) {
                launchTaskActivity(task)
            } else {
                // TODO log "no activity"
            }
        }
        simulTaskButton.setOnClickListener {
            text.text = "Chambre ${task.roomNumber} \n ${task.typeOfTask}"
        }
        urgenceButton.setOnClickListener {
            launchUrgenceActivity()
        }

        checkBluetoothState()
        startService(Intent(this, BluetoothServerController::class.java))
    }

    private fun checkBluetoothState() {
        var m_bluetoothAdapter: BluetoothAdapter? = null
        val REQUEST_ENABLE_BLUETOOTH = 1
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(m_bluetoothAdapter == null){
            Toast.makeText(applicationContext,"This device doesn't support bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if(!m_bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
    }

    private fun launchUrgenceActivity() {
        val intent = Intent(this, UrgenceActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchPauseActivity() {
        val intent = Intent(this, PauseActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchTaskActivity(task: TaskModel) {
        if (text.text != waitingTaskText) {
            val intent = Intent(this, CurrentTaskActivity::class.java)

            intent.putExtra("task", task)

            startActivity(intent)
            //finish()
        }
    }
}
