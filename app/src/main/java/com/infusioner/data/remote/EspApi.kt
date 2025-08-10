package com.infusioner.data.remote
import com.infusioner.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ESP32Api {
  @GET("/status") suspend fun status(): StatusEspDto
  @POST("/start") suspend fun start(@Body req: StartEspReq): SimpleResp
  @POST("/stop") suspend fun stop(): SimpleResp
  @POST("/pause") suspend fun pause(): SimpleResp
  @POST("/resume") suspend fun resume(): SimpleResp
  @POST("/extend") suspend fun extend(@Body req: ExtendReq): SimpleResp
}
