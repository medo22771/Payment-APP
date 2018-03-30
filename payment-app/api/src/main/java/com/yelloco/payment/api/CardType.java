package com.yelloco.payment.api;

/**
 * Type of card used within payment transaction.
 */
public enum CardType {

    /**
     * Contact chip card (smart card)
     */
    CONTACT_CHIP,
    /**
     * Contactless (NFC) card
     */
    CONTACTLESS,
    /**
     * Magnetic stripe card
     */
    MAGNETIC_STRIPE,
    /**
     * Card data is manually filled within transaction
     */
    MANUAL_ENTRY
}
