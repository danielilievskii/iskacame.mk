package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "gathering_images")
class GatheringImage(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    var gathering: Gathering,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    var uploader: User,

    @Column(name = "url", nullable = false)
    var url: String,

    @Column(name = "public_id", nullable = false)
    var publicId: String,

) : BaseEntity<Long>()
