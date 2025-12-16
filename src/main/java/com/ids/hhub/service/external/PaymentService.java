package com.ids.hhub.service.external;

public interface PaymentService {
    // Restituisce true se il pagamento è andato a buon fine
    boolean processPayment(String receiverEmail, double amount);
}
