package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long>