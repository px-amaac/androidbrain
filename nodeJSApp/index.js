var ardrone = require('ar-drone');
var winston = require('winston');
var autonomy = require('ardrone-autonomy');
var io = require('socket.io');
var port = 4242;
var flying = 0;
var client = ardrone.createClient();
var connected = false;
//force client to send gps with nav data
client.config('general:navdata_options', 777060865);

client.on('navdata', function(navdata) {
	winston.log('info', navdata);
	winston.log('info', navdata.gps);
});

var controller = new autonomy.Controller(client, {debug: false});

winston.add(winston.transports.File, { filename: 'dronebrain.log' });
winston.remove(winston.transports.Console);
var server = io.listen(port);
var gotome = false;
//callback for arclient
var callback = function(err) { if (err) console.log(err); };

//event handlers for the android app.
server.sockets.on('connection', function(socket) {
	
	controller.on('localxy', function(data) {
		socket.emit('localxy', data);
	});
	
	client.on('navdata', function(navdata) {
		if(!connected) {
			connected = true;
			console.log("droneconnected");
		}
	socket.emit('dronedata', navdata);
	});
	socket.on('location', function(data) {
		winston.log('info', data.latitude);
		winston.log('info', data.longitude);
		console.log("long" + data.longitude);
		console.log("lat" + data.latitude);	
		var loc = {lat:0.0, lon:0.0};
		loc.lat = data.latitude;
		loc.lon = data.longitude;
		if(gotome){
			controller.gotolatlon(loc, callback);
		}
	});
	
	socket.on('takeoff', function(data) {
		winston.log('info', data.message);
		console.log("TAKEOFFDRONE");
		if(flying == 0) {
			client.takeoff(callback);
			flying = 1;	
		} else {
			client.land(callback);
			flying = 0;
		}
	});
	
	socket.on('calibrate', function(data) {
		winston.log('info', data.message);
		console.log("CALIBRATE");
		//calibrate drone
		client.calibrate(0);
	});

	socket.on('reset', function(data) {
		winston.log('info', data.message);
		console.log("RESET");
		client.disableEmergency();
	});
	socket.on('zero', function(data) {
		console.log("ZERO");
		controller.zero();
		if(gotome)
		{
			gotome = false;
		}
		else
		{
			gotome = true;
		}
	});

	socket.on('forward', function(data) {
		console.log("FROWARD");
		controller.forward(2, callback);

	});
	socket.on('backward', function(data) {
		console.log("BACKWARD");
		controller.backward(2, callback);

	});
	socket.on('left', function(data) {
		console.log("LEFT");
		controller.left(2, callback);

	});
	socket.on('right', function(data) {
		console.log("RIGHT");
		controller.right(2, callback);

	});
	socket.on('clockwise', function(data) {
		console.log("CLOCKWISE");
		controller.cw(45, callback);

	});
	socket.on('up', function(data) {
		console.log("UP");
		controller.up(1, callback);

	});
	socket.on('down', function(data) {
		console.log("DOWN");
		controller.down(1, callback);

	});

});
