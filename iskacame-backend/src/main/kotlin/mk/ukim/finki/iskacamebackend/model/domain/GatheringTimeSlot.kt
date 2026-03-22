package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.TimeSlot
import java.time.LocalDate

@Entity
@Table(name = "gathering_time_slots")
class GatheringTimeSlot(

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering,

    val date: LocalDate,

    @Enumerated(EnumType.STRING)
    val slot: TimeSlot

) : BaseEntity<Long>()