var ardrone = require('ar-drone');
var winston = require('winston');
var autonomy = require('ardrone-autonomy');
var io = require('socket.io');
var port = 4242;
var flying = 0;
var client = ardrone.createClient();
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

//callback for arclient
var callback = function(err) { if (err) console.log(err); };

var turncccallback = function(cb) {
	controller.cw(180, cb);

};

var hovercallback = function() {
	controller.hover();
	controller.cw(180, moveforwardcallback);
};

var moveforwardcallback = function() {
	//controller.forward(0.5, callback);
	controller.forward(0.5, backwardscallback);
	controller.up(1, callback);
};

var backwardscallback = function() {
	controller.backward(0.5, callback);
	controller.down(1, callback);
};

//event handlers for the android app.
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
	});

	socket.on('testdata', function(data) {
		if(flying == 1){
			controller.zero();
			controller.altitude(1, hovercallback);
			//controller.left(0.5, callback);
			//controller.right(0.5, callback);

		}
	});

});
