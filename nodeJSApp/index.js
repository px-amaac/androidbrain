var os = require('os');
var net = require('net');
var networkInterfaces = os.networkInterfaces()

var port = 4242;
var count = 1;

function callback_server_connection(socket){
	var remoteAddress = socket.remoteAddress;
	var remotePort = socket.remotePort;
	socket.setNoDelay(true);
	console.log("Connected: ", remoteAddress, " : ", remotePort);
	var msg = 'Hello ' + remoteAddress + ' : ' + remotePort + '\r\n'
		+ "You are #" + count + '\r\n';
	count++
	socket.end(msg);

	socket.on('data', function(data) {
		console.log(data.toString());
	});

	socket.on('end', function() {
		console.log("ended: ", remoteAddress, " : ", remotePort);
	});
}

console.log("nodejs server is waiting:");
for(var interface in networkInterfaces) {
	networkInterfaces[interface].forEach(function(details) {
		if((details.family=='IPv4') && !details.internal) {
			console.log(interface, details.address);
		}
	});
}

console.log("port: ", port);
var netServer = net.createServer(callback_server_connection);
netServer.listen(port)
