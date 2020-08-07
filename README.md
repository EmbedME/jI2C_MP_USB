jI2C_MP_USB
===========

jI2C_MP_USB is a Java library for accessing I2C devices with I2C_MP_USB (https://www.fischl.de/i2c-mp-usb/).

Build
-----
Ant is used to build the library from Java source code. To create the JAR file,
use
```
ant jar
```

jI2C_MP_USB depends on usb4java. The usb4java JAR files must be included in
classpath.
http://usb4java.org/


Usage
-----

Add I2C_MP_USB.jar to the Classpath or as Library to your project. E.g. in
Netbeans: File -> Project Properties -> Libraries -> Compile -> Add JAR/Folder

Import the package containing the library in your Java code:
```
import de.fischl.i2c_mp_usb.*;
```

Example - Read byte from a specific register address (byte address = 8 bit):
```
I2C_MP_USB i2c = new I2C_MP_USB();
i2c.connect();
i2c.setBaudrate(100);

// read from I2C device with address 0x50, read from register 0x10
byte test = i2c.readByteData(0x50, 0x10);
System.out.println(String.format("%02X", test));

i2c.disconnect();
```

Example - Read data block from a specific register address (word address = 16 bit):
```
I2C_MP_USB i2c = new I2C_MP_USB();
i2c.connect();
  
int deviceAddress = 0x50;
int register = 0x0000;
int readLength = 256;
            
ByteBuffer bufferOut = ByteBuffer.allocateDirect(2);
bufferOut.put((byte)((register << 8) & 0xff));
bufferOut.put((byte)(register & 0xff));
ByteBuffer bufferIn = ByteBuffer.allocateDirect(readLength);
i2c.transmit(deviceAddress, bufferOut, bufferIn);
            
while (bufferIn.hasRemaining())
    System.out.println(bufferIn.position() + " -> " + String.format("%02X", bufferIn.get()));

i2c.disconnect();
```


Changelog
---------

1.0.0 (2020-08-07)
First release


License
-------

jI2C_MP_USB is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

jI2C_MP_USB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jI2C_MP_USB.  If not, see <http://www.gnu.org/licenses/>.
