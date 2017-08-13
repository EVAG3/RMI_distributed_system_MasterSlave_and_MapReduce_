# RMI distributed system with Master/Slave and MapReduce design
## Author :
Yu Zhang
Contact at thuzyu19@gmail.com

## Description:
This application demonstrate the basic RMI distributed system. The application shows how a heavy loaded task could be mapped into many slaves and collect the result back to the master. A master/slave design is applied for a better efficiency and easier management. This application also demonstrate how the distributed system could communicate between the master and slave. 

## 1. Start the master and slave
The gradle tasks are set up for easier testing. So you could start the server with gradle command. Also, the project could run in Eclipse as the main() method is provided for each server. 

### *NOTICE: You may need to have multiple terminal to do this.
### *NOTICE: Start the slaves before the master. Otherwise, the program may crash as the master could not find the slaves.
### *IMPORTANT: Make sure slaves and master are really running. You should see "[INFO ] Slave server(Slave1) is running".

### 1) gradle clean build
Before start the master and slave, make sure that the project is properly built.
gradle clean build

### 2) gradle startSlave1 / gradle startSlave2

First start the workers, here I provide two local slaves and hard coded the basic information. Please modify there if you want to run the slaves server in other places.
gradle startSlave1
gradle startSlave2

### 3) gradle startMaster
Then start the Master. The master communication information is hard-Coded in the clients servers. So change the ServerInfo in the clients if you want to set up the master in other places.
gradle startMaster

## 2. How to submit the client task
You need to start a client to submit the task.
gradle startClient

Any printable information will be shown in the related server. The result is also saved back into the task. As you read the code within the ClientServer, you will figure out this truth.  
