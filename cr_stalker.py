#! /usr/bin/env python

import matplotlib.pyplot as plt

file_names = ['qlearning-with-evaluation-cr9-channel-presence.txt', 'qlearning-with-evaluation-cr7-channel-presence.txt',
              'qlearning-with-evaluation-cr5-channel-presence.txt', 'qlearning-with-evaluation-cr3-channel-presence.txt',
              'qlearning-with-evaluation-cr1-channel-presence.txt', 'qlearning-with-evaluation-cr19-channel-presence.txt',
              'qlearning-with-evaluation-cr17-channel-presence.txt', 'qlearning-with-evaluation-cr15-channel-presence.txt',
              'qlearning-with-evaluation-cr13-channel-presence.txt', 'qlearning-with-evaluation-cr11-channel-presence.txt'
              ]

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
    plt.plot(range(1, 10001), yval, c=colors[index])

plt.ylim(0, 1.1e9)

plt.show()
