# % reset - f

from hyperopt import fmin, tpe, hp


def f(space):
    x = space['x']
    y = space['y']
    # return 1 / abs( x) + 1 / abs(y)
    return x ** 2 + y ** 2


space = {
    'x': hp.uniform('x', -5, 5),
    'y': hp.uniform('y', -5, 5)
}

best = fmin(
    fn=f,
    space=space,
    algo=tpe.suggest,
    max_evals=1000
)

print("Found minimum after 1000 trials:")
print(best)
