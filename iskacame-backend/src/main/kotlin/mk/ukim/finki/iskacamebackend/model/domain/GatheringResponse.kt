package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.GatheringType

@Entity
@Table(
    name = "gathering_responses",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "gathering_id"])]
)
class GatheringResponse(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @ManyToMany
    @JoinTable(
        name = "gathering_response_time_preference",
        joinColumns = [JoinColumn(name = "response_id")],
        inverseJoinColumns = [JoinColumn(name = "time_slot_id")]
    )
    var timeSlotPreferences: MutableSet<GatheringTimeSlot> = mutableSetOf(),

    @ElementCollection(targetClass = GatheringType::class)
    @CollectionTable(
        name = "gathering_response_type_preference",
        joinColumns = [JoinColumn(name = "response_id")]
    )
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var typePreferences: MutableSet<GatheringType> = mutableSetOf()

) : BaseEntity<Long>()
