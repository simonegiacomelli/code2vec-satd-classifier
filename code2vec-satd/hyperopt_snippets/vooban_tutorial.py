# https://medium.com/vooban-ai/hyperopt-tutorial-for-optimizing-neural-networks-hyperparameters-e3102814b919
# % reset - f
from hyperopt import pyll, hp
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats.kde import gaussian_kde

# Let's plot the result of sampling from many different probability distributions:
hyperparam_generators = {
    'randint': hp.randint('randint', 5),
    'uniform': hp.uniform('uniform', -1, 3),
    'loguniform': hp.loguniform('loguniform', -2, 2),
    'normal': hp.normal('normal', 1, 2),
    'lognormal': hp.lognormal('lognormal', 0, 0.3)
}

n_samples = 5000

for title, space in hyperparam_generators.items():
    evaluated = [
        pyll.stochastic.sample(space) for _ in range(n_samples)
    ]
    x_domain = np.linspace(min(evaluated), max(evaluated), n_samples)

    plt.figure(figsize=(18, 6))

    hist = gaussian_kde(evaluated, 0.001)
    plt.plot(x_domain, hist(x_domain), label="True Histogram")

    blurred_hist = gaussian_kde(evaluated, 0.1)
    plt.plot(x_domain, blurred_hist(x_domain), label="Smoothed Histogram")

    plt.title("Histogram (pdf) for a {} distribution".format(title))
    plt.legend()
    plt.show()
