import re
import string
import subprocess
import socket               # Import socket module

bufferSize = 1024
DrinkNames = [
	"coffee",
	"cola",
	"orange juice",
	"milk tea",
	"black tea"
]

def main():
	network = None
	host_ip_addr = get_gateway_ip()


	clientSocket = socket.socket()         # Create a socket object
	portsToConnect = [12345,6789,6890,7899,9000]     # Reserve a port for your service.
	while True:
		for port in portsToConnect:
			try:
				clientSocket.connect((host_ip_addr, port))
				while True:
					recvStr = socket.recv(bufferSize)
					splitStrs = recvStr.split(':') 
					if len(splitStrs) > 1: #Format: Data:[drinkIndex]
						print DrinkNames[int(splitStrs[1])]						
					else: 
						print recvStr
					socket.sendall(raw_input("wait for input")); #it would return until it send all of data or error happen
			except connectError:
				print connectError



	#print s.recv(1024)


	#clientSocket.close                     # Close the socket when done

def getDrinkName(typeIndex):
	return DrinkNames[typeIndex]

def get_gateway_ip():
	cmd = subprocess.Popen("route -n get default", shell=True, stdout=subprocess.PIPE)
	for line in cmd.stdout:
		result = get_ip_info(line)
		if result != -1:
			return result

def get_ip_info(destStr):
	p = re.compile(ur'gateway: (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})')
	matchResult = re.findall(p, destStr)
	if len(matchResult) > 0:
		return matchResult[0]
	else:
		return -1

if __name__ == "__main__":
	main()