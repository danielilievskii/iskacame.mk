package mk.ukim.finki.iskacamebackend.dto.request.gathering

import java.math.BigDecimal

data class CreatePaymentRequest(
    val toUserId: Long,
    val amount: BigDecimal
)