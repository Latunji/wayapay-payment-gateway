UPDATE public."m_payment_gateway" SET status='SUCCESSFUL' WHERE status='TRANSACTION_COMPLETED';

UPDATE public."m_payment_gateway" SET status='PENDING' WHERE status='TRANSACTION_PENDING';

UPDATE public."m_payment_gateway" SET status='FAILED' WHERE status='TRANSACTION_FAILED';

UPDATE public."m_payment_gateway" SET status='ERROR' WHERE status='TRANSACTION_ERROR';

UPDATE public."m_payment_gateway" SET status='ABANDONED' WHERE status='TRANSACTION_ABANDON';

UPDATE public."m_payment_gateway" SET status='EXPIRED' WHERE status='TRANSACTION_EXPIRED';
