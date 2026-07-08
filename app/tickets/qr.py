import hmac
import hashlib
import uuid
from app.core.config import settings

def sign_ticket(ticket_id: uuid.UUID, event_id: uuid.UUID, user_id: str | uuid.UUID) -> str:
    """
    Generate a cryptographic signature for a ticket using HMAC-SHA256.
    The message contains the ticket_id, event_id, and user_id.
    """
    message = f"{ticket_id}:{event_id}:{user_id}".encode("utf-8")
    secret = settings.QR_JWT_SECRET.encode("utf-8")
    signature = hmac.new(secret, message, hashlib.sha256).hexdigest()
    return signature

def verify_ticket_signature(
    ticket_id: uuid.UUID, event_id: uuid.UUID, user_id: str | uuid.UUID, signature: str
) -> bool:
    """
    Verify if the signature of the ticket matches the generated HMAC signature.
    """
    expected_signature = sign_ticket(ticket_id, event_id, user_id)
    return hmac.compare_digest(expected_signature, signature)
