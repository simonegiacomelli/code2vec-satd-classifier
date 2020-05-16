import sys
from pathlib import Path

inp = Path(sys.argv[1])
out = Path(sys.argv[2])
print('inp = ', inp, 'out = ', out)
err_count = 0
with open(inp, 'r') as t:
    with open(out, 'w') as o:
        expected = 0
        for line in t:
            tabIndex = line.index('\t')
            header = line[0:tabIndex]
            spaceIndex = header.index(' ')
            status = header[spaceIndex + 1:]
            expected += 1
            actual = int(header.split('_')[0])
            if expected != actual:
                print('Line mismatch !  expected:' + str(expected) + ' actual:' + str(actual))
                print('     in file ' + str(inp))
                exit(1)

            if status == 'OK':
                line = line[tabIndex + 1:]
                if line.startswith('fixed') or line.startswith('satd'):
                    o.write(line)
            else:
                err_count += 1
                print('--------------------------------------------------------------------------------')
                print(f'ERROR {header}\n' + line.replace('\\n', '\n'))
                print('--------------------------------------------------------------------------------')
if err_count > 0:
    exit(1)
else:
    exit(0)
