package mk.ukim.finki.iskacamebackend.dto.request.gathering

import java.math.BigDecimal

data class CreateExpenseRequest(
    val paidByUserId: Long,
    val totalAmount: BigDecimal,
    val description: String,
    val splits: List<CreateSplitRequest>
)

data class CreateSplitRequest(
    val userId: Long,
    val amountOwed: BigDecimal,
)

