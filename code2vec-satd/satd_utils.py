def prop2dict(prop: str):
    def accept(line):
        line = line.strip()
        return len(line) > 0 and not line.startswith('#')

    def split(line):
        res = line.split('=', 2)
        if len(res) == 1:
            res = res + [None]
        res[0] = res[0].strip()
        return res

    pair = [split(line) for line in prop.split('\n') if accept(line)]

    d = {key: val for key, val in pair}
    return d


if __name__ == '__main__':
    print(prop2dict('k1=val1\nkx \nky=\n\n # k2=val2\n\nk3=val3\nk4=val4 '))
