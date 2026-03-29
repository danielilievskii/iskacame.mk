package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreateExpenseRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePaymentRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.DebtDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.ActivityDto
import mk.ukim.finki.iskacamebackend.service.intf.ExpenseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/gatherings/{gatheringId}")
@Tag(name = "Expenses", description = "Manage expenses and debts within a gathering")
class ExpenseController(
    private val expenseService: ExpenseService
) {

    @PostMapping("/expenses")
    @Operation(
        summary = "Create an expense",
        description = "Creates a new expense for the given gathering, including how the cost is split among participants."
    )
    fun createExpense(
        @PathVariable gatheringId: Long,
        @RequestBody request: CreateExpenseRequest
    ): ResponseEntity<Void> {

        expenseService.createExpense(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/expenses/{expenseId}")
    @Operation(
        summary = "Delete an expense",
        description = "Deletes an expense and all its associated splits from the gathering. Once removed, the expense is no longer factored into debt calculations."
    )
    fun deleteExpense(
        @PathVariable gatheringId: Long,
        @PathVariable expenseId: Long
    ): ResponseEntity<Void> {

        expenseService.deleteExpense(gatheringId, expenseId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/payments")
    @Operation(
        summary = "Create a payment",
        description = "Records a direct payment between two participants in the gathering, reducing the payer's outstanding debt toward the recipient."
    )
    fun createPayment(
        @PathVariable gatheringId: Long,
        @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<Void> {

        expenseService.createPayment(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/payments/{paymentId}")
    @Operation(
        summary = "Delete a payment",
        description = "Deletes a payment from the gathering. Once removed, the payment is no longer factored into debt calculations and affected balances are restored."
    )
    fun deletePayment(
        @PathVariable gatheringId: Long,
        @PathVariable paymentId: Long
    ): ResponseEntity<Void> {

        expenseService.deletePayment(gatheringId, paymentId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/activities")
    @Operation(
        summary = "Get activity feed for a gathering",
        description = "Returns a chronologically ordered list of all expenses and payments in the gathering, most recent first."
    )
    fun getActivities(
        @PathVariable gatheringId: Long
    ): ResponseEntity<List<ActivityDto>> {

        val activityDtos = expenseService.getActivities(gatheringId)
        return ResponseEntity.ok(activityDtos)
    }

    @GetMapping("/debts")
    @Operation(
        summary = "Get debts for a gathering",
        description = "Returns a simplified list of debts representing who owes whom and how much, based on all expense splits and payments in the gathering."
    )
    fun getDebts(
        @PathVariable gatheringId: Long
    ): ResponseEntity<List<DebtDto>> {

        val debtDtos = expenseService.calculateDebts(gatheringId)
        return ResponseEntity.ok(debtDtos)
    }
}