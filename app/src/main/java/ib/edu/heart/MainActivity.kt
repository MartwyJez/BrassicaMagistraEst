package edu.ib.heart

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import ib.edu.heart.*

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Function

import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DataBaseSensor


    companion object {
        private const val TAG = "MainActivity"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    // ATTENTION! Replace with the device ID from your device.
    private var deviceId = ""

    private val api: PolarBleApi by lazy {
        // Notice PolarBleApi.ALL_FEATURES are enabled
        PolarBleApiDefaultImpl.defaultImplementation(applicationContext, PolarBleApi.ALL_FEATURES)
    }

    private var deviceConnected = false
    private var bluetoothEnabled = false

    private lateinit var connectButton: Button
    private lateinit var settButton: Button
    private lateinit var sensorButton: Button
    private lateinit var textView: TextView

    private var liczba: Int = 0
    private var nagrywaj: String = ""


    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "version: " + PolarBleApiDefaultImpl.versionInfo())

        databaseHelper = DataBaseSensor(this)
        deviceId = databaseHelper.lastRow().toString()



        connectButton = findViewById(R.id.connect_button)
        sensorButton = findViewById(R.id.czujnik)
        settButton = findViewById(R.id.settings)
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
                showToast("Nawiązano połączenie z czujnikiem")
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Łączenie z: " + polarDeviceInfo.deviceId)
            }

            @SuppressLint("StringFormatInvalid")
            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Rozłączono: " + polarDeviceInfo.deviceId)
                showToast("Przerwano połączenie z czujnikiem")
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

        sensorButton.setOnClickListener {
            val intent = Intent(this, ChangeIDActivity::class.java)
            startActivity(intent)
        }
        connectButton.setOnClickListener {

            if(deviceId.isNullOrEmpty()){
                showToast("Nie uzupelniono ID czujnika")
            }
            else {

                val intent = Intent(this, CodesChooserActivity::class.java)
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

                    val toast = Toast.makeText(
                        applicationContext, "Nieudane połączenie z czujnikiem.", Toast.LENGTH_LONG
                    )
                    toast.show()
                    Log.e(TAG, "Nie udało się $attempt. Reason $polarInvalidArgument ")
                }
            }
        }

        settButton.setOnClickListener {

            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        }


        requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
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

    private fun toggleButtonUp(button: Button, text: String? = null) {
        toggleButton(button, false, text)
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

    private fun disableAllButtons() {
        connectButton.isEnabled = false


    }

    private fun enableAllButtons() {
        connectButton.isEnabled = true

    }
    private fun disposeAllStreams() {
       }

}