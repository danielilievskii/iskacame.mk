package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.model.enums.ActivityType
import java.math.BigDecimal
import java.time.Instant

sealed class ActivityDto {
    abstract val id: Long
    abstract val createdAt: Instant
    abstract val type: ActivityType

    data class ExpenseActivityDto(
        override val id: Long,
        val description: String?,
        val totalAmount: BigDecimal,
        val paidByUserId: Long,
        val splits: List<ExpenseSplitDto>,
        override val createdAt: Instant,
        override val type: ActivityType = ActivityType.EXPENSE
    ) : ActivityDto()

    data class PaymentActivityDto(
        override val id: Long,
        val fromUserId: Long,
        val toUserId: Long,
        val amount: BigDecimal,
        override val createdAt: Instant,
        override val type: ActivityType = ActivityType.PAYMENT
    ) : ActivityDto()
}

data class ExpenseSplitDto(
    val userId: Long,
    val amountOwed: BigDecimal,
    val isOwnShare: Boolean
)