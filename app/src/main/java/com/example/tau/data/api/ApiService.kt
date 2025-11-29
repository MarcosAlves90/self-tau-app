package com.example.tau.data.api

import com.example.tau.data.DisciplineRequest
import com.example.tau.data.DisciplineResponse
import com.example.tau.data.TaskRequest
import com.example.tau.data.TaskResponse
import com.example.tau.data.ScheduleRequest
import com.example.tau.data.ScheduleResponse
import com.example.tau.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/usuarios")
    suspend fun createUser(@Body user: User): Response<Void>

    @POST("api/usuarios/login")
    suspend fun login(@Body user: User): Response<User>

    @POST("api/disciplinas")
    suspend fun createDiscipline(@Body discipline: DisciplineRequest): Response<DisciplineResponse>

    @GET("api/disciplinas")
    suspend fun getDisciplines(@Query("usuario_id") usuarioId: Int): Response<List<DisciplineResponse>>

    @PUT("api/disciplinas/{id}")
    suspend fun updateDiscipline(
        @Path("id") id: Int,
        @Body discipline: DisciplineRequest
    ): Response<Void>

    @DELETE("api/disciplinas/{id}")
    suspend fun deleteDiscipline(@Path("id") id: Int): Response<Void>

    @POST("api/tarefas")
    suspend fun createTask(@Body task: TaskRequest): Response<TaskResponse>

    @GET("api/tarefas")
    suspend fun getTasks(
        @Query("usuario_id") usuarioId: Int,
        @Query("disciplina_id") disciplinaId: Int? = null,
        @Query("status") status: Boolean? = null,
        @Query("data_inicio") dataInicio: Long? = null,
        @Query("data_fim") dataFim: Long? = null
    ): Response<List<TaskResponse>>

    @PUT("api/tarefas/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body task: TaskRequest
    ): Response<Void>

    @DELETE("api/tarefas/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Void>

    @POST("api/horarios")
    suspend fun createSchedule(@Body schedule: ScheduleRequest): Response<ScheduleResponse>

    @GET("api/horarios")
    suspend fun getSchedules(@Query("usuario_id") usuarioId: Int): Response<List<ScheduleResponse>>

    @PUT("api/horarios/{id}")
    suspend fun updateSchedule(
        @Path("id") id: Int,
        @Body schedule: ScheduleRequest
    ): Response<Void>

    @DELETE("api/horarios/{id}")
    suspend fun deleteSchedule(@Path("id") id: Int): Response<Void>

}
