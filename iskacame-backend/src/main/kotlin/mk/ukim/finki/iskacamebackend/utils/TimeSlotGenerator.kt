package mk.ukim.finki.iskacamebackend.utils

import mk.ukim.finki.iskacamebackend.model.enums.TimeSlot
import java.time.LocalDate
import java.time.LocalDateTime

object TimeSlotGenerator {

    /**
     * Generates a list of time slots between the given start and end date-times.
     *
     * Iterates over each day in the range [startDateTime, endDateTime] and includes
     * only the [TimeSlot]s that overlap with the gathering's time window.
     *
     * @param startDateTime The start of the gathering (inclusive).
     * @param endDateTime The end of the gathering (exclusive at slot boundary).
     * @return A list of [LocalDate] to [TimeSlot] pairs representing all overlapping slots,
     * ordered chronologically.
     */
    fun generate(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<Pair<LocalDate, TimeSlot>> {

        val timeSlots = mutableListOf<Pair<LocalDate, TimeSlot>>()

        var currentDate = startDateTime.toLocalDate()
        val endDate = endDateTime.toLocalDate()

        while (!currentDate.isAfter(endDate)) {

            for (slot in TimeSlot.entries) {
                val slotStartDateTime = currentDate.atTime(slot.startHour, 0)
                val slotEndDateTime = currentDate.atTime(slot.endHour, 0)

                val slotStartsBeforeGatheringEnds = slotStartDateTime.isBefore(endDateTime)
                val slotEndsAfterGatheringStarts = slotEndDateTime.isAfter(startDateTime)

                if (slotStartsBeforeGatheringEnds && slotEndsAfterGatheringStarts) {
                    timeSlots.add(Pair(currentDate, slot))
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        return timeSlots
    }
}