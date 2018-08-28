from argparse import ArgumentParser
from astropy.time import Time
from astropy import units as u
from astropy.coordinates import SkyCoord, EarthLocation, AltAz
import yaml
import json

parser = ArgumentParser()
parser.add_argument('config')
parser.add_argument('outputfile')


if __name__ == '__main__':
    args = parser.parse_args()

    orm = EarthLocation.from_geodetic(
        lon=-17.891116 * u.deg,
        lat=28.761647 * u.deg,
        height=2200 * u.meter,
    )

    with open(args.config) as f:
        sources = yaml.safe_load(f)

    data = []
    for source in sources:
        result = {}

        if 'name' in source:
            coord = SkyCoord.from_name(source['name'])
        else:
            coord = SkyCoord(ra=source['ra'] * u.hourangle, dec=source['dec'] * u.dec)

        t = Time(source['obstime'], scale='utc')
        frame = AltAz(obstime=t, location=orm)
        coord_t = coord.transform_to(frame)

        if coord_t.az.deg > 180:
            az = coord_t.az.deg - 360
        else:
            az = coord_t.az.deg

        zd = coord_t.zen.deg

        result['name'] = source.get('name', '')
        result['ra'] = coord.ra.hourangle
        result['dec'] = coord.dec.deg
        result['az'] = az
        result['zd'] = zd
        result['obstime'] = t.iso
        data.append(result)

    with open(args.outputfile, 'w') as f:
        json.dump(data, f)
