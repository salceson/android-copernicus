# coding=utf-8
import json
import socket
import struct
import thread

from flask import Flask, jsonify, request, abort
from gcm import *
from gcm.gcm import GCMException


app = Flask('Android-Copernicus-Server')

MCAST_GRP = '234.6.6.6'
MCAST_PORT = 3666
DEBUG = True
SERVER_IP = ''
PORT = 20666
ALARM_MODE = False
DEVICES = set()
API_KEY = ""

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(('', MCAST_PORT))
mreq = struct.pack("4sl", socket.inet_aton(MCAST_GRP), socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)


def send(command):
    if DEBUG:
        print 'SENT: "%s"' % command
    sock.sendto(command, (MCAST_GRP, MCAST_PORT))
    pass


@app.route('/lights/', methods=['POST'])
def lights():
    # noinspection PyBroadException
    try:
        data = json.loads(request.data)
    except:
        abort(400)
        return
    if DEBUG:
        print data
    if 'floor' not in data or 'room' not in data or 'operation' not in data:
        abort(400)
        return
    msg = ";".join([str(data['floor']), str(data['room']), 'lamp', str(data['operation'])])
    send(msg)
    return jsonify({'status': 'OK'})


@app.route('/alarm/', methods=['GET'])
def get_alarm():
    global ALARM_MODE
    return jsonify({'alarm': 'on' if ALARM_MODE else 'off'})


@app.route('/alarm/', methods=['POST'])
def set_alarm():
    global ALARM_MODE
    # noinspection PyBroadException
    try:
        data = json.loads(request.data)
    except:
        abort(400)
        return
    if DEBUG:
        print data
    if "mode" not in data:
        abort(400)
        return
    mode = data['mode']
    if mode != 'on' and mode != 'off':
        abort(400)
        return
    ALARM_MODE = (mode == 'on')
    print ALARM_MODE
    return jsonify({'status': 'OK'})


@app.route('/device/', methods=['POST'])
def register_device():
    global DEVICES
    # noinspection PyBroadException
    try:
        data = json.loads(request.data)
    except:
        abort(400)
        return
    if DEBUG:
        print data
    if "id" not in data:
        abort(400)
        return
    DEVICES.add(data['id'])
    return jsonify({'status': 'OK'})


def thread_func():
    global sock, ALARM_MODE, DEVICES, API_KEY
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
        if device == 'motion' and operation == 'triggered' and ALARM_MODE:
            for registration_id in DEVICES:
                print registration_id
                # noinspection PyBroadException
                try:
                    gcm_connection = GCM(API_KEY)
                    data = {'status': 'alarm_triggered', 'floor': str(floor), 'room': str(room)}
                    gcm_connection.plaintext_request(registration_id=registration_id, data=data)
                    print "Done"
                except GCMException as e:
                    print e


if __name__ == '__main__':
    thread.start_new_thread(thread_func, ())
    app.run(port=PORT, host=SERVER_IP)
