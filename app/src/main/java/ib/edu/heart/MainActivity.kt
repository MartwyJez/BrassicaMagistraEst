package edu.ib.heart

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.Pair
import com.google.android.material.snackbar.Snackbar
import com.opencsv.CSVWriter
import com.polar.androidblesdk.DialogUtility
import com.polar.androidblesdk.R
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
<<<<<<< HEAD
import com.polar.sdk.api.model.*
import ib.edu.heart.IntervalCountChooserActivity
=======
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarExerciseEntry
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import ib.edu.heart.HeartBeatActivity
>>>>>>> origin/dev2
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {



    companion object {
        private const val TAG = "MainActivity"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    // ATTENTION! Replace with the device ID from your device.
    private var deviceId = "5D446327"

    var entries = ArrayList<Int>()

    private val api: PolarBleApi by lazy {
        // Notice PolarBleApi.ALL_FEATURES are enabled
        PolarBleApiDefaultImpl.defaultImplementation(applicationContext, PolarBleApi.ALL_FEATURES)
    }
    private var recordingStartStopDisposable: Disposable? = null
    private var recordingStatusReadDisposable: Disposable? = null


    private var deviceConnected = false
    private var bluetoothEnabled = false
    private var exerciseEntries: MutableList<PolarExerciseEntry> = mutableListOf()

    private lateinit var connectButton: Button
    private lateinit var startH10RecordingButton: Button
    private lateinit var stopH10RecordingButton: Button
    private lateinit var readH10RecordingStatusButton: Button
    private lateinit var textView: TextView
    private lateinit var nextLayout: Button
    private var liczba: Int = 0
    private var nagrywaj: String = ""


    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "version: " + PolarBleApiDefaultImpl.versionInfo())
        connectButton = findViewById(R.id.connect_button)
        startH10RecordingButton = findViewById(R.id.start_h10_recording)
        stopH10RecordingButton = findViewById(R.id.stop_h10_recording)
        readH10RecordingStatusButton = findViewById(R.id.h10_recording_status)
        nextLayout = findViewById(R.id.next_layout)
        textView = findViewById(R.id.textv2)
        api.setPolarFilter(false)
        api.setApiLogger { s: String -> Log.d(API_LOGGER_TAG, s) }
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BLE power: $powered")
                bluetoothEnabled = powered
                if (powered) {
                    enableAllButtons()
                    showToast("Włączony Bluetooth na telefonie")
                } else {
                    disableAllButtons()
                    showToast("Wyłączony Bluetooth na telefonie")
                }
            }

            @SuppressLint("StringFormatInvalid")
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Połaczono z: " + polarDeviceInfo.deviceId)
                deviceId = polarDeviceInfo.deviceId
                deviceConnected = true
                val buttonText = getString(R.string.disconnect_from_device, deviceId)
                toggleButtonDown(connectButton, buttonText)
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Łączenie z: " + polarDeviceInfo.deviceId)
            }

            @SuppressLint("StringFormatInvalid")
            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Rozłączono: " + polarDeviceInfo.deviceId)
                deviceConnected = false
                val buttonText = getString(R.string.connect_to_device, deviceId)
                toggleButtonUp(connectButton, buttonText)
            }

            override fun streamingFeaturesReady(
                identifier: String, features: Set<DeviceStreamingFeature>
            ) {
                for (feature in features) {
                    Log.d(TAG, "Przesyłanie $feature jest gotowe")
                }
            }

            override fun hrFeatureReady(identifier: String) {
                Log.d(TAG, "HR gotowe: $identifier")
                // hr notifications are about to start
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d(TAG, "uuid: $uuid value: $value")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "POZIOM BATERII: $level")
            }

            override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
                Log.d(TAG, "HR wartosć: ${data.hr} rrsMs: ${data.rrsMs} rr: ${data.rrs} contact: ${data.contactStatus} , ${data.contactStatusSupported}")
                if(nagrywaj.equals("start")){
                    liczba += data.rrs.size
                }
            }

            override fun polarFtpFeatureReady(s: String) {
                Log.d(TAG, "FTP ready")
            }
        })

        connectButton.text = getString(R.string.connect_to_device, deviceId)
        connectButton.setOnClickListener {

            val intent = Intent(this, IntervalCountChooserActivity::class.java)
            startActivity(intent)

            try {
                if (deviceConnected) {
                    api.disconnectFromDevice(deviceId)
                } else {
                    api.connectToDevice(deviceId)
                }
            } catch (polarInvalidArgument: PolarInvalidArgument) {
                val attempt = if (deviceConnected) {
                    "Rozłącz"
                } else {
                    "połącz"
                }
                Log.e(TAG, "Nie udało się $attempt. Reason $polarInvalidArgument ")
            }
        }

        startH10RecordingButton.setOnClickListener {
            val isDisposed = recordingStartStopDisposable?.isDisposed ?: true
            if (isDisposed) {
                val recordIdentifier = "TEST_APP_ID"
                val recordingStartOk = "Rozpoczął sie zapis danych o id $recordIdentifier"
                Log.d(TAG, "recordingStartOk")
                showSnackbar(recordingStartOk)
                nagrywaj = "start"
            }
        }

        nextLayout.setOnClickListener {
            intent = Intent(this, HeartBeatActivity::class.java)
            startActivity(intent)

        }



        stopH10RecordingButton.setOnClickListener {
            val isDisposed = recordingStartStopDisposable?.isDisposed ?: true
            if (isDisposed) {
                val recordingStopOk = "Zatrzymano rejestracje"
                Log.d(TAG, "recordingStopOk")
                showSnackbar(recordingStopOk)
                nagrywaj = "stop"
                textView.text = liczba.toString()
                liczba = 0

            }
        }

        readH10RecordingStatusButton.setOnClickListener {
            val isDisposed = recordingStatusReadDisposable?.isDisposed ?: true
            if (isDisposed) {
                recordingStatusReadDisposable = api.requestRecordingStatus(deviceId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { pair: Pair<Boolean, String> ->

                            val recordingOn = pair.first
                            val recordingId = pair.second

                            val recordingStatus = if (!recordingOn && recordingId.isEmpty()) {
                                "Status: rejestracja wyłączona"
                            } else if (!recordingOn && recordingId.isNotEmpty()) {
                                "Status: rejestracja wyłączona\n\n" +
                                        "Zapis o id $recordingId jest zapisany na urządzeniu"
                            } else if (recordingOn && recordingId.isNotEmpty()) {
                                "Status: rejestracja jest włączona\n\n" +
                                        "Zapis o id $recordingId jest rejestrowany"
                            } else if (recordingOn && recordingId.isEmpty()) {
                                "Status czujnika jest nie określony"
                            } else {
                                // This state is unreachable and should never happen
                                "Status: BŁĄD"
                            }
                            Log.d(TAG, recordingStatus)
                            showDialog("Status urządzenia", recordingStatus)
                        },
                        { error: Throwable ->
                            val recordingStatusReadError = "Nie udało się wczytać statusu urządznia. Reason: $error"
                            Log.e(TAG, recordingStatusReadError)
                            showSnackbar(recordingStatusReadError)
                        }
                    )
            } else {
                Log.d(TAG, "Żądanie statusu rejestracji jest już w toku.")
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        }
    }


    fun writeDataAtOnce(filePath: String?) {

        // first create file object for file placed at location
        // specified by filepath
        val file = File(filePath)
        try {
            // create FileWriter object with file as parameter
            val outputfile = FileWriter(file)

            // create CSVWriter object filewriter object as parameter
            val writer = CSVWriter(outputfile)

            // create a List which contains String array
            val data: MutableList<Array<String>> = ArrayList()
            data.add(arrayOf("Name", "Class", "Marks"))
            data.add(arrayOf("Aman", "10", "620"))
            data.add(arrayOf("Suraj", "10", "630"))
            writer.writeAll(data)

            // closing writer connection
            writer.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (index in 0..grantResults.lastIndex) {
                if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
                    disableAllButtons()
                    Log.w(TAG, "Brak wystarczających uprawnień")
                    showToast("Brak wystarczających uprawnień")
                    return
                }
            }
            Log.d(TAG, "Potrzebne uprawnienia są przyznawane")
            enableAllButtons()
        }
    }

    private fun requestStreamSettings(
        identifier: String,
        feature: DeviceStreamingFeature
    ): Flowable<PolarSensorSetting> {

        val availableSettings = api.requestStreamSettings(identifier, feature)
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { error: Throwable ->
                val errorString = "Settings are not available for feature $feature. REASON: $error"
                Log.w(TAG, errorString)
                showToast(errorString)
                PolarSensorSetting(emptyMap())
            }
        val allSettings = api.requestFullStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Log.w(
                    TAG,
                    "Full stream settings are not available for feature $feature. REASON: $error"
                )
                PolarSensorSetting(emptyMap())
            }
        return Single.zip(availableSettings, allSettings) { available: PolarSensorSetting, all: PolarSensorSetting ->
            if (available.settings.isEmpty()) {
                throw Throwable("Settings are not available")
            } else {
                Log.d(TAG, "Feature " + feature + " available settings " + available.settings)
                Log.d(TAG, "Feature " + feature + " all settings " + all.settings)
                return@zip android.util.Pair(available, all)
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
            .flatMap(
                Function { sensorSettings: android.util.Pair<PolarSensorSetting, PolarSensorSetting> ->
                    DialogUtility.showAllSettingsDialog(
                        this@MainActivity,
                        sensorSettings.first.settings,
                        sensorSettings.second.settings
                    ).toFlowable()
                } as Function<android.util.Pair<PolarSensorSetting, PolarSensorSetting>, Flowable<PolarSensorSetting>>
            )
    }


    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        api.foregroundEntered()
    }

    public override fun onDestroy() {
        super.onDestroy()
        api.shutDown()
    }

    private fun toggleButtonDown(button: Button, text: String? = null) {
        toggleButton(button, true, text)
    }

    private fun toggleButtonDown(button: Button, @StringRes resourceId: Int) {
        toggleButton(button, true, getString(resourceId))
    }

    private fun toggleButtonUp(button: Button, text: String? = null) {
        toggleButton(button, false, text)
    }

    private fun toggleButtonUp(button: Button, @StringRes resourceId: Int) {
        toggleButton(button, false, getString(resourceId))
    }

    private fun toggleButton(button: Button, isDown: Boolean, text: String? = null) {
        if (text != null) button.text = text

        var buttonDrawable = button.background
        buttonDrawable = DrawableCompat.wrap(buttonDrawable!!)
        if (isDown) {
            DrawableCompat.setTint(buttonDrawable, resources.getColor(R.color.primaryDarkColor))
        } else {
            DrawableCompat.setTint(buttonDrawable, resources.getColor(R.color.primaryColor))
        }
        button.background = buttonDrawable
    }
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()

    }

    private fun showSnackbar(message: String) {
        val contextView = findViewById<View>(R.id.buttons_container)
        Snackbar.make(contextView, message, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // Respond to positive button press
            }
            .show()
    }

    private fun disableAllButtons() {
        connectButton.isEnabled = false
        startH10RecordingButton.isEnabled = false
        stopH10RecordingButton.isEnabled = false
        readH10RecordingStatusButton.isEnabled = false

    }

    private fun enableAllButtons() {
        connectButton.isEnabled = true
        startH10RecordingButton.isEnabled = true
        stopH10RecordingButton.isEnabled = true
        readH10RecordingStatusButton.isEnabled = true
    }
    private fun disposeAllStreams() {
       }

}