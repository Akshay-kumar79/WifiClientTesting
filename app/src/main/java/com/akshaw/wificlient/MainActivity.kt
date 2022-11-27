package com.akshaw.wificlient

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.akshaw.wificlient.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var SERVER_IP: String = ""
    var SERVER_PORT = 0

    private var socket: Socket? = null

    var outputStream: OutputStream? = null
    var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etIP.setText("192.168.43.187")
        binding.etPort.setText("8080")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 21)
        }

//        binding.etIP.setText(getCurrentSsid(this))

        binding.btnConnect.setOnClickListener {
            binding.tvMessages.text = ""
            SERVER_IP = binding.etIP.text.toString().trim()
            SERVER_PORT = binding.etPort.text.toString().trim().toInt()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    socket = Socket()
                    socket!!.connect(InetSocketAddress(SERVER_IP, SERVER_PORT))

                    inputStream = socket!!.getInputStream()
                    outputStream = socket!!.getOutputStream()

                    withContext(Dispatchers.Main) {
                        binding.tvMessages.text = "Connected\n"
                    }

                    val buffer = ByteArray(1024)
                    var bytes: Int
                    while (socket != null) {
                        bytes = inputStream!!.read(buffer)
                        if (bytes > 0) {
                            val finalByte = bytes
                            withContext(Dispatchers.Main) {
                                val message = String(buffer, 0, finalByte)
                                binding.tvMessages.append("server: $message\n")
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.v("MYTAG", "error: ${e.localizedMessage}")
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val message: String = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        outputStream?.let {
                            it.write(message.toByteArray())
                            Log.v("MYTAG", "error: ${message}")
                        }

                        withContext(Dispatchers.Main){
                            binding.tvMessages.append("client: $message\n")
                            binding.etMessage.setText("")
                        }
                    } catch (e: IOException) {
                        Log.v("MYTAG", "error: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

//    fun getCurrentSsid(context: Context): String? {
//        var ssid: String? = null
//        val connManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//        if (networkInfo!!.isConnected) {
//            val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
//            val connectionInfo = wifiManager.connectionInfo
//            if (connectionInfo != null && connectionInfo.ssid.isNotBlank()) {
//                ssid = connectionInfo.macAddress
//            }
//        }
//        return ssid
//    }
}