#!/usr/bin/env python
# coding=utf-8

__author__ = 'Michał Ciołczyk'

VIRTUAL_COPERNICUS = True
MCAST_GRP = '234.6.6.6'
MCAST_PORT = 3666
FLOOR = '1'  # 1 at the moment only
ROOM = 'kitchen'  # kitchen|corridor
DELAY = 5  # in seconds
DEBUG = True

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
import struct
import thread

from time import time

from copernicus import Copernicus


api = Copernicus(serial)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(('', MCAST_PORT))
mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

timestamp = 0
light_on = False


def send(command):
    if DEBUG:
        print 'SENT: "%s"' % command
    sock.sendto(command, (MCAST_GRP, MCAST_PORT))
    pass


def button1_handler(state, disable=False):
    global light_on
    if DEBUG and not disable:
        print 'Button 1', state
    if state:
        light_on = not light_on
        api.command('led', light_on)
    pass


def button2_handler(state):
    if DEBUG:
        print 'Button 2', state
    if state:
        send(str(FLOOR) + ';*;lamp;off')
    pass


def motion_handler(state):
    global timestamp
    if state:
        if DEBUG:
            print 'Motion'
        current_timestamp = time()
        if (current_timestamp - timestamp) > DELAY:
            send(str(FLOOR) + ';' + str(ROOM) + ';motion;triggered')
            timestamp = current_timestamp
    pass


def thread_func():
    global sock, light_on
    while True:
        command = sock.recv(10240)
        if DEBUG:
            print command.split(';')
        tab = command.split(';')

        if len(tab) < 4:
            continue

        floor = tab[0]
        room = tab[1]
        device = tab[2]
        operation = tab[3]

        if floor != FLOOR and floor != "*":
            continue
        if room != ROOM and room != "*":
            continue
        if device != "lamp" and device != "*":
            continue
        if operation == "on":
            api.command('led', True)
            light_on = True
        elif operation == "off":
            api.command('led', False)
            light_on = False
        else:
            button1_handler(True, True)


api.command('subscribe', 'button1', 'button2', 'motion')
api.set_handler('button1', button1_handler)
api.set_handler('button2', button2_handler)
api.set_handler('motion', motion_handler)

thread.start_new_thread(thread_func, ())


while True:
    api.listen()
