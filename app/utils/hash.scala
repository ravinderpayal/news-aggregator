package utils

import java.security.MessageDigest
import java.util.Formatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.lang.Byte

import org.apache.commons.codec.binary.Hex
object Crypto {
  def signData(data: String, key: String):String = {
    val signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1")
    val mac: Mac = Mac.getInstance("HmacSHA1")
    mac.init(signingKey)
    val hexBytes = mac.doFinal(data.getBytes())
    new String(new Hex().encode(hexBytes), "UTF-8")
  }
}
object hash{
  /**
    * Source: https://stackoverflow.com/questions/46329956/how-to-correctly-generate-sha-256-checksum-for-a-string-in-scala
   */
  def sha256Hash(text: String) : String = MessageDigest.getInstance("SHA-256")
    .digest(text.getBytes("UTF-8"))
    .map("%02x".format(_)).mkString
}
