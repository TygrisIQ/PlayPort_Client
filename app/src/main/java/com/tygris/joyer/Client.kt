package com.tygris.joyer

import android.content.Context
import android.net.wifi.WifiManager
import android.os.StrictMode
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun discoverServer(onFound: (String) -> Unit) {
    Thread {
        val messageStr = "IamClient"
        val socket = DatagramSocket()
        try {
            socket.broadcast = true
            val sendData = messageStr.toByteArray()
            val bufferSize = 1024;
            val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName("255.255.255.255"), 8005);
            socket.send(sendPacket)


            val recvBuf = ByteArray(bufferSize)
            val receivePacket = DatagramPacket(recvBuf, recvBuf.size)

            try {
                socket.receive(receivePacket)
                val response = String(receivePacket.data, 0, receivePacket.length)
                Log.d("TAGGY", "Received response: $response from ${receivePacket.address.hostAddress}")
                onFound(receivePacket.address.hostAddress ?: "not-found")
            } catch (e: SocketTimeoutException) {
                Log.e("TAGGY", "No response received within timeout")
            }
        } catch (e: IOException) {
            Log.e("TAGGY", e.toString());
        }finally{
            socket.close()
        }
    }.start()
}