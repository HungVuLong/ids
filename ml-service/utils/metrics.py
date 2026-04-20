# IDS ML Service — utils/metrics.py
# Evaluation helpers used by train.py

import json
import numpy as np
import matplotlib.pyplot as plt
from pathlib import Path
from datetime import datetime, timezone
from sklearn.metrics import (
    classification_report, confusion_matrix, roc_curve, auc
)


def plot_confusion_matrix(y_true, y_pred, labels, save_path=None):
    """Plot heatmap confusion matrix, save to save_path if provided else show"""
    cm = confusion_matrix(y_true, y_pred)

    # Create figure and axis
    fig, ax = plt.subplots(figsize=(8, 6))

    # Plot heatmap
    im = ax.imshow(cm, interpolation='nearest', cmap=plt.cm.Blues)
    ax.figure.colorbar(im, ax=ax)

    # Set ticks and labels
    ax.set(xticks=np.arange(cm.shape[1]),
           yticks=np.arange(cm.shape[0]),
           xticklabels=labels,
           yticklabels=labels,
           ylabel='True label',
           xlabel='Predicted label')

    # Rotate the tick labels for better readability
    plt.setp(ax.get_xticklabels(), rotation=45, ha="right",
             rotation_mode="anchor")

    # Add text annotations
    thresh = cm.max() / 2.0
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            ax.text(j, i, format(cm[i, j], 'd'),
                   ha="center", va="center",
                   color="white" if cm[i, j] > thresh else "black",
                   fontsize=14, fontweight='bold')

    fig.tight_layout()

    # Save or show
    if save_path:
        save_path = Path(save_path)
        save_path.parent.mkdir(parents=True, exist_ok=True)
        plt.savefig(save_path, dpi=100, bbox_inches='tight')
        print(f"✓ Confusion matrix saved to {save_path}")
    else:
        plt.show()

    plt.close(fig)


def plot_roc_curve(model, X_test, y_test, save_path=None):
    """Plot ROC curve, compute AUC, save if save_path provided, return auc score"""
    # Get probability predictions for positive class
    y_proba = model.predict_proba(X_test)[:, 1]

    # Compute ROC curve
    fpr, tpr, thresholds = roc_curve(y_test, y_proba)
    roc_auc = auc(fpr, tpr)

    # Create figure and plot
    fig, ax = plt.subplots(figsize=(8, 6))
    ax.plot(fpr, tpr, color='darkorange', lw=2, label=f'ROC curve (AUC = {roc_auc:.4f})')
    ax.plot([0, 1], [0, 1], color='navy', lw=2, linestyle='--', label='Random classifier')

    ax.set(xlim=[0.0, 1.0],
           ylim=[0.0, 1.05],
           xlabel='False Positive Rate',
           ylabel='True Positive Rate',
           title='Receiver Operating Characteristic (ROC) Curve')

    ax.legend(loc="lower right", fontsize=11)
    ax.grid(True, alpha=0.3)
    fig.tight_layout()

    # Save or show
    if save_path:
        save_path = Path(save_path)
        save_path.parent.mkdir(parents=True, exist_ok=True)
        plt.savefig(save_path, dpi=100, bbox_inches='tight')
        print(f"✓ ROC curve saved to {save_path}")
    else:
        plt.show()

    plt.close(fig)
    return roc_auc


def print_comparison_table(results):
    """Print formatted table of model results, highlight row with highest f1"""
    print(f"\n{'Model':<20} {'Accuracy':>10} {'F1':>10} {'Precision':>10} {'Recall':>10} {'Time':>8}")
    print("-" * 70)

    # Find max F1 for highlighting
    max_f1 = max(r['f1'] for r in results)

    for r in results:
        # Highlight row with highest F1 score
        prefix = "→ " if r['f1'] == max_f1 else "  "
        print(f"{prefix}{r['name']:<18} {r['accuracy']:>10.4f} {r['f1']:>10.4f} {r['precision']:>10.4f} {r['recall']:>10.4f} {r['train_time']:>8.2f}s")

    print("-" * 70)
    best = max(results, key=lambda x: x['f1'])
    print(f"✓ Best model: {best['name']} (F1={best['f1']:.4f})")


def save_evaluation_report(results, best, save_path):
    """Save dict with keys: models list, best dict, generated_at ISO string as JSON"""
    save_path = Path(save_path)
    save_path.parent.mkdir(parents=True, exist_ok=True)

    report = {
        "models": results,
        "best": best,
        "generated_at": datetime.now(timezone.utc).isoformat()
    }

    with open(save_path, 'w') as f:
        json.dump(report, f, indent=2, default=str)

    print(f"✓ Evaluation report saved to {save_path}")
