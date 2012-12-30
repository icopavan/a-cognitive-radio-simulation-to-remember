import os, json

files = os.listdir('acrstr-latest')
os.chdir('acrstr-latest')
for f in files:
	fl = open(f, 'r')
	info = json.loads(fl.readline())
	running_sum = 0.0
	values = 0
	for line in fl:
		running_sum += float(line)
		values += 1
	avg = running_sum / float(values)
	fn = open('../check-last-{}-values.txt'.format(info['checked recent values']), 'w')
	fn.write('{}\n'.format(avg))
