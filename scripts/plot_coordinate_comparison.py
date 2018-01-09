import pandas as pd
import matplotlib.pyplot as plt
from argparse import ArgumentParser

parser = ArgumentParser()
parser.add_argument('inputfile')
args = parser.parse_args()


df = pd.read_json(args.inputfile)
df['obstime'] = pd.to_datetime(df['obstime'])
df['distance'] *= 3600

print('Distances between FACT-Tools and astropy in arcseconds')
print(df.distance.describe())

fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)
ax.set_title('Comparison of FACT-Tools coordinate trafo with astropy')
ax.hist(df.distance, bins=100)
ax.set_xlabel('distance / arcseconds')
fig.tight_layout(pad=0)

fig = plt.figure()
ax = fig.add_subplot(1, 1, 1)
ax.set_title('Comparison of FACT-Tools coordinate trafo with astropy')
df.plot('obstime', 'distance', ax=ax)
ax.set_ylabel('distance / arcseconds')
fig.tight_layout(pad=0)

plt.show()
