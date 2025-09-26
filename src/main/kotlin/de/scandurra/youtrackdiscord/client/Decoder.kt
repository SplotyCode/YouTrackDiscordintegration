package de.scandurra.youtrackdiscord.client

import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.GZIPInputStream

object Decoder {
    fun decodeBase64GzipToString(b64: String): String =
        gunzip(Base64.getDecoder().decode(b64))

    fun gunzip(bytes: ByteArray): String {
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gis ->
            return gis.readAllBytes().decodeToString()
        }
    }
}