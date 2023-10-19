package com.example.phasmobile.util

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.json.JSONObject


private const val TAG = "WebSocketListener"

class WebSocketListener(private val viewModel: MainViewModel) : okhttp3.WebSocketListener() {
    private lateinit var client: OkHttpClient
    private var webSocket: WebSocket? = null

    fun run() {
        if (webSocket != null) {
            sendName()
            return
        }
        // Create a trust manager that does not validate certificate chains
        val trustAllCerts: Array<TrustManager> = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {

                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }

            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory


        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

//        val request = Request.Builder()
//            .url("wss://socketsbay.com/wss/v2/1/demo/")
//            .build()
        val request = Request.Builder()
            .url("wss://192.168.1.199:2000")
            .build()
        webSocket = client.newWebSocket(request, this)

        client.dispatcher.executorService.shutdown()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d(TAG, "onOpen")
        viewModel.updateConnected(true)

        sendName()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        Log.d(TAG, "Received bytes message: $bytes")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d(TAG, "Received text message: $text")

        val json = JSONObject(text);

        if(json.has("Sim")) {
            val sim = json.getJSONObject("Sim")

            val update = GameState(
                ghostLocation = sim.getInt("ghost_location"),
                favoriteRoom = sim.getInt("favorite_room"),
                orbsVisible = sim.getBoolean("ghost_orbs_visible"),
                ambientTemp = sim.getInt("ambient_temp"),
                ghostRoomTemp = sim.getInt("ghost_room_temp"),
                emfLevel = sim.getInt("emf_level")
            )
            viewModel.updateGamestate(update)
        }
    }


    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.d(TAG, "onClosing: $code $reason")
        viewModel.updateConnected(false)
        this.webSocket = null
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.d(TAG, "onClosed: $code $reason")
    }

    fun sendName() {
        val name = viewModel.uiState.value.name
        sendJsonMessage(
            "JoinLobby",
            hashMapOf("name" to name)
        )
    }

    fun sendJsonMessage(messageId: String, args: HashMap<String, Any?>) {
        var contents = JSONObject()
        for ((key, value) in args) {
            contents.put(key, value)
        }

        var message = JSONObject()
        message.put(messageId, contents)

        webSocket?.send(message.toString())
    }

}
