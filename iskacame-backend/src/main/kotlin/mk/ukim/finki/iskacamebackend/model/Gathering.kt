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
    var creator: User,

    @Column(name = "title")
    var title: String,

    @Column(name = "description")
    var description: String?,

    @Column(name = "start_date")
    var startDate: LocalDateTime?,

    @Column(name = "end_date")
    var endDate: LocalDateTime?,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: GatheringStatus,

    @Column(name = "finalized_time")
    var finalizedTime: LocalDateTime?,

    @ManyToOne
    @JoinColumn(name = "finalized_place_id")
    var finalizedPlace: Place?,
) : BaseEntity<Long>()
