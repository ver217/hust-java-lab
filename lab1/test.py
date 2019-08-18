import subprocess

input_args = [
    '-S  5  -I  1  2  3  4 -O 2 -O 2',
    '-S  5  -I  1  2  3  4 -O 2 -I 5 6',
    '-S  5 -O 0 -I  1  2  3  4 -O 5 -I 1',
    '-S  5  -I  1  2  3  4 -O 2 -I 5 6 7 -I 8',
    '-S  5  -I  1  2  3  4 -I 5 6 7 8 -O 3 -I 9 0 -I 1',
    '-S  3 -I 1 2 3 -O 1 -I 5 6',
    '-S  3 -I 1 2 3 4 -I 5 6 -O 6 -O 1'
]

outputs = [
    'S  5  I  1  2  3  4  O  3  4  O',
    'S  5  I  1  2  3  4  O  3  4  I  3  4  5  6',
    'S  5  O  I  1  2  3  4  O  E',
    'S  5  I  1  2  3  4  O  3  4  I  3  4  5  6  7  I  3  4  5  6  7  8',
    'S  5  I  1  2  3  4  I  1  2  3  4  5  6  7  8  O  4  5  6  7  8  I  4  5  6  7  8  9  0  I  E',
    'S  3  I  1  2  3  O  2  3  I  2  3  5  6',
    'S  3  I  1  2  3  4  I  1  2  3  4  5  6  O  O  E'
]

if __name__ == '__main__':
    true_cnt = 0
    for i, input_arg in enumerate(input_args):
        print('test: java Main ' + input_arg)
        status, out = subprocess.getstatusoutput('java Main ' + input_arg)
        outs = out.split('\n')
        if len(outs) > 1:
            print("\n".join(outs[1:]))
            out = outs[0]
        print('user output: ' + out)
        print('standard output: ' + outputs[i])
        if out.split() == outputs[i].split():
            print('True\n')
            true_cnt += 1
        else:
            print('False\n')
    print(f'total: {true_cnt}/{len(input_args)}')
