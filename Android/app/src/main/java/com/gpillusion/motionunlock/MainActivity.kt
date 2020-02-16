package com.gpillusion.motionunlock

import android.annotation.SuppressLint
import android.content.*
import android.os.AsyncTask
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.gpillusion.motionunlock.GearService.LocalBinder
import com.gpillusion.motionunlock.data.ConvertedData
import com.gpillusion.motionunlock.data.RawData
import com.gpillusion.motionunlock.data.SensorData
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private var mService: GearService? = null
    private var mIsBound = false
    private var recyclerView: RecyclerView? = null
    private lateinit var registerProgressBar: ProgressBar
    private lateinit var registerProgressText: MaterialTextView
    private lateinit var registerStartButton: MaterialButton
    private lateinit var registerStopButton: MaterialButton
    private lateinit var registerRetryButton: MaterialButton
    private lateinit var registerNextButton: MaterialButton
    private lateinit var registerCloseButton: MaterialButton
    private var isRecognizing = false
    private var count = 1
    private lateinit var testProgressBar: ProgressBar
    private lateinit var testProgressText: MaterialTextView
    private lateinit var testStartButton: MaterialButton
    private lateinit var testStopButton: MaterialButton
    private lateinit var testRetryButton: MaterialButton
    private lateinit var testCloseButton: MaterialButton
    private lateinit var sharedPrefs: SharedPreferences

    private var firstVector = ArrayList<Float>()
    private var secondVector = ArrayList<Float>()
    private var thirdVector = ArrayList<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (!mIsBound) {
            mIsBound = bindService(
                    Intent(this@MainActivity, GearService::class.java),
                    mConnection,
                    Context.BIND_AUTO_CREATE
            )
        }

        sharedPrefs = getSharedPreferences("Sensor_data", Context.MODE_PRIVATE)

        loadingLayout = findViewById(R.id.loading_layout)
        registerLayout = findViewById(R.id.register_layout)
        testLayout = findViewById(R.id.test_layout)
        unlockLayout = findViewById(R.id.unlock_layout)
        disconnectedLayout = findViewById(R.id.disconnected_layout)
        failLayout = findViewById(R.id.fail_layout)
        val retryButton = findViewById<MaterialCardView>(R.id.retry_button)
        retryButton.setOnClickListener {
            ConnectTask().execute()
        }
        val reconnectButton = findViewById<MaterialCardView>(R.id.reconnect_button)
        reconnectButton.setOnClickListener {
            ConnectTask().execute()
        }

        recyclerView = findViewById(R.id.recycler_view)
        adapter = MainAdapter(ArrayList())
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        val clearButton = findViewById<MaterialButton>(R.id.clear)
        clearButton.setOnClickListener {
            adapter.clear()
        }

        // Register
        val registerButton = findViewById<MaterialButton>(R.id.register)
        registerButton.setOnClickListener {
            isRegistering = true
            unlockLayout.visibility = View.GONE
            registerLayout.visibility = View.VISIBLE
            testLayout.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.GONE
        }

        registerProgressBar = findViewById(R.id.register_progress)
        registerProgressText = findViewById(R.id.register_progress_text)
        registerStartButton = findViewById(R.id.register_start)
        registerStopButton = findViewById(R.id.register_stop)
        registerRetryButton = findViewById(R.id.register_retry)
        registerNextButton = findViewById(R.id.register_next)
        registerCloseButton = findViewById(R.id.register_close)

        registerStartButton.setOnClickListener {
            isRegistering = true
            isRecognizing = true
            RegisterTask().execute()
        }

        registerStopButton.setOnClickListener {
            isRegistering = false
            isRecognizing = false
        }

        registerRetryButton.setOnClickListener {
            tempAccList.clear()
            tempGyrList.clear()
            when (count) {
                1 -> firstVector.clear()
                2 -> secondVector.clear()
                else -> thirdVector.clear()
            }
            isRegistering = true
            isRecognizing = true
            RegisterTask().execute()
        }

        registerNextButton.setOnClickListener {
            count += 1
            isRegistering = true
            isRecognizing = true
            RegisterTask().execute()
        }

        registerCloseButton.setOnClickListener {
            count = 1
            firstVector.clear()
            secondVector.clear()
            thirdVector.clear()
            unlockLayout.visibility = View.VISIBLE
            registerLayout.visibility = View.GONE
            testLayout.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.GONE
            registerProgressBar.visibility = View.GONE
            registerProgressText.text = "모션을 등록하려면 시작 버튼을 눌러주세요."
            registerStartButton.visibility = View.VISIBLE
            registerStopButton.visibility = View.GONE
            registerRetryButton.visibility = View.GONE
            registerNextButton.visibility = View.GONE
            registerCloseButton.visibility = View.GONE
        }

        // Test
        testProgressBar = findViewById(R.id.test_progress)
        testProgressText = findViewById(R.id.test_result)

        val testButton = findViewById<MaterialButton>(R.id.test)
        testButton.setOnClickListener {
            unlockLayout.visibility = View.GONE
            registerLayout.visibility = View.GONE
            testLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.GONE
        }

        testStartButton = findViewById(R.id.test_start)
        testStartButton.setOnClickListener {
            isRegistering = true
            isRecognizing = true
            TestTask().execute()
        }

        testStopButton = findViewById(R.id.test_stop)
        testStopButton.setOnClickListener {
            isRegistering = false
            isRecognizing = false
        }

        testRetryButton = findViewById(R.id.test_retry)
        testRetryButton.setOnClickListener {
            isRegistering = true
            isRecognizing = true
            TestTask().execute()
        }

        testCloseButton = findViewById(R.id.test_close)
        testCloseButton.setOnClickListener {
            unlockLayout.visibility = View.VISIBLE
            registerLayout.visibility = View.GONE
            testLayout.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.GONE
            testProgressBar.visibility = View.GONE
            testProgressText.text = "테스트를 시작하려면 시작 버튼을 눌러주세요."
            testStartButton.visibility = View.VISIBLE
            testStopButton.visibility = View.GONE
            testRetryButton.visibility = View.GONE
            testCloseButton.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mIsBound && mService != null) {
            mService!!.closeConnection()
        }

        if (mIsBound) {
            unbindService(mConnection)
            mIsBound = false
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder) {
            mService = (service as LocalBinder).service
            ConnectTask().execute()
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            mService = null
            mIsBound = false
            unlockLayout.visibility = View.GONE
            registerLayout.visibility = View.GONE
            testLayout.visibility = View.GONE
            loadingLayout.visibility = View.GONE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.VISIBLE
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class ConnectTask : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
            unlockLayout.visibility = View.GONE
            registerLayout.visibility = View.GONE
            testLayout.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
            failLayout.visibility = View.GONE
            disconnectedLayout.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): Boolean {
            var isConnected = false
            var i = 3
            while (i > 0) {
                try {
                    Thread.sleep(1000)
                    mService!!.findPeers()
                    if (mIsBound && mService != null) {
                        isConnected = true
                        break
                    }
                    i--
                } catch (t: Throwable) {
                }
            }
            return isConnected
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (result!!) {
                unlockLayout.visibility = View.VISIBLE
                registerLayout.visibility = View.GONE
                testLayout.visibility = View.GONE
                loadingLayout.visibility = View.GONE
                failLayout.visibility = View.GONE
                disconnectedLayout.visibility = View.GONE
            } else {
                unlockLayout.visibility = View.GONE
                registerLayout.visibility = View.GONE
                testLayout.visibility = View.GONE
                loadingLayout.visibility = View.GONE
                failLayout.visibility = View.VISIBLE
                disconnectedLayout.visibility = View.GONE
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class RegisterTask : AsyncTask<Void, Float, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()

            registerProgressBar.visibility = View.VISIBLE
            when (count) {
                1 -> registerProgressText.text = "모션 인식 중... (1 / 3)"
                2 -> registerProgressText.text = "모션 인식 중... (2 / 3)"
                else -> registerProgressText.text = "모션 인식 중... (3 / 3)"
            }
            registerStartButton.visibility = View.GONE
            registerStopButton.visibility = View.VISIBLE
            registerRetryButton.visibility = View.GONE
            registerNextButton.visibility = View.GONE
            registerCloseButton.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): Int {
            while (isRecognizing) {
                try {
                    mService!!.sendData("DATA")
                } catch (t: Throwable) {
                }
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            when (count) {
                1 -> {
                    val convertedAcc = convertData(tempAccList)
                    val convertedGyr = convertData(tempGyrList)
                    firstVector = integrateVectors(convertedAcc, convertedGyr)

                    tempAccList.clear()
                    tempGyrList.clear()

                    registerProgressBar.visibility = View.GONE
                    registerProgressText.text = "1 / 3 등록 완료."
                    registerStartButton.visibility = View.GONE
                    registerStopButton.visibility = View.GONE
                    registerRetryButton.visibility = View.VISIBLE
                    registerNextButton.visibility = View.VISIBLE
                    registerCloseButton.visibility = View.GONE
                }
                2 -> {
                    val convertedAcc = convertData(tempAccList)
                    val convertedGyr = convertData(tempGyrList)
                    secondVector = integrateVectors(convertedAcc, convertedGyr)

                    tempAccList.clear()
                    tempGyrList.clear()

                    registerProgressBar.visibility = View.GONE
                    registerProgressText.text = "2 / 3 등록 완료."
                    registerStartButton.visibility = View.GONE
                    registerStopButton.visibility = View.GONE
                    registerRetryButton.visibility = View.VISIBLE
                    registerNextButton.visibility = View.VISIBLE
                    registerCloseButton.visibility = View.GONE
                }
                else -> {
                    val convertedAcc = convertData(tempAccList)
                    val convertedGyr = convertData(tempGyrList)
                    thirdVector = integrateVectors(convertedAcc, convertedGyr)

                    val dtw = DTW()
                    val one = dtw.compute(firstVector.toFloatArray(), secondVector.toFloatArray()).distance
                    val two = dtw.compute(secondVector.toFloatArray(), thirdVector.toFloatArray()).distance
                    val three = dtw.compute(thirdVector.toFloatArray(), firstVector.toFloatArray()).distance

                    Log.e("DTW RESULT", one.toString())

                    val tempDoubleArray = ArrayList<Double>()
                    tempDoubleArray.add(one)
                    tempDoubleArray.add(two)
                    tempDoubleArray.add(three)
                    val maxDistance = tempDoubleArray.max()!!
                    Log.e("MAX DISTANCE", maxDistance.toString())

                    val editor = sharedPrefs.edit()
                    for (i in 0 until thirdVector.size) {
                        editor.putFloat("FV_$i", thirdVector[i])
                    }
                    editor.putLong("max_distance", java.lang.Double.doubleToRawLongBits(maxDistance))
                    editor.apply()

                    tempAccList.clear()
                    tempGyrList.clear()

                    registerProgressBar.visibility = View.GONE
                    registerProgressText.text = "모션 등록 완료"
                    registerStartButton.visibility = View.GONE
                    registerStopButton.visibility = View.GONE
                    registerRetryButton.visibility = View.VISIBLE
                    registerNextButton.visibility = View.GONE
                    registerCloseButton.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    inner class TestTask : AsyncTask<Void, Float, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()
            testProgressBar.visibility = View.VISIBLE
            testProgressText.text = "모션 인식 중..."
            testProgressText.setTextColor(resources.getColor(R.color.text_color, theme))
            testStartButton.visibility = View.GONE
            testStopButton.visibility = View.VISIBLE
            testRetryButton.visibility = View.GONE
            testCloseButton.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): Int {
            while (isRecognizing) {
                try {
                    mService!!.sendData("DATA")
                } catch (t: Throwable) {
                }
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)

            val savedVector = ArrayList<Float>()
            for (i in 0..31) {
                savedVector.add(sharedPrefs.getFloat("FV_$i", 0f))
            }

            val dtw = DTW()
            val convertedAcc = convertData(tempAccList)
            val convertedGyr = convertData(tempGyrList)
            val attemptVector = integrateVectors(convertedAcc, convertedGyr)
            val distance = dtw.compute(attemptVector.toFloatArray(), savedVector.toFloatArray()).distance

            tempAccList.clear()
            tempGyrList.clear()

            val maxDistance = java.lang.Double.longBitsToDouble(sharedPrefs.getLong("max_distance", java.lang.Double.doubleToRawLongBits(0.0)))

            Log.e("DISTANCE", distance.toString())
            Log.e("MAX DISTANCE", maxDistance.toString())

            if (distance <= maxDistance * 0.7) {
                testProgressText.setTextColor(resources.getColor(R.color.colorPrimary, theme))
                testProgressText.text = "등록된 사용자와 일치합니다."
            } else {
                testProgressText.setTextColor(resources.getColor(R.color.red, theme))
                testProgressText.text = "등록된 사용자가 아닙니다."
            }

            testProgressBar.visibility = View.GONE
            testStartButton.visibility = View.GONE
            testStopButton.visibility = View.GONE
            testRetryButton.visibility = View.VISIBLE
            testCloseButton.visibility = View.VISIBLE
        }
    }

    private fun convertData(list: ArrayList<RawData>): ArrayList<ConvertedData> {
        val convertedVector = ArrayList<ConvertedData>()

        val size = list.size

        var meanX = 0f
        var meanY = 0f
        var meanZ = 0f
        var meanW = 0f
        for (element in list) {
            meanX += element.x
            meanY += element.y
            meanZ += element.z
            meanW += element.w
        }
        meanX /= size
        meanY /= size
        meanZ /= size
        meanW /= size

        Log.e("MEAN", "X: $meanX, Y: $meanY, Z: $meanZ, W: $meanW")

        var deviationX = 0f
        var deviationY = 0f
        var deviationZ = 0f
        var deviationW = 0f
        for (element in list) {
            val remainderX = element.x - meanX
            val remainderY = element.y - meanY
            val remainderZ = element.z - meanZ
            val remainderW = element.w - meanW

            deviationX += remainderX.pow(2)
            deviationY += remainderY.pow(2)
            deviationZ += remainderZ.pow(2)
            deviationW += remainderW.pow(2)
        }
        val tempX = deviationX / size
        val tempY = deviationY / size
        val tempZ = deviationZ / size
        val tempW = deviationW / size

        deviationX = sqrt(tempX)
        deviationY = sqrt(tempY)
        deviationZ = sqrt(tempZ)
        deviationW = sqrt(tempW)

        Log.e("DEVIATION", "X: $deviationX, Y: $deviationY, Z: $deviationZ, W: $deviationW")

        if (deviationX == 0f) {
            deviationX = 1f
        }

        if (deviationY == 0f) {
            deviationY = 1f
        }

        if (deviationZ == 0f) {
            deviationZ = 1f
        }

        if (deviationW == 0f) {
            deviationW = 1f
        }

        var skewnessX = 0f
        var skewnessY = 0f
        var skewnessZ = 0f
        var skewnessW = 0f
        var kurtosisX = 0f
        var kurtosisY = 0f
        var kurtosisZ = 0f
        var kurtosisW = 0f
        for (element in list) {
            val remainderX = element.x - meanX
            val remainderY = element.y - meanY
            val remainderZ = element.z - meanZ
            val remainderW = element.w - meanW

            skewnessX += remainderX.pow(3) / (meanX * deviationX.pow(3))
            skewnessY += remainderY.pow(3) / (meanY * deviationY.pow(3))
            skewnessZ += remainderZ.pow(3) / (meanZ * deviationZ.pow(3))
            skewnessW += remainderW.pow(3) / (meanW * deviationW.pow(3))

            kurtosisX += remainderX.pow(4) / (meanX * deviationX.pow(4))
            kurtosisY += remainderY.pow(4) / (meanY * deviationY.pow(4))
            kurtosisZ += remainderZ.pow(4) / (meanZ * deviationZ.pow(4))
            kurtosisW += remainderW.pow(4) / (meanW * deviationW.pow(4))
        }

        Log.e("SKEWNESS", "X: $skewnessX, Y: $skewnessY, Z: $skewnessZ, W: $skewnessW")
        Log.e("KURTOSIS", "X: $kurtosisX, Y: $kurtosisY, Z: $kurtosisZ, W: $kurtosisW")

        convertedVector.add(ConvertedData(meanX, deviationX, skewnessX, kurtosisX))
        convertedVector.add(ConvertedData(meanY, deviationY, skewnessY, kurtosisY))
        convertedVector.add(ConvertedData(meanZ, deviationZ, skewnessZ, kurtosisZ))
        convertedVector.add(ConvertedData(meanW, deviationW, skewnessW, kurtosisW))

        return convertedVector
    }

    private fun integrateVectors(
        acc: ArrayList<ConvertedData>,
        gyr: ArrayList<ConvertedData>
    ): ArrayList<Float> {
        // x, y, z, w 순서대로 featureVector에 저장
        val featureVector = ArrayList<Float>()
        for (i in 0..3) {
            val accData = acc[i]
            val gyrData = gyr[i]
            featureVector.add(accData.mean)
            featureVector.add(accData.deviation)
            featureVector.add(accData.skewness)
            featureVector.add(accData.kurtosis)
            featureVector.add(gyrData.mean)
            featureVector.add(gyrData.deviation)
            featureVector.add(gyrData.skewness)
            featureVector.add(gyrData.kurtosis)
        }
        return featureVector
    }

    companion object {
        lateinit var unlockLayout: ConstraintLayout
        lateinit var registerLayout: ConstraintLayout
        lateinit var testLayout: ConstraintLayout
        lateinit var loadingLayout: LinearLayout
        lateinit var failLayout: LinearLayout
        lateinit var disconnectedLayout: LinearLayout
        lateinit var adapter: MainAdapter

        var isRegistering = false
        val tempAccList = ArrayList<RawData>()
        val tempGyrList = ArrayList<RawData>()

        fun addText(data: String) {
            val dataList = data.split("%")
            val hrm = dataList[0]
            val acc = dataList[1]
            val gyr = dataList[2]
            val prs = dataList[3]

            adapter.addText(
                SensorData(
                    hrm,
                    acc,
                    gyr,
                    prs
                )
            )
        }

        fun addData(data: String) {
            // ACC의 X, Y, Z, W (제 4차원의 값) 을 받아와 그것들의 평균, 표준편차, 왜도, 첨도를 구한다.
            // GYR도 똑같이 구한다.
            // 구한 것들을 배열로 나열하면 데이터는 준비 완료. (우리가 필요한 데이터는 FS)
            // FS = { (ACC X축 평균, ACC X축 표준편차, ACC X축 왜도, ACC X축 경도),
            //         (ACC Y축 평균, ...),
            //         (ACC Z축 평균, ...),
            //         (ACC W축 평균, ...),
            //         (GYR X축 평균, GYR X축 표준편차, GYR X축 왜도, GYR X축 경도),
            //         ...
            //       }
            //
            //
            // 4차원 W는 루트(X축^2 + Y축^2 + Z축^2) 로 구할 수 있다.
            // 편차는 원래값 - 표준
            // 분산은 편차^2의 합 / 총 변량 개수  ==  시그마{(원래값 - 표준)^2} / 개수
            // 표준편차는 루트(변량)
            // 왜도(Skewness)는 (편차^3) / (개수*표준편차^3)의 총합  ==  시그마{(편차^3) / (총개수*표준편차^3)}
            // 첨도(Kurtosis)는 (편차^4) / (개수*표준편차^4)의 총합  ==  시그마{(편차^4) / (총개수*표준편차^4)}
            val dataList = data.split("%")
            val acc = dataList[1]
            val gyr = dataList[2]
            val accDataList = acc.split(", ")
            val gyrDataList = gyr.split(", ")
            val accX = accDataList[0].substring(1).toFloat()
            val accY = accDataList[1].toFloat()
            val accZ = accDataList[2].substring(0, accDataList[2].indexOf(")")).toFloat()
            val accW = sqrt(accX.pow(2) + accY.pow(2) + accZ.pow(2))
            val gyrX = gyrDataList[0].substring(1).toFloat()
            val gyrY = gyrDataList[1].toFloat()
            val gyrZ = gyrDataList[2].substring(0, gyrDataList[2].indexOf(")")).toFloat()
            val gyrW = sqrt(gyrX.pow(2) + gyrY.pow(2) + gyrZ.pow(2))

            tempAccList.add(
                RawData(
                    accX,
                    accY,
                    accZ,
                    accW
                )
            )
            tempGyrList.add(
                RawData(
                    gyrX,
                    gyrY,
                    gyrZ,
                    gyrW
                )
            )
        }
    }
}
