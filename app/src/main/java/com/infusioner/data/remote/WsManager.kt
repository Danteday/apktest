package com.infusioner.data.remote
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(private val client: OkHttpClient){
  private val _events = MutableSharedFlow<Pair<String,String>>(extraBufferCapacity=64, onBufferOverflow=BufferOverflow.DROP_OLDEST)
  val events: SharedFlow<Pair<String,String>> = _events
  private val sockets = mutableMapOf<String, WebSocket>()
  fun connect(id:String, ip:String){
    if (sockets.containsKey(id)) return
    val req = Request.Builder().url("ws://%s:%d/".format(ip,81)).build()
    val ws = client.newWebSocket(req, object: WebSocketListener(){
      override fun onMessage(webSocket: WebSocket, text: String) { _events.tryEmit(id to text) }
      override fun onMessage(webSocket: WebSocket, bytes: ByteString) { _events.tryEmit(id to bytes.utf8()) }
      override fun onFailure(webSocket: WebSocket, t: Throwable, r: Response?) { sockets.remove(id) }
      override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { sockets.remove(id) }
    })
    sockets[id]=ws
  }
  fun disconnect(id:String){ sockets.remove(id)?.close(1000,"bye") }
}
