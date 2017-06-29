import numpy as np
from astropy.time import Time

# /**
#  * ApproxNutation returns a fast approximation of nutation in longitude (Δψ)
#  * and nutation in obliquity (Δε) for a given JDE.
#  *
#  * Accuracy is 0.5″ in Δψ, 0.1″ in Δε.
#  *
#  * Result units are radians.
#  *
#  * @param {number} jde - Julian ephemeris day
#  * @return {number[]} [Δψ, Δε] - [longitude, obliquity] in radians
#  */
# M.approxNutation = function (jde) {
#   var T = (jde - base.J2000) / 36525
#   var Ω = (125.04452 - 1934.136261 * T) * Math.PI / 180
#   var L = (280.4665 + 36000.7698 * T) * Math.PI / 180
#   var N = (218.3165 + 481267.8813 * T) * Math.PI / 180
#   var [sΩ, cΩ] = base.sincos(Ω)
#   var [s2L, c2L] = base.sincos(2 * L)
#   var [s2N, c2N] = base.sincos(2 * N)
#   var [s2Ω, c2Ω] = base.sincos(2 * Ω)
#   var Δψ = (-17.2 * sΩ - 1.32 * s2L - 0.23 * s2N + 0.21 * s2Ω) / 3600 * (Math.PI / 180)
#   var Δε = (9.2 * cΩ + 0.57 * c2L + 0.1 * c2N - 0.09 * c2Ω) / 3600 * (Math.PI / 180)
#   return [Δψ, Δε] // (Δψ, Δε float)
# }
#

    #
	# /**
	#  * MeanObliquity returns mean obliquity following the IAU 1980 polynomial. <br>
	#  * Accuracy is 1" over the range 1000 to 3000 years and 10" over the range 0 to 4000 years.
	#  *
	#  * @function meanObliquity
	#  * @static
	#  *
	#  * @param {A.JulianDay} jdo - julian day
	#  * @return {number} result in radians
	#  */
	# meanObliquity: function (jdo) {
	# 	// (22.2) p. 147
	# 	return A.Math.horner(jdo.jdeJ2000Century(),
	# 		[84381.448/3600*(Math.PI/180), // = A.Coord.calcAngle(false,23, 26, 21.448),
	# 		-46.815/3600*(Math.PI/180),
	# 		-0.00059/3600*(Math.PI/180),
	# 		0.001813/3600*(Math.PI/180)]);
	# },


	# jdJ2000Century: function () {
	# 	// The formula is given in a number of places in the book, for example
	# 	// (12.1) p. 87. (22.1) p. 143. (25.1) p. 163.
	# 	return (this.jd - A.J2000) / A.JulianCentury;
	# },

#     // NutationInRA returns "nutation in right ascension" or "equation of the
# // equinoxes."
# func NutationInRA(jde float64) unit.HourAngle {
# 	// ch 12, p.88
# 	Δψ, Δε := Nutation(jde)
# 	ε0 := MeanObliquity(jde)
# 	return unit.HourAngle(Δψ.Rad() * math.Cos((ε0 + Δε).Rad()))
# }


baseJ2000 = 2451545.0
julianCentury = 36525

def jdeJ2000Century(jde):
    return (jde - baseJ2000)/ julianCentury

def mean_obliquity_rad(jde):
    x = jdeJ2000Century(jde)
    c = 3600*(np.pi/180)
    # first coefficent A.Coord.calcAngle(false,23, 26, 21.448). whatever that implies
    coefficents = [84381.448/c, -46.815/c, -0.00059/c, 0.001813/c]
    p = np.polyval(coefficents, x)
    return p #in radians

def sincos(x):
    return np.sin(x), np.cos(x)


