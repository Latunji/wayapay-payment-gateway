UPDATE public."m_payment_gateway" SET transaction_status='SUCCESSFUL' WHERE transaction_status='TRANSACTION_COMPLETED';

UPDATE public."m_payment_gateway" SET transaction_status='PENDING' WHERE transaction_status='TRANSACTION_PENDING';

UPDATE public."m_payment_gateway" SET transaction_status='FAILED' WHERE transaction_status='TRANSACTION_FAILED';

UPDATE public."m_payment_gateway" SET transaction_status='ERROR' WHERE transaction_status='TRANSACTION_ERROR';

UPDATE public."m_payment_gateway" SET transaction_status='ABANDONED' WHERE transaction_status='TRANSACTION_ABANDON';

UPDATE public."m_payment_gateway" SET transaction_status='EXPIRED' WHERE transaction_status='TRANSACTION_EXPIRED';
