class RelevanceMeasures:
    def __init__(self, tp, tn, fp, fn, discarded_fn):
        self.tp = tp
        self.tn = tn
        self.fp = fp
        self.fn = fn
        self.discarded_fn = discarded_fn
        tp, tn, fp, fn, discarded_fn = map(float, [tp, tn, fp, fn, discarded_fn])
        self.precision = tp / (tp + fp)
        self.recall = tp / (tp + fn)
        self.recall_adjusted = tp / (tp + fn + discarded_fn)
        self.accuracy = (tp + tn) / (tp + tn + fp + fn)
        self.f1 = 2 * (self.precision * self.recall_adjusted) / (self.precision + self.recall_adjusted)

    def round(self):
        self.precision = round(self.precision * 10000.) / 100
        self.recall = round(self.recall * 10000.) / 100
        self.recall_adjusted = round(self.recall_adjusted * 10000.) / 100
        self.accuracy = round(self.accuracy * 10000.) / 100
        self.f1 = round(self.f1 * 10000.) / 100
        return self

    def csv(self):
        return ','.join(
            map(str,
                [self.tp, self.tn, self.fp, self.fn
                    , self.discarded_fn, self.precision, self.recall, self.recall_adjusted, self.accuracy, self.f1]))