def approx_nutation(jde):
    T = (jde - baseJ2000) / 36525
    Ω = (125.04452 - 1934.136261 * T) * np.pi / 180
    L = (280.4665 + 36000.7698 * T) * np.pi / 180
    N = (218.3165 + 481267.8813 * T) * np.pi / 180
    sΩ, cΩ = sincos(Ω)
    s2L, c2L = sincos(2 * L)
    s2N, c2N = sincos(2 * N)
    s2Ω, c2Ω = sincos(2 * Ω)
    Δψ = (-17.2 * sΩ - 1.32 * s2L - 0.23 * s2N + 0.21 * s2Ω) / 3600 * (np.pi / 180)
    Δε = (9.2 * cΩ + 0.57 * c2L + 0.1 * c2N - 0.09 * c2Ω) / 3600 * (np.pi / 180)
    return Δψ, Δε # in radians. I hope

def nutation_in_ra(jde):
    Δψ, Δε = approx_nutation(jde)
    ε0 = mean_obliquity_rad(jde)
    return Δψ * np.cos(ε0 + Δε)

    #
	# /**
	#  * Calculates the decimal year of a given julian day
	#  * @function decimalYear
	#  * @static
	#  *
	#  * @param {number} jd - julian day
	#  * @return {number} decimal year
	#  */
	# decimalYear: function (jd) {
	# 	var cal = A.JulianDay.jdToCalendar(jd);
	# 	return  cal.y + (cal.m - 0.5) / 12;
	# },

# /**
#  * Estimate Delta T for the given Calendar. This is based on Espenak and Meeus, "Five Millennium Canon of
#  * Solar Eclipses: -1999 to +3000" (NASA/TP-2006-214141).
#  * see http://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html
#  * @function estimate
#  * @static
#  *
#  * @param {number} jd - julian day
#  * @return {number} estimated delta T value (seconds)
#  */

# 
# def jd2jde(jd):
#     return jd + estimateDeltaT(jd)

def estimateDeltaT(jd):
    year = Time(jd, format='jd').decimalyear

    if year < 2050:
        t = year - 2000
        return  62.92 + 0.32217 * t + 0.005589 * t**2

    #default
    u = (year - 1820) / 100
    return  -20 + 32 * u**2

	# estimate: function (jd) {
	# 	var year = A.DeltaT.decimalYear(jd);
	# 	var pow = Math.pow;
	# 	var u, t;
	#
	# 	if (year < -500) {
	# 		u = (year - 1820) / 100;
	# 		return -20 + 32 * pow(u, 2);
	# 	}
	# 	if (year < 500) {
	# 		u = year / 100;
	# 		return  10583.6 - 1014.41 * u + 33.78311 * pow(u, 2) - 5.952053 * pow(u, 3) -
	# 				0.1798452 * pow(u, 4) + 0.022174192 * pow(u, 5) + 0.0090316521 * pow(u, 6);
	# 	}
	# 	if (year < 1600) {
	# 		u = (year - 1000) / 100;
	# 		return  1574.2 - 556.01 * u + 71.23472 * pow(u, 2) + 0.319781 * pow(u, 3) -
	# 				0.8503463 * pow(u, 4) - 0.005050998 * pow(u, 5) + 0.0083572073 * pow(u, 6);
	# 	}
	# 	if (year < 1700) {
	# 		t = year - 1600;
	# 		return  120 - 0.9808 * t - 0.01532 * pow(t, 2) + pow(t, 3) / 7129;
	# 	}
	# 	if (year < 1800) {
	# 		t = year - 1700;
	# 		return  8.83 + 0.1603 * t - 0.0059285 * pow(t, 2) + 0.00013336 * pow(t, 3) - pow(t, 4) / 1174000;
	# 	}
	# 	if (year < 1860) {
	# 		t = year - 1800;
	# 		return  13.72 - 0.332447 * t + 0.0068612 * pow(t, 2) + 0.0041116 * pow(t, 3) - 0.00037436 * pow(t, 4) +
	# 				0.0000121272 * pow(t, 5) - 0.0000001699 * pow(t, 6) + 0.000000000875 * pow(t, 7);
	# 	}
	# 	if (year < 1900) {
	# 		t = year - 1860;
	# 		return  7.62 + 0.5737 * t - 0.251754 * pow(t, 2) + 0.01680668 * pow(t, 3) -
	# 				0.0004473624 * pow(t, 4) + pow(t, 5) / 233174;
	# 	}
	# 	if (year < 1920) {
	# 		t = year - 1900;
	# 		return  -2.79 + 1.494119 * t - 0.0598939 * pow(t, 2) + 0.0061966 * pow(t, 3) - 0.000197 * pow(t, 4);
	# 	}
	# 	if (year < 1941) {
	# 		t = year - 1920;
	# 		return  21.20 + 0.84493 * t - 0.076100 * pow(t, 2) + 0.0020936 * pow(t, 3);
	# 	}
	# 	if (year < 1961) {
	# 		t = year - 1950;
	# 		return  29.07 + 0.407 * t - pow(t, 2) / 233 + pow(t, 3) / 2547;
	# 	}
	# 	if (year < 1986) {
	# 		t = year - 1975;
	# 		return  45.45 + 1.067 * t - pow(t, 2) / 260 - pow(t, 3) / 718;
	# 	}
	# 	if (year < 2005) {
	# 		t = year - 2000;
	# 		return  63.86 + 0.3345 * t - 0.060374 * pow(t, 2) + 0.0017275 * pow(t, 3) + 0.000651814 * pow(t, 4) +
	# 				0.00002373599 * pow(t, 5);
	# 	}
	# 	if (year < 2050) {
	# 		t = year - 2000;
	# 		return  62.92 + 0.32217 * t + 0.005589 * pow(t, 2);
	# 	}
	# 	if (year < 2150) {
	# 		return  -20 + 32 * pow(((year - 1820) / 100), 2) - 0.5628 * (2150 - year);
	# 	}
    #
	# 	// default
	# 	u = (year - 1820) / 100;
	# 	return  -20 + 32 * pow(u, 2);
	# }

