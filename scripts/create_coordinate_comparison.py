from argparse import ArgumentParser
from astropy.time import Time
from astropy import units as u
from astropy.coordinates import SkyCoord, EarthLocation, AltAz
import numpy as np
import json

parser = ArgumentParser()
parser.add_argument('source')
parser.add_argument('outputfile')


if __name__ == '__main__':
    args = parser.parse_args()

    orm = EarthLocation.from_geodetic(
        lon=-17.891116 * u.deg,
        lat=28.761647 * u.deg,
        height=2200 * u.meter,
    )

    t0 = Time('2000-01-01 00:00:05', scale='utc')

    coord = SkyCoord.from_name(args.source)
    times = t0 + np.arange(0, 365 * 20 * 24, 6) * u.hour
    frame = AltAz(obstime=times, location=orm)
    coord_t = coord.transform_to(frame)

    results = [{
        'name': args.source,
        'ra': coord.ra.hourangle,
        'dec': coord.dec.deg,
        'az': az,
        'zd': zd,
        'obstime': t.iso,
    } for zd, az, t in zip(coord_t.zen.deg, coord_t.az.deg, times)]

    with open(args.outputfile, 'w') as f:
        json.dump(results, f)
