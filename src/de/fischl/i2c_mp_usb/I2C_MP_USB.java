/*
 * This file is part of jI2C_MP_USB.
 *
 * Copyright (C) 2020  Thomas Fischl 
 *
 * jI2C_MP_USB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jI2C_MP_USB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jI2C_MP_USB.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.fischl.i2c_mp_usb;

import org.usb4java.*;
import java.nio.ByteBuffer;

/**
 * Represents an I2C-MP-USB device.
 * Provide access to I2C devices through I2C-MP-USB (https://www.fischl.de/i2c-mp-usb/).
 *
 * @author Thomas Fischl <tfischl@gmx.de>
 */

public class I2C_MP_USB {

    protected static final short VID = (short) 0x0403;
    protected static final short PID = (short) 0xc631;
    protected static final int CMD_I2C_IO = 4;
    protected static final int CMD_I2C_IO_BEGIN = 1;
    protected static final int CMD_I2C_IO_END = 2;
    protected static final int I2C_M_RD = 0x01;
    protected static final int CMD_START_BOOTLOADER = 0x10;
    protected static final int CMD_SET_BAUDRATE = 0x11;
    protected static final int CMD_GET_STATUS = 3;

    
    /** timeout for libusb actions */
    protected int timeout = 1000;
    
    /** libusb device handler */
    protected DeviceHandle handle = null;
    
    /**
     * Connect to I2C-MP-USB
     * 
     * @throws I2C_MP_USBException Error while connecting to I2C-MP-USB
     */
    public void connect() throws I2C_MP_USBException {
        
        Context context = new Context();
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) throw new I2C_MP_USBException("Unable to initialize libusb");

        handle = LibUsb.openDeviceWithVidPid(context, VID, PID);

