package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import java.time.LocalDateTime

@Entity
@Table(name = "gatherings")
class Gathering(
    @ManyToOne
    @JoinColumn(name = "creator_id")
    val creator: User,

    @Column(name = "title")
    val title: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "start_date")
    val startDate: LocalDateTime?,

    @Column(name = "end_date")
    val endDate: LocalDateTime?,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: GatheringStatus,

    @Column(name = "finalized_time")
    val finalizedTime: LocalDateTime?,

    @Column(name = "finalized_place_id")
    val finalizedPlaceId: Long?,
) : BaseEntity<Long>()
