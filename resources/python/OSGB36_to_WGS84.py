#!/usr/bin/python

from ostn02python.OSGB import *;
from ostn02python.OSTN02 import *;
from ostn02python.transform import *;
import six;
import csv;
import sys;

# takes csv with two cols (no header) for easting and northings co-ords
# returns a csv with two cols corresponding to lat/long
#
#"../data/fire_stations_OSGB36.csv"
#"../data/fire_stations_WGS84.csv"

OSGB36_data = open(sys.argv[1]);

with open(sys.argv[2], "wb") as csvfile:
    outfile = csv.writer(csvfile, delimiter=',')
    for line in OSGB36_data:
        cols = line.rstrip("\n").split(",")
        easting = cols[0]
        northing = cols[1]
        lat, long = grid_to_ll(int(easting),int(northing))
        outfile.writerow([lat, long])

OSGB36_data.close()