        if (handle == null) throw new I2C_MP_USBException("I2C-MP-USB not found");
    }
    
    /**
     * Disconnect I2C-MP-USB
     * 
     * @throws I2C_MP_USBException Error while disconnecting I2C-MP-USB
     */
    public void disconnect() throws I2C_MP_USBException {
        LibUsb.close(handle);
        handle = null;
    }
    
    /**
     * Set I2C baudrate.
     * 
     * @param baudrate I2C clock frequency in kHz
     * @throws I2C_MP_USBException Error while setting baudrate
     */
    public void setBaudrate(int baudrate) throws I2C_MP_USBException {    
        
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(0);
        int transfered = LibUsb.controlTransfer(handle, 
            (byte) (LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_CLASS),
            (byte) (CMD_SET_BAUDRATE),
            (short) baudrate,
            (short) 0x00,
            bufferOut, timeout);

        if (transfered < 0) throw new I2C_MP_USBException("Set baudrate failed");
    }

    /**
     * Probe given address if a device answers
     * 
     * @param deviceAddress I2C device address
     * @return True if device answers
     * @throws I2C_MP_USBException Error while probing device
     */
    public boolean probeDevice(int deviceAddress) throws I2C_MP_USBException {    

        int transfered;
        ByteBuffer buffer = ByteBuffer.allocateDirect(0);

        // Decision whether to read or write like i2cdetect does
        if ((deviceAddress >= 0x30 && deviceAddress <= 0x37) ||
            (deviceAddress >= 0x50 && deviceAddress <= 0x5F)) {

            // read
            transfered = LibUsb.controlTransfer(handle, 
                (byte) (LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_CLASS),
                (byte) (CMD_I2C_IO + CMD_I2C_IO_BEGIN + CMD_I2C_IO_END),
                (short) I2C_M_RD,
                (short) deviceAddress,
                buffer, timeout);

        } else {

            // write
            transfered = LibUsb.controlTransfer(handle, 
                (byte) (LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_CLASS),
                (byte) (CMD_I2C_IO + CMD_I2C_IO_BEGIN + CMD_I2C_IO_END),
                (short) 0x00,
                (short) deviceAddress,
                buffer, timeout);
        }

        // get status
        ByteBuffer bufferIn = ByteBuffer.allocateDirect(1);
        transfered = LibUsb.controlTransfer(handle, 
            (byte) (LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_CLASS),
            (byte) (CMD_GET_STATUS),
            (short) 0x00,
            (short) 0x00,
            bufferIn, timeout);

        if (transfered < 0) throw new I2C_MP_USBException("Probe device failed");

        return bufferIn.get(0) == 1;
    }
     
    /**
     * Jump to bootloader.
     * 
     * @throws I2C_MP_USBException Error while starting bootloader
     */
    public void startBootloader() throws I2C_MP_USBException {    
        
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(0);
        int transfered = LibUsb.controlTransfer(handle, 
            (byte) (LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_CLASS),
            (byte) (CMD_START_BOOTLOADER),
            (short) 0x5237,
            (short) 0x00,
            bufferOut, timeout);

        if (transfered < 0) throw new I2C_MP_USBException("Start bootloader failed");
    }
        
    /**
     * Transmit data to/from given I2C device.
     * 
     * @param deviceAddress I2C device address
     * @param bufferOut Data buffer for sending out
     * @param bufferIn Data buffer for reading in
     * @throws I2C_MP_USBException Error while transmitting data
     */
    public void transmit(int deviceAddress, ByteBuffer bufferOut, ByteBuffer bufferIn) throws I2C_MP_USBException {
        
        if (handle == null) throw new I2C_MP_USBException("Not connected to I2C-MP_USB");
        
        int writeflags = CMD_I2C_IO + CMD_I2C_IO_BEGIN;
        int readflags = CMD_I2C_IO + CMD_I2C_IO_END;
        boolean skipwrite = false;
        boolean skipread = false;

        // check if we have to do a write
        if ((bufferOut == null) || (bufferOut.capacity() == 0)) {
            readflags |= CMD_I2C_IO_BEGIN;
            skipwrite = true;
        }
        
        // check if there is buffer space for read
        if ((bufferIn == null) || (bufferIn.capacity() == 0)) {
            writeflags |= CMD_I2C_IO_END;
            skipread = true;
        }
                
        // I2C write
        if (!skipwrite) {
            int transfered = LibUsb.controlTransfer(handle, 
                (byte) (LibUsb.ENDPOINT_OUT | LibUsb.REQUEST_TYPE_CLASS),
                (byte) writeflags,
                (short) 0x00,
                (short) deviceAddress,
                bufferOut, timeout);

            if (transfered < 0) throw new I2C_MP_USBException("I2C write failed");
        }
        
        // I2C read
        if (!skipread) {
            int transfered = LibUsb.controlTransfer(handle, 
                (byte) (LibUsb.ENDPOINT_IN | LibUsb.REQUEST_TYPE_CLASS),
                (byte) readflags,
                (short) I2C_M_RD,
                (short) deviceAddress,
                bufferIn, timeout);

            if (transfered < 0) throw new I2C_MP_USBException("I2C read failed");
        }
    }
    
    /**
     * Read one byte from given I2C device.
     * 
     * @param deviceAddress I2C device address
     * @return Byte read from I2C device
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public byte readByte(int deviceAddress) throws I2C_MP_USBException {
        ByteBuffer bufferIn = ByteBuffer.allocateDirect(1);
        transmit(deviceAddress, null, bufferIn);
        return bufferIn.get(0);
    }
    
    /**
     * Read one byte from given device at given register address.
     * 
     * @param deviceAddress I2C device address
     * @param register Register address
     * @return Byte read from given device at given register
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public byte readByteData(int deviceAddress, int register) throws I2C_MP_USBException {
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(1);
        bufferOut.put((byte)register);
        ByteBuffer bufferIn = ByteBuffer.allocateDirect(1);
        transmit(deviceAddress, bufferOut, bufferIn);
        return bufferIn.get(0);
    }
    
    /**
     * Read two bytes (one word / LSB) from given device at given register address.
     * 
     * @param deviceAddress I2C device address
     * @param register Register address
     * @return Word (LSB) read from given device at given register
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public int readWordData(int deviceAddress, int register) throws I2C_MP_USBException {
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(1);
        bufferOut.put((byte)register);
        ByteBuffer bufferIn = ByteBuffer.allocateDirect(2);
        transmit(deviceAddress, bufferOut, bufferIn);
        return (bufferIn.get(0) & 0xff) | ((bufferIn.get(1) & 0xff) << 8);
    }
        
    /**
     * Write one byte to given I2C device.
     * 
     * @param deviceAddress I2C device address
     * @param value Byte value to write
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public void writeByte(int deviceAddress, byte value) throws I2C_MP_USBException {
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(1);
        bufferOut.put(value);
        transmit(deviceAddress, bufferOut, null);
    }

    
    /**
     * Write one byte to given device at given register address.
     * 
     * @param deviceAddress I2C device address
     * @param register Register address
     * @param value Byte value to write
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public void writeByteData(int deviceAddress, int register, byte value) throws I2C_MP_USBException {
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(2);
        bufferOut.put((byte)register);
        bufferOut.put(value);
        transmit(deviceAddress, bufferOut, null);
    }
    
    /**
     * Write two bytes (one word / LSB) to given device at given register address.
     * 
     * @param deviceAddress I2C device address
     * @param register Register address
     * @param value Word (LSB) to write
     * @throws I2C_MP_USBException Error while accessing I2C device
     */
    public void writeWordData(int deviceAddress, int register, int value) throws I2C_MP_USBException {
        ByteBuffer bufferOut = ByteBuffer.allocateDirect(3);
        bufferOut.put((byte)register);
        bufferOut.put((byte) (value & 0xff));
        bufferOut.put((byte) ((value >> 8) & 0xff));
        transmit(deviceAddress, bufferOut, null);
    }
    
}
