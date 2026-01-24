package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StudentRepository : JpaRepository<StudentEntity, Long> {

    fun findByDni(dni: String): Optional<StudentEntity>

    @Query("""
        select s from StudentEntity s
        where s.career.id in :careerIds
        order by s.lastName asc, s.firstName asc
    """)
    fun findAllByCareerIds(@Param("careerIds") careerIds: List<Long>): List<StudentEntity>

    fun findAllByCareerId(careerId: Long): List<StudentEntity>
    fun findAllByCareerIdAndCoordinatorIsNull(careerId: Long): List<StudentEntity>

    fun findAllByTutorId(tutorId: Long): List<StudentEntity>
    fun findByIdAndTutor_Id(studentId: Long, tutorId: Long): StudentEntity?

    fun findByAcademicPeriod_Id(academicPeriodId: Long): List<StudentEntity>

    // ✅ NUEVO: buscar el alumno EN ESE PERIODO
    fun findByDniAndAcademicPeriod_Id(dni: String, academicPeriodId: Long): Optional<StudentEntity>

    // ✅ NUEVO: contar por periodo
    @Query("select count(s) from StudentEntity s where s.academicPeriod.id = :periodId")
    fun countByAcademicPeriod(@Param("periodId") periodId: Long): Long

    // ✅ NUEVO: contar estudiantes sin periodo
    @Query("select count(s) from StudentEntity s where s.academicPeriod is null")
    fun countWithoutAcademicPeriod(): Long

    fun findAllByCareerIdAndAcademicPeriod_Id(careerId: Long, academicPeriodId: Long): List<StudentEntity>

    fun findAllByCareerIdAndAcademicPeriod_IdAndCoordinatorIsNull(
        careerId: Long,
        academicPeriodId: Long
    ): List<StudentEntity>

    // ===== NUEVO: COORDINATOR + PERIODO =====
    @Query("""
        select s from StudentEntity s
        where s.coordinator.id = :coordinatorId
          and s.academicPeriod.id = :periodId
        order by s.lastName asc, s.firstName asc
    """)
    fun findAllByCoordinatorIdAndAcademicPeriodId(
        @Param("coordinatorId") coordinatorId: Long,
        @Param("periodId") periodId: Long
    ): List<StudentEntity>

    @Query("""
        select s from StudentEntity s
        where s.id = :studentId
          and s.coordinator.id = :coordinatorId
          and s.academicPeriod.id = :periodId
    """)
    fun findByIdAndCoordinatorIdAndAcademicPeriodId(
        @Param("studentId") studentId: Long,
        @Param("coordinatorId") coordinatorId: Long,
        @Param("periodId") periodId: Long
    ): StudentEntity?

    // ===== NUEVO: TUTOR + PERIODO =====
    @Query("""
        select s from StudentEntity s
        where s.tutor.id = :tutorId
          and s.academicPeriod.id = :periodId
        order by s.lastName asc, s.firstName asc
    """)
    fun findAllByTutorIdAndAcademicPeriodId(
        @Param("tutorId") tutorId: Long,
        @Param("periodId") periodId: Long
    ): List<StudentEntity>

    @Query("""
        select s from StudentEntity s
        where s.id = :studentId
          and s.tutor.id = :tutorId
          and s.academicPeriod.id = :periodId
    """)
    fun findByIdAndTutorIdAndAcademicPeriodId(
        @Param("studentId") studentId: Long,
        @Param("tutorId") tutorId: Long,
        @Param("periodId") periodId: Long
    ): StudentEntity?

    @Query("""
    select s from StudentEntity s
    where s.career.id in :careerIds
      and s.academicPeriod.id = :periodId
    order by s.lastName asc, s.firstName asc
    """)
    fun findAllByCareerIdsAndAcademicPeriodId(
        @Param("careerIds") careerIds: List<Long>,
        @Param("periodId") periodId: Long
    ): List<StudentEntity>

    @Query("select count(i) from IncidentEntity i where i.student.id = :studentId")
    fun countIncidentsByStudent(@Param("studentId") studentId: Long): Long

    @Query("select count(o) from ObservationEntity o where o.student.id = :studentId")
    fun countObservationsByStudent(@Param("studentId") studentId: Long): Long

    @Query(
        value = "select count(*) from incident where student_id = :studentId",
        nativeQuery = true
    )
    fun countIncidentsByStudentId(@Param("studentId") studentId: Long): Long

    @Query(
        value = "select count(*) from observation where student_id = :studentId",
        nativeQuery = true
    )
    fun countObservationsByStudentId(@Param("studentId") studentId: Long): Long

    // ✅ NUEVO: Conteo simple por carrera
    fun countByCareerId(careerId: Long): Long

    // ✅ NUEVO: Conteo por carrera Y periodo
    @Query("""
        select count(s) from StudentEntity s
        where s.career.id = :careerId
          and s.academicPeriod.id = :periodId
    """)
    fun countByCareerIdAndPeriod(
        @Param("careerId") careerId: Long,
        @Param("periodId") periodId: Long
    ): Long
}