# def deltaT(givenMJD):
#     # double theEpoch; /* Julian Epoch */
#     # double t; /* Time parameter used in the equations. */
#     # double D; /* The return value. */
#
#     givenMJD -= 50000;
#
#     theEpoch = 2000. + (givenMJD - 1545.) / 365.25
#
#     # /* For 1987 to 2015 we use a graphical linear fit to the annual tabulation
#     #  * from USNO/RAL, 2001, Astronomical Almanach 2003, p.K9.  We use this up
#     #  * to 2015 about as far into the future as it is based on data in the past.
#     #  * The result is slightly higher than the predictions from that source. */
#     if (1987 <= theEpoch) and (theEpoch <= 2015):
#         t = (theEpoch - 2002.)
#         D = 9.2 * t / 15. + 65.
#         D /= 86400.
#         return D
#
#     elif theEpoch > 2015:
#         # /* Else (between 1600 and 1800 and after 2015) we use the equation from
#         #  * Morrison and Stephenson, quoted as eqation 9.1 in Meeus, 1991,
#         #  * Astronomical Algorithms, p.73. */
#         t  = theEpoch - 1810.;
#         D  = 0.00325 * t * t - 15.;
#         D /= 86400.;
#         return D

def idlMod(a, b):
  """
    Emulate 'modulo' behavior of IDL.

    Parameters
    ----------
    a : float or array
        Numerator
    b : float
        Denominator

    Returns
    -------
    IDL modulo : float or array
        The result of IDL modulo operation.
  """
  if isinstance(a, np.ndarray):
    s = np.sign(a)
    m = np.mod(a, b)
    m[(s < 0)] -= b
  else:
    m = a % b
    if a < 0: m -= b
  return m


def cirrange(x, radians=False):
  """
    Force angle into range.

    Emulates IDL's `cirrange`.

    Parameters
    ----------
    x : float or array
        The angle(s).
    radians : boolean, optional
        If True, angle will be forced into
        0-2pi range. If False (default),
        the 0-360 deg range will be applied.

    Returns
    -------
    Modified angle : float or array
        The angle forced into the 0-360 deg
        or 0-2pi radians range.
  """
  if radians:
    m = 2.0 * np.pi
  else:
    m = 360.0
  return x % m

