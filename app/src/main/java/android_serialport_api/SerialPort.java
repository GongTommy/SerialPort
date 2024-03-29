/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Google官方代码
 *
 * 此类的作用为，JNI的调用，用来加载.so文件的
 *
 * 获取串口输入输出流
 */
public class SerialPort {

	private static final String TAG = "SerialPort";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");//申请root权限
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";//666代表所有用户都有读写权限
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		//开启串口，传入物理地址、波特率、flags值
		mFd = open(device.getAbsolutePath(), baudrate, flags);//
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters  获取串口输入流
	public InputStream getInputStream() {
		return mFileInputStream;
	}
    //获取串口输出流
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI   开启串口
	private native static FileDescriptor open(String path, int baudrate, int flags);
	//关闭串口
	public native void close();
	static {
		System.out.println("==============================");
		//加载库文件.so文件
		System.loadLibrary("serial_port");//so包所在路径/vendor/lib:/system/lib
		System.out.println("********************************");
	}
}
