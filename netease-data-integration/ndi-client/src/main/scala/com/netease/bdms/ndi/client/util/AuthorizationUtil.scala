package com.netease.bdms.ndi.client.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import sun.misc.BASE64Encoder

object AuthorizationUtil {

  def generateSignature(secret: String, timestamp: Long = System.currentTimeMillis()): String = {
    val base64Encoder = new BASE64Encoder;
    val messageDigest = MessageDigest.getInstance("MD5")
    base64Encoder.encode(messageDigest.digest((secret + timestamp).getBytes(StandardCharsets.UTF_8)))
  }
}
