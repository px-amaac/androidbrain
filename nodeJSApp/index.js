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
	if(!connected){
		connected = true;
		console.log("droneconnected");
	}
	winston.log('info', navdata);
	winston.log('info', navdata.gps);
});

var controller = new autonomy.Controller(client, {debug: false});

winston.add(winston.transports.File, { filename: 'dronebrain.log' });
winston.remove(winston.transports.Console);
var server = io.listen(port);

//callback for arclient
var callback = function(err) { if (err) console.log(err); };

var turncccallback = function(cb) {
	controller.cw(180, cb);

};

var hovercallback = function() {
	controller.hover();
};

var moveforwardcallback = function() {
	controller.forward(0.5, callback);
};

//event handlers for the android app.
server.sockets.on('connection', function(socket) {
	client.on('navdata', function(navdata) {
		if(connected) {
			connected = true;
			console.log("droneconnected");
		}
		socket.emit('dronedata', navdata);
	});
	socket.on('location', function(data) {
		winston.log('info', data.latitude);
		winston.log('info', data.longitude);
		console.log("long" + data.gps.longitude);
		console.log("lat" + data.gps.latitude);	
	});
	
	socket.on('takeoff', function(data) {
		winston.log('info', data.message);
		console.log("TAKEOFFDRONE");
		if(flying == 0) {
		//	client.takeoff(callback);
			flying = 1;	
		} else {
		//	client.land(callback);
			flying = 0;
		}
	});
	
	socket.on('calibrate', function(data) {
		winston.log('info', data.message);
		console.log("CALIBRATE");
		//calibrate drone
		//client.calibrate(0);
	});

	socket.on('reset', function(data) {
		winston.log('info', data.message);
		console.log("RESET");
	});

	socket.on('testdata', function(data) {
		console.log("zero drone");
		controller.zero();
		console.log("lat " + controller.origin().lat + "lon " + controller.origin().lon);

	});

});
