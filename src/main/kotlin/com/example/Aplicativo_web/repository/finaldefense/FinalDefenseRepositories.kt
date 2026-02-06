package com.example.Aplicativo_web.repository.finaldefense

import com.example.Aplicativo_web.entity.finaldefense.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FinalDefenseWindowRepository : JpaRepository<FinalDefenseWindowEntity, Long> {

    @Query("""
        select w from FinalDefenseWindowEntity w
        where w.academicPeriod.id = :periodId
        order by w.startsAt desc
    """)
    fun findAllForPeriod(@Param("periodId") periodId: Long): List<FinalDefenseWindowEntity>

    @Query("""
        select w from FinalDefenseWindowEntity w
        where w.academicPeriod.id = :periodId
          and w.isActive = true
          and (:careerId is null or w.career.id = :careerId or w.career is null)
        order by w.startsAt asc
    """)
    fun findActiveForPeriodAndCareer(
        @Param("periodId") periodId: Long,
        @Param("careerId") careerId: Long?
    ): List<FinalDefenseWindowEntity>
    fun findAllByAcademicPeriod_IdOrderByStartsAtAsc(academicPeriodId: Long): List<FinalDefenseWindowEntity>
}


interface FinalDefenseSlotRepository : JpaRepository<FinalDefenseSlotEntity, Long> {
    fun findAllByWindow_IdOrderByStartsAtAsc(windowId: Long): List<FinalDefenseSlotEntity>
}

interface FinalDefenseGroupRepository : JpaRepository<FinalDefenseGroupEntity, Long>

interface FinalDefenseGroupMemberRepository : JpaRepository<FinalDefenseGroupMemberEntity, FinalDefenseGroupMemberId> {

    @Query("""
        select gm from FinalDefenseGroupMemberEntity gm
        join fetch gm.student s
        where gm.group.id = :groupId
    """)
    fun findAllByGroupIdFetchStudents(@Param("groupId") groupId: Long): List<FinalDefenseGroupMemberEntity>

    fun findAllByGroup_Id(groupId: Long): List<FinalDefenseGroupMemberEntity>
}

interface FinalDefenseBookingRepository : JpaRepository<FinalDefenseBookingEntity, Long> {
    fun findBySlot_Id(slotId: Long): FinalDefenseBookingEntity?

    @Query("""
        select b from FinalDefenseBookingEntity b
        join fetch b.slot sl
        join fetch sl.window w
        join fetch w.academicPeriod p
        join fetch b.group g
        join fetch g.career c
        where p.id = :periodId
        order by b.id desc
    """)
    fun findAllByPeriodFetch(@Param("periodId") periodId: Long): List<FinalDefenseBookingEntity>
}

interface FinalDefenseBookingJuryRepository : JpaRepository<FinalDefenseBookingJuryEntity, FinalDefenseBookingJuryId> {

    @Query("""
    select bj from FinalDefenseBookingJuryEntity bj
    join fetch bj.booking b
    join fetch b.slot sl
    join fetch sl.window w
    join fetch w.academicPeriod p
    join fetch b.group g
    join fetch g.career c
    join fetch bj.juryUser ju
    where lower(ju.username) = lower(:username)
      and p.isActive = true
    order by b.id desc
""")
    fun findMyBookingsFetch(@Param("username") username: String): List<FinalDefenseBookingJuryEntity>

    @Query("""
        select case when count(bj) > 0 then true else false end
        from FinalDefenseBookingJuryEntity bj
        where bj.booking.id = :bookingId and lower(bj.juryUser.username) = lower(:username)
    """)
    fun isJuryAssigned(@Param("bookingId") bookingId: Long, @Param("username") username: String): Boolean

    @Query("""
        select bj from FinalDefenseBookingJuryEntity bj
        join fetch bj.juryUser ju
        where bj.booking.id = :bookingId
    """)
    fun findAllByBookingIdFetchJury(@Param("bookingId") bookingId: Long): List<FinalDefenseBookingJuryEntity>

    fun findAllByBooking_Id(bookingId: Long): List<FinalDefenseBookingJuryEntity>
    fun countByBooking_Id(bookingId: Long): Long

}

interface FinalDefenseEvaluationRepository :
    JpaRepository<FinalDefenseEvaluationEntity, Long> {

    fun findAllByBooking_IdOrderByCreatedAtAsc(
        bookingId: Long
    ): List<FinalDefenseEvaluationEntity>

    fun findByBooking_IdAndJuryUser_IdAndStudent_Id(
        bookingId: Long,
        juryUserId: Long,
        studentId: Long
    ): FinalDefenseEvaluationEntity?

    fun findAllByBooking_IdAndStudent_Id(
        bookingId: Long,
        studentId: Long
    ): List<FinalDefenseEvaluationEntity>

    fun countByBooking_Id(bookingId: Long): Long
}

