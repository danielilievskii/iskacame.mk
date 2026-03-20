package mk.ukim.finki.iskacamebackend.model.enums

enum class TimeSlot(val startHour: Int, val endHour: Int) {
    MORNING(6, 12),
    NOON(12, 14),
    AFTERNOON(14, 18),
    EVENING(18, 23);
}
