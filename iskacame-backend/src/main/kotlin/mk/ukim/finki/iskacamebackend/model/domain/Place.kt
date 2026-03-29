package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.PriceLevel

@Entity
@Table(name = "gathering_places")
class Place(
    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @Column(name = "name")
    var name: String,

    @Column(name = "address")
    var address: String?,

    @Column(name = "longitude")
    var longitude: Double?,

    @Column(name = "latitude")
    var latitude: Double?,

    @Column(name = "type")
    var type: String,

    @Column(name = "price_level")
    @Enumerated(EnumType.STRING)
    var priceLevel: PriceLevel
    
) : BaseEntity<Long>()