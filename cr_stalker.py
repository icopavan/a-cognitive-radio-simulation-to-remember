#! /usr/bin/env python2.7

import matplotlib.pyplot as plt

cr_list = [ 1, 3 ]
pu_list = [ 1 ]

file_name = 'qlearning-with-evaluation-cr{0}-channel-presence.txt'
file_names = []
for cr in cr_list:
    file_names.append(file_name.format(cr))
for pu in pu_list:
    file_names.append("PU{0}.txt".format(pu))

colors = [ 'blue','green','red','cyan','magenta','yellow','black','Thistle', 'CornflowerBlue', 'PaleVioletRed' ]
              
all_yvals = []
for file_name in file_names:
    f = open(file_name)
    yvals = []
    for l in f:
        yvals.append(l.split(',')[0].split(' ')[0])
    all_yvals.append(yvals)
    f.close()

for index, yval in enumerate(all_yvals):
    plt.plot(range(1, 10001), yval, c=colors[index], linewidth=3.0)

plt.ylim(0, 1.1e9)

plt.show()
