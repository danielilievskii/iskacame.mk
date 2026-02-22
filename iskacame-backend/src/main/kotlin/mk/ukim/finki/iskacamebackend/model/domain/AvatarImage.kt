package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class AvatarImage(
  @Column(name = "avatar_url")
  var url: String? = null,

  @Column(name = "avatar_public_id")
  var publicId: String? = null
)