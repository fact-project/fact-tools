from astropy.time import Time
import astropy.units as u
import numpy as np
import json
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('outputfile')


if __name__ == '__main__':
    args = parser.parse_args()

    times = Time('1990-01-01T00:00:00') + np.arange(0, 50, 0.5) * u.year

    data = []
    for time in times:
        gmst_rad = time.sidereal_time(kind='mean', longitude=0 * u.deg).rad

        data.append({})
        data[-1]['obstime'] = time.iso
        data[-1]['gmst_rad'] = gmst_rad


    with open(args.outputfile, 'w') as f:
        json.dump(data, f)
