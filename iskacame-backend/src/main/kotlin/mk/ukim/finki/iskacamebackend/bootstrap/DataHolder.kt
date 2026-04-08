package mk.ukim.finki.iskacamebackend.bootstrap

import jakarta.annotation.PostConstruct
import mk.ukim.finki.iskacamebackend.model.domain.ChatRoom
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.GatheringParticipation
import mk.ukim.finki.iskacamebackend.model.domain.GatheringTimeSlot
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import mk.ukim.finki.iskacamebackend.repository.ChatRoomRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringParticipationRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringTimeSlotRepository
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.utils.TimeSlotGenerator
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class DataHolder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val gatheringRepository: GatheringRepository,
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val gatheringTimeSlotRepository: GatheringTimeSlotRepository,
    private val chatRoomRepository: ChatRoomRepository
) {
    @PostConstruct
    fun init() {

        val users = mutableListOf<User>()
        if (userRepository.count() == 0L) {

            val user1 = User(
                name = "Daniel Ilievski",
                username = "di",
                email = "dilievski54@gmail.com",
                password = passwordEncoder.encode("di"),
                phone = "+38971234567",
                roles = mutableSetOf(UserRole.USER),
                avatar = null,
                emailVerified = true,
                enabled = true
            )
            users.add(user1)

            val user2 = User(
                name = "Ljubica Damjanovik",
                username = "ld",
                email = "",
                password = passwordEncoder.encode("ld"),
                phone = "123456789",
                roles = mutableSetOf(UserRole.USER),
                avatar = null,
                emailVerified = true,
                enabled = true
            )
            users.add(user2)

            val user3 = User(
                name = "Nikola Jordanoski",
                username = "nj",
                email = "nikolaj_koko@yahoo.com",
                password = passwordEncoder.encode("nj"),
                phone = "+123456789",
                roles = mutableSetOf(UserRole.USER),
                avatar = null,
                emailVerified = true,
                enabled = true
            )
            users.add(user3)

            userRepository.saveAll(users)
        }

        val gatherings = mutableListOf<Gathering>()
        val participations = mutableListOf<GatheringParticipation>()
        val chatRooms = mutableListOf<ChatRoom>()
        if (gatheringRepository.count() == 0L && gatheringParticipationRepository.count() == 0L) {

            val now = LocalDateTime.now()

            val gathering1 = Gathering(
                creator = users[0],
                title = "Cafe",
                description = "Casual.",
                location = null,
                startDate = now.plus(3, ChronoUnit.DAYS).withHour(19).withMinute(0),
                endDate = now.plus(5, ChronoUnit.DAYS).withHour(22).withMinute(0),
                status = GatheringStatus.OPEN,
                finalizedTime = null,
                finalizedPlace = null
            )
            gatherings.add(gathering1)
            chatRooms.add(ChatRoom(gathering = gathering1))

            val gathering2 = Gathering(
                creator = users[1],
                title = "Weekend Hike",
                description = "Easy / medium hike. Meeting point will be decided later.",
                location = null,
                startDate = now.plus(7, ChronoUnit.DAYS).withHour(8).withMinute(0),
                endDate = now.plus(10, ChronoUnit.DAYS).withHour(13).withMinute(0),
                status = GatheringStatus.DRAFT,
                finalizedTime = null,
                finalizedPlace = null
            )
            gatherings.add(gathering2)
            chatRooms.add(ChatRoom(gathering = gathering2))

            val gathering3 = Gathering(
                creator = users[2],
                title = "Boardgames Night",
                description = "Bring your favorite game. Snacks welcome!",
                location = null,
                startDate = now.plus(1, ChronoUnit.DAYS).withHour(19).withMinute(0),
                endDate = now.plus(3, ChronoUnit.DAYS).withHour(22).withMinute(0),
                status = GatheringStatus.CANCELLED,
                finalizedTime = null,
                finalizedPlace = null
            )
            gatherings.add(gathering3)
            chatRooms.add(ChatRoom(gathering = gathering3))

            gatheringRepository.saveAll(gatherings)
            chatRoomRepository.saveAll(chatRooms)

            val timeSlots = gatherings.flatMap { gathering ->
                val result = TimeSlotGenerator.generate(gathering.startDate, gathering.endDate)

                result.map { (date, slot) ->
                    GatheringTimeSlot(
                        gathering = gathering,
                        date = date,
                        slot = slot
                    )
                }
            }
            gatheringTimeSlotRepository.saveAll(timeSlots)

            gatherings.forEach { gathering ->
                participations.add(
                    GatheringParticipation(
                        user = gathering.creator,
                        gathering = gathering,
                        status = ParticipationStatus.JOINED
                    )
                )

                val otherUsers = users.filter { it.id != gathering.creator.id }

                participations.add(
                    GatheringParticipation(
                        user = otherUsers[0],
                        gathering = gathering,
                        status = ParticipationStatus.JOINED
                    )
                )

                participations.add(
                    GatheringParticipation(
                        user = otherUsers[1],
                        gathering = gathering,
                        status = ParticipationStatus.INVITED
                    )
                )
            }

            gatheringParticipationRepository.saveAll(participations)
        }
    }
}