def nutate(jd, radian=False, plot=False):
  """
    Computes the Earth's nutation in longitude and obliquity for a given (array) of Julian date.

    .. warning:: The output of the IDL routine is in units of arcseconds, whereas the default
                 if this routine returns degrees.

    Parameters
    ----------
    jd : float
         The Julian date
    radian : boolean, optional
         Results are returned in radian instead of in degrees.
         The default is False.
    plot : boolean, optional
         Results are plotted. The default is False.

    Returns
    -------
    Longitude : float
        The nutation in longitude (in deg by default).
    Obliquity : float
        The nutation in latitude (in deg by default).


    Notes
    -----

    .. note:: This function was ported from the IDL Astronomy User's Library.

    :IDL - Documentation:

    NAME:
          NUTATE
    PURPOSE:
          Return the nutation in longitude and obliquity for a given Julian date

    CALLING SEQUENCE:
          NUTATE, jd, Nut_long, Nut_obliq

    INPUT:
          jd - Julian ephemeris date, scalar or vector, double precision
    OUTPUT:
          Nut_long - the nutation in longitude, same # of elements as jd
          Nut_obliq - nutation in latitude, same # of elements as jd

    EXAMPLE:
          (1) Find the nutation in longitude and obliquity 1987 on Apr 10 at Oh.
                 This is example 22.a from Meeus
           IDL> jdcnv,1987,4,10,0,jul
           IDL> nutate, jul, nut_long, nut_obliq
                ==> nut_long = -3.788    nut_obliq = 9.443

          (2) Plot the large-scale variation of the nutation in longitude
                  during the 20th century

          IDL> yr = 1900 + indgen(100)     ;Compute once a year
          IDL> jdcnv,yr,1,1,0,jul          ;Find Julian date of first day of year
          IDL> nutate,jul, nut_long        ;Nutation in longitude
          IDL> plot, yr, nut_long

          This plot will reveal the dominant (18.6 year) period, but a finer
          grid is needed to display the shorter periods in the nutation.
    METHOD:
          Uses the formula in Chapter 22 of ``Astronomical Algorithms'' by Jean
          Meeus (1998, 2nd ed.) which is based on the 1980 IAU Theory of Nutation
          and includes all terms larger than 0.0003".

    PROCEDURES CALLED:
          POLY()                       (from IDL User's Library)
          CIRRANGE, ISARRAY()          (from IDL Astronomy Library)

    REVISION HISTORY:
          Written, W.Landsman (Goddard/HSTX)      June 1996
          Converted to IDL V5.0   W. Landsman   September 1997
          Corrected minor typos in values of d_lng W. Landsman  December 2000
          Updated typo in cdelt term              December 2000
          Avoid overflow for more than 32767 input dates W. Landsman January 2005
  """

  #form time in Julian centuries from 1900.0
  jdcen = (np.array(jd, ndmin=1) - 2451545.0)/36525.0

  #Mean elongation of the Moon
  coef_moon = [1.0/189474.0, -0.0019142, 445267.111480, 297.85036]
  d = np.polyval(coef_moon, jdcen)*np.pi/180.
  d = cirrange(d, radians=True)

  #Sun's mean anomaly
  coef_sun = [-1.0/3e5, -0.0001603, 35999.050340, 357.52772]
  sun = np.polyval(coef_sun, jdcen)*np.pi/180.
  sun = cirrange(sun, radians=True)

  # Moon's mean anomaly
  coef_mano = [1.0/5.625e4, 0.0086972, 477198.867398, 134.96298]
  mano = np.polyval(coef_mano, jdcen)*np.pi/180.
  mano = cirrange(mano, radians=True)

  # Moon's argument of latitude
  coef_mlat = [-1.0/3.27270e5, -0.0036825, 483202.017538, 93.27191]
  mlat = np.polyval(coef_mlat, jdcen)*np.pi/180.
  mlat = cirrange(mlat, radians=True)

  # Longitude of the ascending node of the Moon's mean orbit on the ecliptic,
  #  measured from the mean equinox of the date
  coef_moe = [1.0/4.5e5, 0.0020708, -1934.136261, 125.04452]
  omega = np.polyval(coef_moe, jdcen)*np.pi/180.
  omega = cirrange(omega, radians=True)

  d_lng = np.array([0.,-2.,0.,0.,0.,0.,-2.,0.,0.,-2,-2,-2,0,2,0,2,0,0,-2,0,2,0,0,-2,0,-2,0,0,2, \
           -2,0,-2,0,0,2,2,0,-2,0,2,2,-2,-2,2,2,0,-2,-2,0,-2,-2,0,-1,-2,1,0,0,-1,0,0, \
           2,0,2], float)

  m_lng = np.array([0,0,0,0,1,0,1,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,1,0,-1,0,0,0,1,1,-1,0, \
           0,0,0,0,0,-1,-1,0,0,0,1,0,0,1,0,0,0,-1,1,-1,-1,0,-1], float)

  mp_lng = np.array([0,0,0,0,0,1,0,0,1,0,1,0,-1,0,1,-1,-1,1,2,-2,0,2,2,1,0,0,-1,0,-1, \
            0,0,1,0,2,-1,1,0,1,0,0,1,2,1,-2,0,1,0,0,2,2,0,1,1,0,0,1,-2,1,1,1,-1,3,0], float)

  f_lng = np.array([0,2,2,0,0,0,2,2,2,2,0,2,2,0,0,2,0,2,0,2,2,2,0,2,2,2,2,0,0,2,0,0, \
           0,-2,2,2,2,0,2,2,0,2,2,0,0,0,2,0,2,0,2,-2,0,0,0,2,2,0,0,2,2,2,2], float)

  om_lng = np.array([1,2,2,2,0,0,2,1,2,2,0,1,2,0,1,2,1,1,0,1,2,2,0,2,0,0,1,0,1,2,1, \
            1,1,0,1,2,2,0,2,1,0,2,1,1,1,0,1,1,1,1,1,0,0,0,0,0,2,0,0,2,2,2,2], float)

  sin_lng = np.array([-171996, -13187, -2274, 2062, 1426, 712, -517, -386, -301, 217, \
             -158, 129, 123, 63, 63, -59, -58, -51, 48, 46, -38, -31, 29, 29, 26, -22, \
             21, 17, 16, -16, -15, -13, -12, 11, -10, -8, 7, -7, -7, -7, \
             6,6,6,-6,-6,5,-5,-5,-5,4,4,4,-4,-4,-4,3,-3,-3,-3,-3,-3,-3,-3 ], float)

  sdelt = np.array([-174.2, -1.6, -0.2, 0.2, -3.4, 0.1, 1.2, -0.4, 0., -0.5, 0., 0.1, \
           0.,0.,0.1, 0.,-0.1,0.,0.,0.,0.,0.,0.,0.,0.,0.,0., -0.1, 0., 0.1, \
           0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.] , float)

  cos_lng = np.array([ 92025, 5736, 977, -895, 54, -7, 224, 200, 129, -95,0,-70,-53,0, \
              -33, 26, 32, 27, 0, -24, 16,13,0,-12,0,0,-10,0,-8,7,9,7,6,0,5,3,-3,0,3,3, \
              0,-3,-3,3,3,0,3,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0 ], float)

  cdelt = np.array([8.9, -3.1, -0.5, 0.5, -0.1, 0.0, -0.6, 0.0, -0.1, 0.3, \
           0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0., \
           0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.], float)

  # Sum the periodic terms
  n = jdcen.size
  nut_lon = np.zeros(n)
  nut_obliq = np.zeros(n)
  arg = np.outer(d_lng,d) + np.outer(m_lng,sun) + np.outer(mp_lng,mano) + \
        np.outer(f_lng,mlat) + np.outer(om_lng,omega)
  arg = np.transpose(arg)
  sarg = np.sin(arg)
  carg = np.cos(arg)
  for i in range(n):
    nut_lon[i] = 0.0001*np.sum( (sdelt*jdcen[i] + sin_lng)*sarg[i] )
    nut_obliq[i] = 0.0001*np.sum( (cdelt*jdcen[i] + cos_lng)*carg[i] )

  # Until here result are in arcseconds!
  # Convert to degrees
  nut_lon /= 3600.
  nut_obliq /= 3600.
  if radian:
    nut_lon *= (np.pi/180.)
    nut_obliq *= (np.pi/180.)


  return nut_lon, nut_obliq


