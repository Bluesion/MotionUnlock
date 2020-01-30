/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that 
 * the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright notice, 
 *       this list of conditions and the following disclaimer in the documentation and/or 
 *       other materials provided with the distribution. 
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its contributors may be used to endorse or 
 *       promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.gpillusion.sensorunlock

import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.samsung.android.sdk.SsdkUnsupportedException
import com.samsung.android.sdk.accessory.*
import java.io.IOException

class GearService : SAAgent(TAG, SASOCKET_CLASS) {

    private val mBinder: IBinder = LocalBinder()
    private var mConnectionHandler: ServiceConnection? = null
    private var mHandler = Handler()

    override fun onCreate() {
        super.onCreate()
        val mAccessory = SA()
        try {
            mAccessory.initialize(this)
        } catch (e: SsdkUnsupportedException) { // try to handle SsdkUnsupportedException
            if (processUnsupportedException(e)) {
                return
            }
        } catch (e1: Exception) {
            e1.printStackTrace()
            /*
             * Your application can not use Samsung Accessory SDK. Your application should work smoothly
             * without using this SDK, or you may want to notify user and close your application gracefully
             * (release resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onFindPeerAgentsResponse(peerAgents: Array<SAPeerAgent?>?, result: Int) {
        if (result == PEER_AGENT_FOUND && peerAgents != null) {
            for (peerAgent in peerAgents) {
                requestServiceConnection(peerAgent)
            }
        } else if (result == FINDPEER_DEVICE_NOT_CONNECTED) {
            Toast.makeText(applicationContext, "FINDPEER_DEVICE_NOT_CONNECTED", Toast.LENGTH_LONG).show()
            updateTextView("Disconnected")
        } else if (result == FINDPEER_SERVICE_NOT_FOUND) {
            Toast.makeText(applicationContext, "FINDPEER_SERVICE_NOT_FOUND", Toast.LENGTH_LONG).show()
            updateTextView("Disconnected")
        } else {
            Toast.makeText(applicationContext, "No peers have been found!!!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onServiceConnectionRequested(peerAgent: SAPeerAgent?) {
        if (peerAgent != null) {
            acceptServiceConnectionRequest(peerAgent)
        } else {
            Log.d("GEAR", "No peer Agent")
        }
    }

    override fun onServiceConnectionResponse(peerAgent: SAPeerAgent?, socket: SASocket, result: Int) {
        when (result) {
            CONNECTION_SUCCESS -> {
                mConnectionHandler = socket as ServiceConnection?
                updateTextView("Connected")
            }
            CONNECTION_ALREADY_EXIST -> {
                updateTextView("Connected")
                Toast.makeText(baseContext, "CONNECTION_ALREADY_EXIST", Toast.LENGTH_LONG).show()
            }
            CONNECTION_DUPLICATE_REQUEST -> {
                updateTextView("DUPLICATED")
                Toast.makeText(baseContext, "CONNECTION_DUPLICATE_REQUEST", Toast.LENGTH_LONG).show()
            }
            else -> {
                updateTextView("WRONG")
                Toast.makeText(baseContext, "Service Connection Failure", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPeerAgentsUpdated(peerAgents: Array<SAPeerAgent>?, result: Int) {
        val peers: Array<SAPeerAgent>? = peerAgents
        mHandler.post {
            if (peers != null) {
                if (result == PEER_AGENT_AVAILABLE) {
                    Toast.makeText(applicationContext, "PEER_AGENT_AVAILABLE", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "PEER_AGENT_UNAVAILABLE", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class ServiceConnection : SASocket(ServiceConnection::class.java.name) {
        override fun onError(channelId: Int, errorMessage: String?, errorCode: Int) {}

        override fun onReceive(channelId: Int, data: ByteArray?) {
            val message = String(data!!)
            addMessage(message)
        }

        override fun onServiceConnectionLost(reason: Int) {
            updateTextView("Disconnected")
            closeConnection()
        }
    }

    inner class LocalBinder : Binder() {
        val service: GearService
            get() = this@GearService
    }

    fun findPeers() {
        findPeerAgents()
    }

    fun sendData(data: String): Boolean {
        var value = false
        if (mConnectionHandler != null) {
            try {
                mConnectionHandler!!.send(getServiceChannelId(0), data.toByteArray())
                value = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return value
    }

    fun closeConnection(): Boolean {
        return if (mConnectionHandler != null) {
            mConnectionHandler!!.close()
            mConnectionHandler = null
            true
        } else {
            false
        }
    }

    private fun processUnsupportedException(e: SsdkUnsupportedException): Boolean {
        e.printStackTrace()
        val errType: Int = e.type
        if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED || errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) { /*
             * Your application can not use Samsung Accessory SDK. You application should work smoothly
             * without using this SDK, or you may want to notify user and close your app gracefully (release
             * resources, stop Service threads, close UI thread, etc.)
             */
            stopSelf()
        } else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
            Log.e(TAG, "You need to install Samsung Accessory SDK to use this application.")
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {
            Log.e(TAG, "You need to update Samsung Accessory SDK to use this application.")
        } else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
            Log.e(TAG, "We recommend that you update your Samsung Accessory SDK before using this application.")
            return false
        }
        return true
    }

    private fun updateTextView(str: String) {
        mHandler.post { MainActivity.setCondition(str) }
    }

    private fun addMessage(data: String) {
        mHandler.post { MainActivity.addText(data) }
    }

    companion object {
        private const val TAG = "SensorUnlock"
        private val SASOCKET_CLASS = ServiceConnection::class.java
    }
}