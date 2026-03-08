package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.PlaceLevel
import mk.ukim.finki.iskacamebackend.model.enums.PlaceType

@Entity
@Table(name = "places")
class Place(
    @Column(name = "name")
    var name: String,

    @Column(name = "address")
    var address: String?,

    @Column(name = "longitude")
    var longitude: Double?,

    @Column(name = "latitude")
    var latitude: Double?,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    var type: PlaceType,

    @Column(name = "price_level")
    @Enumerated(EnumType.STRING)
    var priceLevel: PlaceLevel,

    @Column(name = "link")
    var link: String?,

    @ManyToOne
    @JoinColumn(name = "owner_id")
    var owner: User?
) : BaseEntity<Long>()
