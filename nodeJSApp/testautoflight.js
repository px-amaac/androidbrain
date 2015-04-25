var arDrone = require('ar-drone');
var winston = require('winston');
var autonomy = require('ardrone-autonomy');
var mission = autonomy.createMission();

// force land on ctrl-c
var exiting = false;
process.on('SIGINT', function() {
	if(exiting) {
		process.exit(0);
	} else {
		console.log('Got SIGINT. Landing');
		exiting = true;
		mission.control().disable();
		mission.client().land(function() {
			process.exit(0);
		});
	}
});

winston.add(winston.transports.File, { filename: 'gps.log' });
winston.remove(winston.transports.Console);

var client = arDrone.createClient();

//send navdata options command to get gps data back
client.config('general:navdata_options', 777060865);
client.on('navdata', function(navdata) {
	winston.log('info', navdata);
	winston.log('info', navdata.gps);
	console.log(navdata);
});

mission.takeoff()
	.zero()
	.altitude(1)
	.hover(1000)
	.forward(.5)
	.backward(.5)
	.hover(1000)
	.land();

mission.run(function (err, result) {
	if(err) {
		console.trace("Oops, something happened: %s", err.message);
		mission.client().stop();
		mission.client().land();
	} else {
		console.log("Mission Success");
		process.exit(0);
	}
});

