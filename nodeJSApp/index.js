var os = require('os');
var net = require('net');
var arDrone = require('ar-drone');
var winston = require('winston');
var autonomy = require('ardrone-autonomy');
var io = require('socket.io');
var port = 4242;

winston.add(winston.transports.File, { filename: 'dronebrain.log' });
winston.remove(winston.transports.Console);
var server = io.listen(port);

server.sockets.on('connection', function(socket) {
	socket.on('location', function(data) {
		winston.log('info', data.latitude);
		winston.log('info', data.longitude);
		console.log("long" + data.longitude);
		console.log("lat" + data.latitude);	
	});
	
	socket.on('takeoff', function(data) {
		winston.log('info', data.message);
		console.log("TAKEOFFDRONE");
	});
	
	socket.on('calibrate', function(data) {
		winston.log('info', data.message);
		console.log("CALIBRATE");
	});

	socket.on('reset', function(data) {
		winston.log('info', data.message);
		console.log("RESET");
	});
});
