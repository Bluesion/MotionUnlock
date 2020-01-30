package com.gpillusion.sensorunlock

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.gpillusion.sensorunlock.GearService.LocalBinder

class MainActivity : AppCompatActivity() {

    private var mService: GearService? = null
    private var mIsBound = false
    private var recyclerView: RecyclerView? = null

    private var handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            update()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartRate = findViewById(R.id.heart_rate)
        recyclerView = findViewById(R.id.recycler_view)
        adapter = MainAdapter(ArrayList())
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        mIsBound = bindService(Intent(this@MainActivity, GearService::class.java), mConnection, Context.BIND_AUTO_CREATE)

        val connectButton = findViewById<MaterialButton>(R.id.connect)
        val disconnectButton = findViewById<MaterialButton>(R.id.disconnect)

        val dataButton = findViewById<MaterialButton>(R.id.data)
        dataButton.setOnClickListener {
            if (mIsBound && mService != null) {
                mService!!.sendData("DATA")
            }
            if (adapter.itemCount > 1) {
                recyclerView!!.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }

        connectButton.setOnClickListener {
            if (mIsBound && mService != null) {
                mService!!.findPeers()
            }
        }
        disconnectButton.setOnClickListener {
            if (mIsBound &&  mService != null) {
                if (!(mService!!.closeConnection())) {
                    setCondition("Disconnected")
                    Toast.makeText(applicationContext, "AlreadyDisconnected", Toast.LENGTH_LONG).show()
                    adapter.clear()
                }
            }
        }

        val myThread = Thread(Runnable {
            while (true) {
                try {
                    Thread.sleep(1000)
                    handler.sendMessage(handler.obtainMessage())
                } catch (t: Throwable) {
                }
            }
        })
        myThread.start()
    }

    private fun update() {
        mService!!.sendData("DATA")
        recyclerView!!.scrollToPosition(adapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mIsBound && mService != null) {
            if (!mService!!.closeConnection()) {
                setCondition("Disconnected")
                adapter.clear()
            }
        }
        // Un-bind service
        if (mIsBound) {
            unbindService(mConnection)
            mIsBound = false
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder) {
            mService = (service as LocalBinder).service
            setCondition("onServiceConnected")
            mService!!.findPeers()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            mService = null
            mIsBound = false
            setCondition("onServiceDisconnected")
        }
    }

    companion object {
        lateinit var heartRate: MaterialTextView
        lateinit var adapter: MainAdapter

        fun addText(data: String) {
            val dataList = data.split("%")
            val hrm = dataList[0]
            val acc = dataList[1]
            val gyr = dataList[2]
            val prs = dataList[3]
            adapter.addText(Data(hrm, acc, gyr, prs))
        }

        fun setCondition(str: String?) {
            heartRate.text = str
        }
    }
}
