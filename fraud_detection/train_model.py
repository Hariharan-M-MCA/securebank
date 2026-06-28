import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder
from sklearn.preprocessing import StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from sklearn.metrics import roc_auc_score


def load_data(path="data/transactions.csv"):
    return pd.read_csv(path)


def build_and_train(df):

    X = df[
        [
            "amount",
            "transactionType",
            "hour_of_day",
            "day_of_week",
            "account_age_days",
            "user_transaction_count",
            "transaction_velocity",
            "amount_deviation"
        ]
    ]

    y = df["is_fraud"]

    cat_feats = ["transactionType"]

    num_feats = [
        "amount",
        "hour_of_day",
        "day_of_week",
        "account_age_days",
        "user_transaction_count",
        "transaction_velocity",
        "amount_deviation"
    ]

    preprocessor = ColumnTransformer(
        [
            (
                "num",
                StandardScaler(),
                num_feats
            ),
            (
                "cat",
                OneHotEncoder(handle_unknown="ignore"),
                cat_feats
            )
        ]
    )

    pipeline = Pipeline(
        [
            (
                "preproc",
                preprocessor
            ),
            (
                "clf",
                RandomForestClassifier(
                    n_estimators=200,
                    class_weight="balanced",
                    random_state=42,
                    n_jobs=-1
                )
            )
        ]
    )

    X_train, X_test, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.2,
        stratify=y,
        random_state=42
    )

    print("Training model...")
    pipeline.fit(X_train, y_train)

    preds = pipeline.predict(X_test)

    probs = pipeline.predict_proba(X_test)[:, 1]

    print("\nClassification Report:\n")

    print(
        classification_report(
            y_test,
            preds,
            digits=4
        )
    )

    auc = roc_auc_score(
        y_test,
        probs
    )

    print("\nROC AUC:", round(auc, 4))

    joblib.dump(
        pipeline,
        "model_pipeline.pkl"
    )

    print(
        "\nSaved model to model_pipeline.pkl"
    )


if __name__ == "__main__":

    df = load_data()

    build_and_train(df)