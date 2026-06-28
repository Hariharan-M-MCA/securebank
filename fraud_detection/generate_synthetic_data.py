from datetime import datetime, timedelta
import numpy as np
import pandas as pd

TRAN_TYPES = ["DEPOSIT", "WITHDRAW", "TRANSFER"]

RNG = np.random.default_rng(42)


def make_user_profiles(n_users=1200):

    users = []

    for uid in range(n_users):

        user_id = f"user_{uid}"

        account_age = int(
            RNG.integers(1, 5000)
        )

        # More realistic customer profiles
        base_amount = float(
            RNG.choice([
                200,
                500,
                1000,
                2500,
                5000,
                10000,
                25000
            ])
        )

        tx_count = int(
            RNG.integers(5, 3000)
        )

        users.append(
            (
                user_id,
                account_age,
                base_amount,
                tx_count
            )
        )

    return users


def gen_transactions(users, max_tx_per_user=40):

    rows = []

    txid = 1

    now = datetime.now()

    for (
        user_id,
        account_age,
        base_amount,
        tx_count
    ) in users:

        upper = min(
            max_tx_per_user,
            max(2, tx_count // 10 + 1)
        )

        n_tx = int(
            RNG.integers(
                1,
                upper
            )
        )

        for _ in range(n_tx):

            amount = max(
                1.0,
                abs(
                    float(
                        RNG.normal(
                            base_amount,
                            base_amount * 1.2
                        )
                    )
                )
            )

            tx_type = RNG.choice(
                TRAN_TYPES,
                p=[0.5, 0.3, 0.2]
            )

            timestamp = now - timedelta(
                days=int(RNG.integers(0, 365)),
                hours=int(RNG.integers(0, 24)),
                minutes=int(RNG.integers(0, 60))
            )

            hour = timestamp.hour

            day = timestamp.weekday()

            velocity = int(
                RNG.poisson(1.5)
            )

            amount_deviation = (
                amount /
                (base_amount + 1e-6)
            )

            is_weekend = (
                1 if day >= 5 else 0
            )

            is_night_transaction = (
                1 if hour <= 4 else 0
            )

            high_amount_flag = (
                1 if amount > 20000 else 0
            )

            new_account_flag = (
                1 if account_age < 30 else 0
            )

            score = 0.0

            if amount > 20000:
                score += 0.4

            if amount > 50000:
                score += 0.4

            if velocity >= 3:
                score += 0.3

            if velocity >= 5:
                score += 0.3

            if account_age < 30:
                score += 0.3

            if account_age < 7:
                score += 0.3

            if tx_type == "TRANSFER":
                score += 0.15

            if is_night_transaction:
                score += 0.2

            if amount_deviation > 2:
                score += 0.2

            if amount_deviation > 4:
                score += 0.3

            if amount_deviation > 6:
                score += 0.4

            # Strong fraud combinations

            if (
                amount > 20000 and
                velocity >= 3
            ):
                score += 0.5

            if (
                amount > 10000 and
                account_age < 30
            ):
                score += 0.5

            if (
                is_night_transaction and
                velocity >= 3
            ):
                score += 0.3

            if (
                amount > 50000 and
                tx_type == "TRANSFER"
            ):
                score += 0.5

            prob = min(
                0.99,
                max(
                    0.01,
                    score + RNG.uniform(
                        -0.05,
                        0.05
                    )
                )
            )

            is_fraud = int(
                RNG.random() < prob
            )

            rows.append(
                {
                    "transactionId": txid,
                    "accountId": user_id,
                    "amount": round(amount, 2),
                    "transactionType": tx_type,
                    "timestamp": timestamp.isoformat(),

                    "hour_of_day": hour,
                    "day_of_week": day,

                    "account_age_days": account_age,
                    "user_transaction_count": tx_count,
                    "transaction_velocity": velocity,

                    "amount_deviation": round(
                        amount_deviation,
                        3
                    ),

                    "is_weekend": is_weekend,
                    "is_night_transaction": is_night_transaction,
                    "high_amount_flag": high_amount_flag,
                    "new_account_flag": new_account_flag,

                    "is_fraud": is_fraud
                }
            )

            txid += 1

    return pd.DataFrame(rows)


if __name__ == "__main__":

    users = make_user_profiles(1200)

    df = gen_transactions(
        users,
        max_tx_per_user=40
    )

    df.to_csv(
        "data/transactions.csv",
        index=False
    )

    print(
        f"Saved {len(df)} transactions to data/transactions.csv"
    )