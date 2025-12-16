package com.ids.hhub.service.external;

import org.springframework.stereotype.Service;

@Service
public class MockPaymentService implements PaymentService {
    @Override
    public boolean processPayment(String receiverEmail, double amount) {
        System.out.println("$$$ PAGAMENTO IN CORSO $$$");
        System.out.println("Destinatario: " + receiverEmail);
        System.out.println("Importo: €" + amount);
        System.out.println("Stato: SUCCESSO");
        return true;
    }
}
