package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.GatheringType

@Entity
@Table(name = "gathering_responses")
class GatheringResponse(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @ElementCollection
    @CollectionTable(name = "gathering_response_pref_time", joinColumns = [JoinColumn(name = "response_id")])
    @Column(name = "preferred_time")
    var preferredTime: MutableMap<String, Boolean>,

    @Column(name = "gathering_type")
    @Enumerated(EnumType.STRING)
    var preferredType: MutableSet<GatheringType>
) : BaseEntity<Long>()
