package com.example.solargarden

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("ranking/{semana}")
    suspend fun getRanking(@Path("semana") semana: String): List<EmpleadoRanking>

    @GET("empleado/{oid}/diagnostico-campo")
    suspend fun getDiagnosticoCampo(@Path("oid") oid: Int): Diagnostico

    @GET("empleado/{oid}/diagnostico-empaque")
    suspend fun getDiagnosticoEmpaque(@Path("oid") oid: Int): Diagnostico

    @GET("empleado/{oid}/historico-campo")
    suspend fun getHistoricoCampo(@Path("oid") oid: Int): List<PuntoHistorico>

    @GET("empleado/{oid}/historico-empaque")
    suspend fun getHistoricoEmpaque(@Path("oid") oid: Int): List<PuntoHistorico>

    @GET("empleado/{oid}/asistencia")
    suspend fun getAsistencia(@Path("oid") oid: Int): Asistencia

    @GET("empleado/{oid}/atributos-campo")
    suspend fun getAtributosCampo(@Path("oid") oid: Int): Atributos

    @GET("empleado/{oid}/atributos-empaque")
    suspend fun getAtributosEmpaque(@Path("oid") oid: Int): Atributos

    @GET("empleado/{oid}/con-meta")
    suspend fun getConMeta(@Path("oid") oid: Int): List<ActividadConMeta>

    @GET("empleado/{oid}/sin-meta")
    suspend fun getSinMeta(@Path("oid") oid: Int): List<ActividadSinMeta>

    @GET("empleado/{oid}/tendencia-diaria")
    suspend fun getTendenciaDiaria(@Path("oid") oid: Int): List<PuntoDiario>

    @GET("empleado/{oid}/empaque")
    suspend fun getEmpaque(@Path("oid") oid: Int): List<ProductoEmpaque>
}