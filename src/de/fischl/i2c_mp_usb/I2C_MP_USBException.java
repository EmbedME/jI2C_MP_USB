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

/**
 * Exception regarding USBtin
 *
 * @author Thomas Fischl <tfischl@gmx.de>
 */
public class I2C_MP_USBException extends Exception {

    /**
     * Standard constructor
     */
    public I2C_MP_USBException() {
        super();
    }

    /**
     * Construct exception
     * 
     * @param message Message string
     */
    public I2C_MP_USBException(String message) {
        super(message);
    }

    /**
     * Construct exception
     * 
     * @param message Message string
     * @param cause Cause of exception
     */
    public I2C_MP_USBException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct exception
     * 
     * @param cause Cause of exception
     */
    public I2C_MP_USBException(Throwable cause) {
        super(cause);
    }
}
