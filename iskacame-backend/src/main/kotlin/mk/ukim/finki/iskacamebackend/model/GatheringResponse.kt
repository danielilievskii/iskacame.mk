package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.enums.GatheringType

@Entity
@Table(name = "gathering_responses")
class GatheringResponse(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering,

    @ElementCollection
    @CollectionTable(name = "gathering_response_pref_time", joinColumns = [JoinColumn(name = "response_id")])
    @Column(name = "preferred_time")
    val preferredTime: MutableMap<String, Boolean>,

    @Column(name = "gathering_type")
    @Enumerated(EnumType.STRING)
    val preferredType: MutableSet<GatheringType>
) : BaseEntity<Long>()
