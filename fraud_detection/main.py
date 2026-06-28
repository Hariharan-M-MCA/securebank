from fastapi import FastAPI
from pydantic import BaseModel
from datetime import datetime

import pandas as pd
import joblib

app = FastAPI(title="Fraud Detection Service")

# Load trained model
model = joblib.load("model_pipeline.pkl")


class TransactionData(BaseModel):
    transactionId: int
    amount: float
    transactionType: str
    accountNumber: str
    email: str

    # New fields
    accountAgeDays: int = 365
    transactionVelocity: int = 1
    userTransactionCount: int = 100


class FraudResponse(BaseModel):
    transactionId: int
    fraudScore: float
    isFraud: bool
    reason: str


@app.get("/health")
def health():
    return {
        "status": "Fraud Detection Service is running"
    }


@app.post("/predict", response_model=FraudResponse)
def predict(transaction: TransactionData):

    now = datetime.now()

    hour_of_day = now.hour
    day_of_week = now.weekday()

    # Values coming from request
    account_age_days = transaction.accountAgeDays
    user_transaction_count = transaction.userTransactionCount
    transaction_velocity = transaction.transactionVelocity

    amount_deviation = min(
    transaction.amount / 10000,
    3.5
)
    is_weekend = 1 if day_of_week >= 5 else 0

    is_night_transaction = (
        1 if hour_of_day <= 4 else 0
    )

    high_amount_flag = (
        1 if transaction.amount > 10000 else 0
    )

    new_account_flag = (
        1 if account_age_days < 30 else 0
    )

    features = pd.DataFrame([
        {
            "amount": transaction.amount,
            "transactionType": transaction.transactionType,
            "hour_of_day": hour_of_day,
            "day_of_week": day_of_week,
            "account_age_days": account_age_days,
            "user_transaction_count": user_transaction_count,
            "transaction_velocity": transaction_velocity,
            "amount_deviation": amount_deviation,
            "is_weekend": is_weekend,
            "is_night_transaction": is_night_transaction,
            "high_amount_flag": high_amount_flag,
            "new_account_flag": new_account_flag
        }
    ])

    # Debug output
    print("\n========== FEATURES SENT TO MODEL ==========")
    print(features)
    print("===========================================\n")

    fraud_score = float(
        model.predict_proba(features)[0][1]
    )

    is_fraud = fraud_score >= 0.50

    if fraud_score >= 0.80:
        reason = "High fraud probability"

    elif fraud_score >= 0.50:
        reason = "Suspicious transaction"

    else:
        reason = "Normal transaction"

    return FraudResponse(
        transactionId=transaction.transactionId,
        fraudScore=round(fraud_score, 3),
        isFraud=is_fraud,
        reason=reason
    )