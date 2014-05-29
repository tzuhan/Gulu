import sys
import re
import string
import subprocess
import socket               # Import socket module
import time
import os

bufferSize = 1024
DrinkNames = [
	"coffee",
	"cola",
	"orange juice",
	"milk tea",
	"black tea"
]
delayTimeBetweenTwoConnection = 1

def main():
	network = None
	host_ip_addr = str(get_gateway_ip())
	portsToConnect = [49152,50000,54321,54213,49999]     # Reserve a port for your service.
	while True:
		for port in portsToConnect:
			try:
				print host_ip_addr + ',' + str(port)
				clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)	# Create a socket object
				clientSocket.connect((host_ip_addr, port))
				print "connect success"
				while True:
					recvStr = clientSocket.recv(bufferSize)
					splitStrs = recvStr.split(':') 
					if len(splitStrs) > 1: #Format: Data:[drinkIndex]
						print DrinkNames[int(splitStrs[1])]						
					else: 
						print recvStr
					clientSocket.sendall(raw_input("wait for input:") + '\n'); #it would return until it send all of data or error happen
			except:
				print sys.exc_info()[0]
				try:
					clientSocket.close()
				except:
					pass
				time.sleep(delayTimeBetweenTwoConnection)


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