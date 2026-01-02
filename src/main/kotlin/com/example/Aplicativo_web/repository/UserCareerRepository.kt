package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.UserCareerEntity
import com.example.Aplicativo_web.entity.UserCareerId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserCareerRepository : JpaRepository<UserCareerEntity, UserCareerId> {

    @Modifying
    @Query("delete from UserCareerEntity uc where uc.id.userId = :userId")
    fun deleteAllByUserId(@Param("userId") userId: Long)

    @Query("select uc.id.careerId from UserCareerEntity uc where uc.id.userId = :userId")
    fun findCareerIdsByUserId(@Param("userId") userId: Long): List<Long>
}
