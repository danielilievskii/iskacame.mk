package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import mk.ukim.finki.iskacamebackend.model.enums.PlaceLevel
import mk.ukim.finki.iskacamebackend.model.enums.PlaceType

@Entity
@Table(name = "places")
class Place(
    @Column(name = "name")
    val name: String,

    @Column(name = "address")
    val address: String?,

    @Column(name = "longitude")
    val longitude: Double?,

    @Column(name = "latitude")
    val latitude: Double?,

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    val type: PlaceType,

    @Column(name = "price_level")
    @Enumerated(EnumType.STRING)
    val priceLevel: PlaceLevel,

    @Column(name = "link")
    val link: String?,

    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: User?
) : BaseEntity<Long>()
