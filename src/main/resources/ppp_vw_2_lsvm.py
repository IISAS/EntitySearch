__author__ = 'marek'

import argparse

def get_hash(in_str):
    return abs(hash(in_str)) % (10 ** 5)

def convert(in_file, out_file, ignore_ns):
    f = open(out_file,'w')
    with open(in_file, 'rb') as file:
        for row in file.readlines():
            parts = row.rstrip().split('|')
            target = 0 if parts[0].split(' ')[0].startswith('-') else 1

            outline = str(target)
            for j in range(1,len(parts)):
                ns = parts[j].strip()
                tokens = ns.split(' ')
                ns_name = tokens[0].strip()
                if not ignore_ns or (ignore_ns and ns_name not in ignore_ns):
                    for i in range(1,len(tokens)):
                        token = tokens[i]
                        key = token
                        val = None
                        if ':' in token:
                            key, val = token.split(':')
                        outline += (' '+str(get_hash(key))+':') + (val if val else '1')
            f.write(outline.replace('\n','')+'\n')
    f.close()




def main(args):
    field_idx = 0
    feature_idx = 0
    convert(args.input, args.out, args.ignore)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Convert VW input to LibSVM input')
    parser.add_argument('--input', type=str, help='input file - VW formatted')
    parser.add_argument('--out', type=str, help='output file ')
    parser.add_argument('--ignore', type=str, help='ns to ignore ')

    args = parser.parse_args()
    main(args)
