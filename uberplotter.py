#! /usr/bin/env python
import matplotlib.pyplot as plt
import json, os

NUMBER_OF_EPOCHS = 10000
LEGEND_POSITION = 3
LATEST_OUTPUT_DIRECTORY = 'acrstr-latest'

processed_files = []

def read_files():
    files = os.listdir(LATEST_OUTPUT_DIRECTORY)
    os.chdir(LATEST_OUTPUT_DIRECTORY)
    for a_file in files:
        values = []
        opened_file = open(a_file, 'r')
        info = json.loads(opened_file.readline())
        for line in opened_file:
            values.append(float(line))
        plot_data(values, info)

def plot_data(values, info):
    plt.plot(range(1,1+NUMBER_OF_EPOCHS), values)

if __name__ == '__main__':
    read_files()
    plt.show()