def co_nutate(jd, ra, dec, radian=False, plot=False, full_output=False):
  """
    Compute the changes in RA and DEC due to the Earth's nutation.

    Parameters
    ----------
    jd : float or array
         The Julian date. If given as array,
         its size must match that of `ra` and `dec`.
    ra : float or array
         The right ascension in degrees.
         If array, it must be same size as `dec`.
    dec : float or array
         The declination in degrees.
         If array, it must be same size as ra.
    radian : boolean, optional
         Results are returned in radian instead of in degrees.
         The default is False.
    plot : boolean, optional
         If True, the results are plotted.
         For single value `jd`, the change in `ra` and `dec` is plotted
         versus `ra` and `dec`.
         For an array of JDs, ra and dec is plotted versus JD.
         The default is False
    full_output : boolean, optional
         If True, the result will also contain the obliquity of the ecliptic,
         the nutation in the longitude and the nutation in the
         obliquity of the ecliptic. The default is False.

    Returns
    -------
    dRa : float or array
        The change in right ascension [by default in deg].
    dDec : float or array
        The change in declination [by default in deg].
    True obliquity : float, optional
        The true obliquity of the ecliptic [by default in deg].
        Only if `full_output` is True.
    dLong : float or array, optional
        The nutation in longitude [by default in deg].
        Only if `full_output` is True.
    dObliquity : float or array, optional
        The nutation in the obliquity of the ecliptic [by default in deg].
        Only if `full_output` is True.

    Notes
    -----

    .. note:: This function was ported from the IDL Astronomy User's Library.

    :IDL - Documentation:

     NAME:
        CO_NUTATE
     PURPOSE:
        Calculate changes in RA and Dec due to nutation of the Earth's rotation
    EXPLANATION:
        Calculates necessary changes to ra and dec due to
        the nutation of the Earth's rotation axis, as described in Meeus, Chap 23.
        Uses formulae from Astronomical Almanac, 1984, and does the calculations
        in equatorial rectangular coordinates to avoid singularities at the
        celestial poles.

    CALLING SEQUENCE:
        CO_NUTATE, jd, ra, dec, d_ra, d_dec, [EPS=, D_PSI =, D_EPS = ]
    INPUTS
       JD: Julian Date [scalar or vector]
       RA, DEC : Arrays (or scalars) of the ra and dec's of interest

      Note: if jd is a vector, ra and dec MUST be vectors of the same length.

    OUTPUTS:
       d_ra, d_dec: the corrections to ra and dec due to nutation (must then
                                   be added to ra and dec to get corrected values).
    OPTIONAL OUTPUT KEYWORDS:
       EPS: set this to a named variable that will contain the obliquity of the
                ecliptic.
       D_PSI: set this to a named variable that will contain the nutation in the
              longitude of the ecliptic
       D_EPS: set this to a named variable that will contain the nutation in the
                          obliquity of the ecliptic
    EXAMPLE:
       (1) Example 23a in Meeus: On 2028 Nov 13.19 TD the mean position of Theta
           Persei is 2h 46m 11.331s 49d 20' 54.54".    Determine the shift in
           position due to the Earth's nutation.

           IDL> jd = JULDAY(11,13,2028,.19*24)       ;Get Julian date
           IDL> CO_NUTATE, jd,ten(2,46,11.331)*15.,ten(49,20,54.54),d_ra,d_dec

                 ====> d_ra = 15.843"   d_dec = 6.217"
    PROCEDURES USED:
       NUTATE
    REVISION HISTORY:
       Written  Chris O'Dell, 2002
       Vector call to NUTATE   W. Landsman   June 2002
  """

  ra = np.array(ra)
  dec = np.array(dec)
  num = ra.size

  # Julian centuries from J2000 of jd.
  jdcen = (np.array(jd) - 2451545.0)/36525.0

  # Must calculate obliquity of ecliptic
  nut = nutate(jd)
  # Change degrees to seconds
  d_psi = nut[0]*3600.
  d_eps = nut[1]*3600.

  eps0 = 23.4392911*3600. - 46.8150*jdcen - 0.00059*jdcen**2 + 0.001813*jdcen**3
  # True obliquity of the ecliptic in radians
  eps = (eps0 + d_eps)/3600.*(np.pi/180.)

  ce = np.cos(eps)
  se = np.sin(eps)

  # convert ra-dec to equatorial rectangular coordinates
  x = np.cos(ra*np.pi/180.) * np.cos(dec*np.pi/180.)
  y = np.sin(ra*np.pi/180.) * np.cos(dec*np.pi/180.)
  z = np.sin(dec*np.pi/180.)

  # apply corrections to each rectangular coordinate
  x2 = x - (y*ce + z*se)*d_psi * (np.pi/(180.*3600.))
  y2 = y + (x*ce*d_psi - z*d_eps) * (np.pi/(180.*3600.))
  z2 = z + (x*se*d_psi + y*d_eps) * (np.pi/(180.*3600.))

  # convert back to equatorial spherical coordinates
  r = np.sqrt(x2**2 + y2**2 + z2**2)
  xyproj = np.sqrt(x2**2 + y2**2)

  ra2 = x2 * 0.
  dec2= x2 * 0.

  if num == 1:
    # Calculate Ra and Dec in RADIANS (later convert to DEGREES)
    if np.logical_and( xyproj == 0 , z != 0 ):
      # Places where xyproj==0 (point at NCP or SCP)
      dec2 = np.arcsin(z2/r)
      ra2 = 0.
    if xyproj != 0:
      # places other than NCP or SCP
      ra2 = np.arctan2( y2 , x2 )
      dec2 = np.arcsin( z2/r )
  else:
    w1 = np.where( np.logical_and( xyproj == 0 , z != 0 ) )[0]
    w2 = np.where( xyproj != 0 )[0]
    # Calculate Ra and Dec in RADIANS (later convert to DEGREES)
    if len(w1) > 0:
      # Places where xyproj==0 (point at NCP or SCP)
      dec2[w1] = np.arcsin(z2[w1]/r[w1])
      ra2[w1] = 0.
    if len(w2) > 0:
      # Places other than NCP or SCP
      ra2[w2] = np.arctan2( y2[w2] , x2[w2] )
      dec2[w2] = np.arcsin( z2[w2]/r[w2] )

  # Convert into DEGREES
  ra2 = ra2/np.pi*180.
  dec2 = dec2/np.pi*180.
  d_psi /= 3600.
  d_eps /= 3600.

  if num == 1:
    if ra2 < 0.: ra2 += 360.
  else:
    w = np.where( ra2 < 0. )[0]
    if len(w) > 0: ra2[w] += 360.

  # Return changes in ra and dec
  d_ra = (ra2 - ra)
  d_dec = (dec2 - dec)

  if radian:
    # convert result to RADIAN
    d_ra *= (np.pi/180.)
    d_dec *= (np.pi/180.)
    d_psi *= (np.pi/180.)
    d_eps *= (np.pi/180.)
  else:
    eps = eps/np.pi*180. # eps in DEGREES


  if full_output:
    return d_ra, d_dec, eps, d_psi, d_eps
  else:
    return d_ra, d_dec
