UPDATE m_payment_gateway set status = 'FAILED' WHERE status='BANK_ERROR';
UPDATE m_payment_gateway set status = 'FAILED' WHERE status='SYSTEM_ERROR';

UPDATE m_payment_gateway set channel = 'CARD' WHERE channel='WEBVIEW';
