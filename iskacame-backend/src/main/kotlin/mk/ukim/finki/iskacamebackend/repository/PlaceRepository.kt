package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.Place
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRepository : JpaRepository<Place, Long>