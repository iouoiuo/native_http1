package com.brainants.nativehttp.native_http

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.HashMap

/** NativeHttpPlugin */
public class NativeHttpPlugin : FlutterPlugin, MethodCallHandler {
    var client = OkHttpClient()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

        val channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "native_http")
        channel.setMethodCallHandler(NativeHttpPlugin())
    }

    companion object {

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "native_http")
            channel.setMethodCallHandler(NativeHttpPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "native_http/request") {
            val url = call.argument<String>("url")!!
            val method = call.argument<String>("method")!!
            var headers = call.argument<HashMap<String, String>>("headers")
            var body = call.argument<HashMap<String, String>>("body")
            if (headers == null) headers = HashMap()
            if (body == null) body = HashMap()
            sendRequest(url, method, headers, body, result)
        } else {
            result.notImplemented()
        }
    }

    val JSON: MediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()

    fun sendRequest(url: String, method: String, headers: HashMap<String, String>, body: HashMap<String, String>, @NonNull result: Result) {
        
        val client = OkhttpUtils.client
        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, param.toString())
        val builder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
        if (headers != null) {
            builder.headers(headers)
        }
        
        val mHandler = Handler(Looper.getMainLooper())
        client.newCall(builder.build()).enqueue(
                object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        mHandler.post {
                            result.error(e.message?:"", e.localizedMessage?:"", null)
                        }
                    }

                    override fun onResponse(call: Call, r: Response) {
                        val response = HashMap<String, Any>()
                        response["code"] = r.code
                        response["body"] = r.body!!.string()
                        mHandler.post {
                            result.success(response)
                        }
                    }
                }
        )
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    }
}
  class Param : HashMap<String, Any>() {

        override fun toString(): String {
            val builder = StringBuilder()
            if (containsKey("key")) {
                remove("key")
            }
            for ((key, value) in this) {
                builder.append(key).append("=").append(urlEncode(value)).append("&")
            }
            if (builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
            return builder.toString()
        }

        fun urlEncode(str: Any?): String {
            return if (str == null) {
                ""
            } else URLEncoder.encode(str.toString())
        }
    }
