#!/usr/bin/env python
# coding=utf-8

__author__ = 'Michał Ciołczyk'

VIRTUAL_COPERNICUS = True
MCAST_GRP = '234.6.6.6'
MCAST_PORT = 3666

if VIRTUAL_COPERNICUS:
    # ----- BEGIN INITIALIZATION -----
    import os
    from serial import Serial

    BASE_DIR = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
    SERIAL_PATH = os.path.join(BASE_DIR, 'dev', 'ttyS0')

    serial = Serial(SERIAL_PATH, 38400)
    # ----- END INITIALIZATION -----
else:
    serial = Serial('/dev/ttyS0', 38400, timeout=1)

import socket

from copernicus import Copernicus


api = Copernicus(serial)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)

alarm_enabled = True


def button1_handler(state):
    print 'Button 1', state
    pass


def button2_handler(state):
    print 'Button 2', state
    pass


def motion_handler(state):
    if alarm_enabled and state:
        print 'Alarm triggered'
    pass


api.command('subscribe', 'button1', 'button2', 'motion')
api.set_handler('button1', button1_handler)
api.set_handler('button2', button2_handler)
api.set_handler('motion', motion_handler)

while True:
    api.listen()
